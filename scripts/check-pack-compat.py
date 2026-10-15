#!/usr/bin/env python3
"""Synthetic offline tests for immutable compatibility rows and guarded refresh."""
import hashlib
import importlib.util
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

sys.dont_write_bytecode = True
HERE = Path(__file__).resolve().parent
EMPTY_HASH = hashlib.sha256(b'').hexdigest()

def main():
    assert (HERE / 'verify-pack.py').exists(), 'Compatibility verifier must exist before safely refreshing an old managed pack'
    spec = importlib.util.spec_from_file_location('pack_verifier', HERE / 'verify-pack.py')
    verifier = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(verifier)
    baseline_names = ['RETIRED_SOURCE_COMMIT', 'RETIRED_PATH_COUNT', 'RETIRED_PATHS_SHA256']
    production_baseline = [getattr(verifier, name, None) for name in baseline_names]
    def pin_test_catalog(catalog):
        verifier.RETIRED_SOURCE_COMMIT = catalog['sourceCommit']
        verifier.RETIRED_PATH_COUNT = len(catalog['paths'])
        verifier.RETIRED_PATHS_SHA256 = hashlib.sha256(json.dumps(sorted(catalog['paths']), ensure_ascii=False, separators=(',', ':')).encode('utf-8')).hexdigest()
    with tempfile.TemporaryDirectory(prefix='ev-pack-compat-') as temporary:
        root = (Path(temporary) / 'pack').resolve()
        root.mkdir()
        (root / 'compatibility').mkdir()
        (root / 'normal.txt').write_bytes(b'Synthetic distributable configuration')
        registry = {'schemaVersion': 1, 'sourceCommit': 'f' * 40, 'paths': ['xaero/retired.bin']}
        pin_test_catalog(registry)
        canonical = {'file': registry['paths'][0], 'alias': '.', 'metafile': False, 'preserve': True, 'hash-format': 'sha256', 'hash': EMPTY_HASH}
        normal = {'file': 'normal.txt', 'hash': hashlib.sha256((root / 'normal.txt').read_bytes()).hexdigest(), 'metafile': False}
        def write(entries, catalog=None):
            (root / 'compatibility/retired-index-paths.json').write_text(json.dumps(registry if catalog is None else catalog))
            text = 'hash-format = "sha256"\n'
            for entry in entries:
                text += '\n[[files]]\n' + ''.join(f'{key} = {json.dumps(value)}\n' for key, value in entry.items())
            (root / 'index.toml').write_text(text)
            (root / 'pack.toml').write_text('name = "Synthetic"\nversion = "test"\npack-format = "packwiz:1.1.0"\n[index]\nfile = "index.toml"\nhash-format = "sha256"\nhash = "' + hashlib.sha256(text.encode()).hexdigest() + '"\n[versions]\nminecraft = "1.21.11"\nfabric = "0.19.5"\n')
        write([normal, canonical])
        outside = Path(temporary) / 'outside'
        outside.mkdir()
        (outside / 'retired.bin').write_bytes(b'SYNTHETIC DO NOT READ OR RESOLVE')
        (root / 'xaero').symlink_to(outside, target_is_directory=True)
        assert verifier.verify(root)[0] == [], 'Retired rows must not resolve or read the legacy filesystem target'
        write([normal], dict(registry, paths=[]))
        assert verifier.verify(root)[0], 'Coordinated registry AND index deletion must not bypass the immutable baseline'
        write([normal, canonical], dict(registry, sourceCommit='a' * 40))
        assert verifier.verify(root)[0], 'A different syntactically valid source commit must fail'
        write([normal, dict(canonical, file='xaero/replacement.bin')], dict(registry, paths=['xaero/replacement.bin']))
        assert verifier.verify(root)[0], 'Replacing registry/index together at the same count must fail the pinned path digest'
        print('PASS coordinated registry/index deletion, same-count replacement and changed source-commit rejection')
        for entry in [dict(canonical, alias='elsewhere'), dict(canonical, preserve=False), dict(canonical, metafile=True), dict(canonical, hash='0' * 64), dict(canonical, extra=True), dict(canonical, preserve=1), dict(canonical, file='XAERO/retired.bin')]:
            write([normal, entry])
            assert verifier.verify(root)[0], 'Any changed compatibility shape must fail'
        write([normal]); assert verifier.verify(root)[0], 'Direct packwiz refresh dropping a retained URI must fail verification'
        write([normal, canonical, canonical]); assert verifier.verify(root)[0], 'Duplicate compatibility URIs must fail'
        write([normal, canonical, dict(normal, file='unlisted.txt', alias='.', preserve=True)]); assert verifier.verify(root)[0], 'Unlisted aliases cannot obtain the compatibility exception'
        write([normal, canonical, dict(normal, file='xaero/unlisted.bin')]); assert verifier.verify(root)[0], 'Other player data stays excluded'
        write([normal, canonical], dict(registry, paths=registry['paths'] + ['XAERO/retired.bin'])); assert verifier.verify(root)[0], 'Case-colliding registry URIs fail'
        write([normal, canonical], dict(registry, paths=['../outside/retired.bin'])); assert verifier.verify(root)[0], 'Registry traversal fails'
        write([normal, canonical], dict(registry, paths=['xaero/%2e%2e/retired.bin'])); assert verifier.verify(root)[0], 'Encoded traversal fails'
        write([normal, canonical], dict(registry, sourceCommit='not-a-commit')); assert verifier.verify(root)[0]
        print('PASS exact retired rows, no legacy read/resolve, missing/duplicate/case/shape/alias/traversal rejection and general exclusions')

        # A metadata destination must not target a retired URI, even outside excluded prefixes.
        (root / 'config').mkdir()
        metadata = 'filename = "retired.cfg"\n[download]\nurl = "https://example.test/fixture"\nhash-format = "sha256"\nhash = "' + EMPTY_HASH + '"\n'
        (root / 'config/test.pw.toml').write_text(metadata)
        collision = dict(canonical, file='config/retired.cfg')
        expanded_registry = dict(registry, paths=registry['paths'] + ['config/retired.cfg'])
        pin_test_catalog(expanded_registry)
        write([normal, canonical, collision], expanded_registry)
        assert verifier.verify(root)[0] == [], 'Collision test starts from a valid expanded synthetic baseline'
        write([normal, canonical, collision, {'file': 'config/test.pw.toml', 'metafile': True, 'hash': hashlib.sha256(metadata.encode()).hexdigest()}], dict(registry, paths=registry['paths'] + ['config/retired.cfg']))
        assert verifier.verify(root)[0], 'Active download destinations cannot overwrite a retired personal URI'
        print('PASS active download destination collision with retired personal path is rejected')

        if shutil.which('packwiz'):
            (root / 'xaero').unlink()
            (root / 'config/test.pw.toml').unlink()
            for name, value in zip(baseline_names, production_baseline):
                setattr(verifier, name, value)
            release_registry = json.loads((HERE.parent / 'compatibility/retired-index-paths.json').read_text(encoding='utf-8'))
            write([normal], release_registry)
            (root / '.packwizignore').write_text('compatibility/\n.packwizignore\nconfig/\n')
            before = __import__('tomllib').loads((root / 'pack.toml').read_text())
            for _ in range(2):
                result = subprocess.run([sys.executable, str(HERE / 'refresh-pack.py'), str(root)], capture_output=True, text=True)
                assert result.returncode == 0, result.stdout + result.stderr
                assert verifier.verify(root)[0] == []
                after = __import__('tomllib').loads((root / 'pack.toml').read_text())
                assert {k:v for k,v in before.items() if k != 'index'} == {k:v for k,v in after.items() if k != 'index'}
            print('PASS real packwiz guarded refresh and repeat restore retained rows, update hashes and preserve versions/metadata')
            subprocess.run(['packwiz', 'refresh'], cwd=root, check=True, capture_output=True)
            assert verifier.verify(root)[0], 'Plain packwiz refresh must not silently pass after removing permanent rows'
            subprocess.run([sys.executable, str(HERE / 'refresh-pack.py'), str(root)], check=True, capture_output=True)
            preserved = [(path, path.read_bytes()) for path in [root / 'pack.toml', root / 'index.toml']]
            fake = Path(temporary) / 'fake-packwiz'
            fake.write_text('#!' + sys.executable + '\n' + 'from pathlib import Path\np=Path(\'pack.toml\')\np.write_text(p.read_text().replace(\'version = "test"\', \'version = "tampered"\'))\n')
            fake.chmod(0o700)
            failed = subprocess.run([sys.executable, str(HERE / 'refresh-pack.py'), str(root), '--packwiz', str(fake)], capture_output=True)
            assert failed.returncode != 0
            for path, content in preserved:
                assert path.read_bytes() == content, 'Failed refresh must restore original pack/index bytes'
            print('PASS unguarded-refresh damage detected, wrapper recovery and rejected metadata changes restore original bytes')

        else:
            raise RuntimeError('packwiz is required to verify the real guarded refresh path')

if __name__ == '__main__':
    main()
