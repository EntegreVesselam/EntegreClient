#!/usr/bin/env python3
"""Refresh locally, restore immutable compatibility URIs and verify before succeeding."""
import argparse
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import tomllib

sys.dont_write_bytecode = True
HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('pack_verifier', HERE / 'verify-pack.py')
verifier = importlib.util.module_from_spec(spec)
spec.loader.exec_module(verifier)


def atomic_write(path, data, mode):
    descriptor, temporary = tempfile.mkstemp(prefix='.' + path.name + '-', dir=path.parent)
    try:
        with os.fdopen(descriptor, 'wb') as stream:
            stream.write(data)
            stream.flush()
            os.fsync(stream.fileno())
        os.chmod(temporary, mode)
        os.replace(temporary, path)
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)


def refresh(root, executable):
    retired = verifier.load_retired_paths(root)
    before_pack = (root / 'pack.toml').read_bytes()
    before = tomllib.loads(before_pack.decode('utf-8'))
    index_path = verifier.lexical_path(before['index']['file'])
    index_file = root / index_path
    if not index_file.resolve().is_relative_to(root):
        raise ValueError('index path escapes pack')
    before_index = index_file.read_bytes()
    pack_mode = (root / 'pack.toml').stat().st_mode & 0o777
    index_mode = index_file.stat().st_mode & 0o777
    try:
        subprocess.run([executable, 'refresh'], cwd=root, check=True)
        refreshed = tomllib.loads((root / 'pack.toml').read_text(encoding='utf-8'))
        if {k:v for k,v in refreshed.items() if k != 'index'} != {k:v for k,v in before.items() if k != 'index'}:
            raise ValueError('refresh changed pack metadata or versions')
        if refreshed['index']['file'] != before['index']['file']:
            raise ValueError('refresh changed index location')
        text = index_file.read_text(encoding='utf-8')
        blocks = re.split(r'(?m)^\[\[files\]\][ \t]*\n', text)
        if len(blocks) < 2:
            raise ValueError('refresh did not generate a valid file index')
        retired_keys = {path.casefold() for path in retired}
        kept = []
        for block in blocks[1:]:
            entry = tomllib.loads('[[files]]\n' + block)['files'][0]
            if entry.get('file', '').casefold() not in retired_keys:
                kept.append('[[files]]\n' + block.rstrip() + '\n')
        canonical = []
        for path in sorted(retired):
            entry = verifier.compatibility_entry(path)
            canonical.append('[[files]]\n' + ''.join(f'{key} = {json.dumps(value, ensure_ascii=False)}\n' for key, value in entry.items()))
        index_bytes = (blocks[0].rstrip() + '\n\n' + '\n'.join(kept + canonical)).encode('utf-8')
        fmt = refreshed['index']['hash-format']
        if fmt not in verifier.HASH_FORMATS:
            raise ValueError('unsupported index hash format')
        digest = hashlib.new(fmt, index_bytes).hexdigest()
        pack_text = (root / 'pack.toml').read_text(encoding='utf-8')
        section = re.search(r'(?ms)^\[index\][ \t]*\n(.*?)(?=^\[|\Z)', pack_text)
        if not section:
            raise ValueError('missing index table after refresh')
        body, count = re.subn(r'(?m)^([ \t]*hash[ \t]*=[ \t]*)["\'][^"\'\n]*["\']', lambda match: match[1] + '"' + digest + '"', section[1])
        if count != 1:
            raise ValueError('cannot update the index hash safely')
        pack_text = pack_text[:section.start(1)] + body + pack_text[section.end(1):]
        atomic_write(index_file, index_bytes, index_mode)
        atomic_write(root / 'pack.toml', pack_text.encode('utf-8'), pack_mode)
        errors, count = verifier.verify(root)
        if errors:
            raise ValueError('verification failed after guarded refresh: ' + '; '.join(errors))
        print(f'OK: guarded refresh verified {count} entries, including {len(retired)} permanent compatibility URIs.')
    except BaseException:
        atomic_write(index_file, before_index, index_mode)
        atomic_write(root / 'pack.toml', before_pack, pack_mode)
        raise


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('pack_dir', nargs='?', type=Path, default=HERE.parent)
    parser.add_argument('--packwiz', default=os.environ.get('PACKWIZ_BIN', 'packwiz'))
    args = parser.parse_args()
    try:
        refresh(args.pack_dir.resolve(), args.packwiz)
    except (OSError, ValueError, subprocess.CalledProcessError):
        print('FAIL: guarded refresh could not verify the result; original pack/index were restored when modified.', file=sys.stderr)
        return 1
    return 0

if __name__ == '__main__':
    sys.exit(main())
