#!/usr/bin/env python3
"""Offline packwiz integrity checks. Requires Python 3.11+; never downloads files."""

import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import sys
import tomllib
from urllib.parse import urlsplit


HASH_FORMATS = {"md5", "sha1", "sha256", "sha384", "sha512"}
EXCLUDED_DIRS = {".git", ".claude", "scripts", "tests", "docs", "__pycache__", "compatibility", "launcher"}
EXCLUDED_NAMES = {"readme.md", "changelog.md", ".ds_store", "servers.dat_old"}


EMPTY_HASH = hashlib.sha256(b"").hexdigest()
# The registry is permanent safety state, not an editable allowlist. Changes require
# a reviewed baseline update; never remove previously retired URIs from later packs.
RETIRED_SOURCE_COMMIT = "fb13701f1242c635023f295d487d98f723347e3a"
RETIRED_PATH_COUNT = 186
# SHA256 of UTF-8 JSON: sorted exact URI strings, ensure_ascii=False, separators=(",", ":").
RETIRED_PATHS_SHA256 = "3b4217d64a0e850b04ce81e004ff52ccfef2f4f7846b35a0cb1798f92d0cc07a"


def lexical_path(value):
    if not isinstance(value, str) or not value or "\\" in value or ":" in value:
        raise ValueError("invalid relative path")
    if any(part in {"", ".", ".."} or any(ord(c) < 32 or ord(c) == 127 for c in part) for part in value.split("/")):
        raise ValueError("invalid relative path")
    if re.search(r"%(?:2e|2f|5c)", value, re.IGNORECASE):
        raise ValueError("encoded path separator or traversal")
    return PurePosixPath(value)


def load_retired_paths(root):
    try:
        registry = json.loads((root / "compatibility/retired-index-paths.json").read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        raise ValueError("missing or invalid compatibility registry") from error
    if (not isinstance(registry, dict) or set(registry) != {"schemaVersion", "sourceCommit", "paths"}
            or type(registry["schemaVersion"]) is not int or registry["schemaVersion"] != 1
            or not isinstance(registry["sourceCommit"], str) or not re.fullmatch(r"[0-9a-f]{40}", registry["sourceCommit"])
            or not isinstance(registry["paths"], list)):
        raise ValueError("invalid compatibility registry schema")
    paths = registry["paths"]
    for value in paths:
        lexical_path(value)
    if len({value.casefold() for value in paths}) != len(paths):
        raise ValueError("duplicate or case-colliding compatibility URI")
    digest = hashlib.sha256(json.dumps(sorted(paths), ensure_ascii=False, separators=(",", ":")).encode("utf-8")).hexdigest()
    if (registry["sourceCommit"] != RETIRED_SOURCE_COMMIT or len(paths) != RETIRED_PATH_COUNT
            or digest != RETIRED_PATHS_SHA256):
        raise ValueError("compatibility registry differs from the pinned historical baseline")
    return set(paths)


def compatibility_entry(path):
    return {"file": path, "alias": ".", "metafile": False, "preserve": True, "hash-format": "sha256", "hash": EMPTY_HASH}


def verify(root):
    errors = []
    destinations = {}
    try:
        retired = load_retired_paths(root)
    except ValueError as error:
        return [f"FAIL compatibility registry: {error}"], 0
    retired_keys = {path.casefold(): path for path in retired}
    retired_seen = set()

    def fail(label, message):
        errors.append(f"FAIL {label}: {message}")

    def relative_path(value, label):
        try:
            path = lexical_path(value)
        except ValueError as error:
            fail(label, str(error))
            return None
        if not (root / path).resolve().is_relative_to(root):
            fail(label, "path escapes pack directory")
            return None
        return path

    def excluded(path):
        parts = [part.casefold() for part in path.parts]
        return (
            bool(set(parts) & EXCLUDED_DIRS)
            or parts[:2] == ["mods", ".index"]
            or path.name.casefold() in EXCLUDED_NAMES
            or parts[0] in {"xaero", "xaeroworldmap", "xaerominimap"}
        )

    def read_toml(path, label):
        try:
            with path.open("rb") as stream:
                return tomllib.load(stream)
        except (OSError, ValueError):
            fail(label, "cannot read valid TOML")
            return None

    def check_hash(path, info, label, default_format=None):
        fmt = info.get("hash-format", default_format)
        expected = info.get("hash")
        if not isinstance(fmt, str) or fmt not in HASH_FORMATS:
            fail(label, "unsupported hash-format")
            return
        length = hashlib.new(fmt).digest_size * 2
        if not isinstance(expected, str) or re.fullmatch(r"[0-9a-fA-F]{%d}" % length, expected) is None:
            fail(label, "invalid hash value")
            return
        if path is not None:
            try:
                with path.open("rb") as stream:
                    actual = hashlib.file_digest(stream, fmt).hexdigest()
            except OSError:
                fail(label, "file missing or unreadable")
                return
            if actual != expected.lower():
                fail(label, "hash mismatch")

    def reserve(path, label):
        key = str(path).casefold()
        if key in retired_keys:
            fail(label, "download destination overlaps a retired personal path")
        if key in destinations:
            fail(label, f"duplicate destination (also {destinations[key]})")
        else:
            destinations[key] = label

    pack = read_toml(root / "pack.toml", "pack.toml")
    if pack is None:
        return errors, 0
    index_info = pack.get("index")
    if not isinstance(index_info, dict):
        fail("pack.toml", "missing index table")
        return errors, 0
    index_path = relative_path(index_info.get("file"), "pack.toml index")
    if index_path is None:
        return errors, 0
    check_hash(root / index_path, index_info, str(index_path))
    index = read_toml(root / index_path, str(index_path))
    if index is None:
        return errors, 0
    files = index.get("files")
    if not isinstance(files, list) or not files:
        fail(str(index_path), "missing or empty files list")
        return errors, 0
    indexed_paths = set()
    for entry in files:
        if not isinstance(entry, dict):
            fail(str(index_path), "invalid file entry")
            continue
        value = entry.get("file")
        if isinstance(value, str) and value.casefold() in retired_keys:
            canonical = retired_keys[value.casefold()]
            key = value.casefold()
            if key in indexed_paths:
                fail("compatibility entry", "duplicate index URI")
            indexed_paths.add(key)
            if (entry != compatibility_entry(canonical) or type(entry.get("metafile")) is not bool
                    or type(entry.get("preserve")) is not bool):
                fail("compatibility entry", "retired URI must have the exact immutable directory-alias shape")
            else:
                retired_seen.add(canonical)
            # Never resolve/read historical sources or reserve their root directory alias.
            continue
        if "alias" in entry:
            fail("index entry", "aliases are reserved for exact registered compatibility entries")
            continue
        path = relative_path(value, "index entry")
        if path is None:
            continue
        label = str(path)
        key = label.casefold()
        if key in indexed_paths:
            fail(label, "duplicate index entry")
        indexed_paths.add(key)
        if excluded(path):
            fail(label, "excluded development file or player data is indexed")
            # Do not read player data or development files, even for hashing.
            continue
        check_hash(root / path, entry, label, index.get("hash-format"))
        if not entry.get("metafile", False):
            reserve(path, label)
            continue
        metadata = read_toml(root / path, label)
        if metadata is None:
            continue
        filename = relative_path(metadata.get("filename"), label + " filename")
        if filename is not None:
            if len(filename.parts) != 1:
                fail(label, "metadata filename must be a basename")
            else:
                destination = path.parent / filename
                if excluded(destination):
                    fail(label, "download destination is excluded")
                reserve(destination, label)
        download = metadata.get("download")
        if not isinstance(download, dict):
            fail(label, "missing download table")
            continue
        check_hash(None, download, label + " download")
        url = download.get("url")
        try:
            parsed = urlsplit(url) if isinstance(url, str) else None
            valid_url = (
                parsed is not None
                and parsed.scheme in {"https", "http"}
                and bool(parsed.hostname)
                and parsed.username is None
                and parsed.password is None
                and not parsed.fragment
                and not any(c.isspace() or ord(c) < 32 for c in url)
            )
            if parsed is not None:
                parsed.port  # Reject malformed port numbers without printing URLs.
        except ValueError:
            valid_url = False
        if not valid_url:
            fail(label, "invalid HTTP(S) download URL")
    if retired_seen != retired:
        fail("compatibility registry", f"{len(retired - retired_seen)} retained URI(s) missing or altered; direct packwiz refresh is unsafe")
    return errors, len(files)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pack_dir", nargs="?", type=Path, default=Path(__file__).resolve().parent.parent)
    args = parser.parse_args()
    errors, count = verify(args.pack_dir.resolve())
    for error in errors:
        print(error)
    if errors:
        print(f"FAILED: {len(errors)} error(s), {count} indexed files checked.")
        return 1
    print(f"OK: pack/index hashes, {count} indexed files, download metadata and destinations verified.")
    print("Compatibility rows retain URI ownership permanently; never delete them or run unguarded refresh.")
    print("Offline checks only: remote availability and Minecraft compatibility are not tested.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
