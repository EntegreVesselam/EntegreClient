#!/usr/bin/env python3
"""Build an offline, reproducible Prism starter from verified payloads; Python 3.11+.

Usage: python3 scripts/build-prism-release.py --self-check
       python3 scripts/build-prism-release.py --output-dir /tmp/entegre-client-release
The public v2.0 starter supplies the command/schema, never personal settings:
https://github.com/EntegreVesselam/EntegreClient/releases/download/v2.0/EntegreClient-v2.0.zip
"""
import argparse
import hashlib
import io
import json
from pathlib import Path
import re
import stat
import struct
import tomllib
import zipfile


ROOT = Path(__file__).resolve().parent.parent
STAMP = (1980, 1, 1, 0, 0, 0)
MODE = (stat.S_IFREG | 0o644) << 16
COMMAND = ('"$INST_JAVA" -jar "$INST_DIR/minecraft/packwiz-installer-bootstrap.jar" '
           'https://raw.githubusercontent.com/EntegreVesselam/EntegreClient/minecraft-26.2/pack.toml')
PAYLOADS = tuple(f"entegre-updater/{stem}.{extension}"
                 for stem in ("engine", "bootstrap") for extension in ("jar", "sha256"))
ALLOWED = {"instance.cfg", "mmc-pack.json", "icon.png", "minecraft/packwiz-installer-bootstrap.jar",
           *("minecraft/" + path for path in PAYLOADS)}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(payload):
    return hashlib.sha256(payload).hexdigest()


def read_source(relative, limit):
    path = ROOT / relative
    require(path.is_file() and not path.is_symlink(), f"Missing or linked source: {relative}")
    require(0 < path.stat().st_size <= limit, f"Unexpected source size: {relative}")
    return path.read_bytes()


def starter_files():
    pack = tomllib.loads(read_source("pack.toml", 65536).decode("utf-8"))
    version = pack["version"]
    require(re.fullmatch(r"[0-9]+(?:\.[0-9]+){2}", version), "Expected a numeric release version")
    versions = pack["versions"]
    # Reviewed against https://meta.prismlauncher.org/v1/net.minecraft/26.2.json (2026-10-18).
    require(versions["minecraft"] == "26.2", "Review LWJGL metadata before changing Minecraft")
    require(re.fullmatch(r"[0-9]+(?:\.[0-9]+){2}", versions["fabric"]), "Invalid Fabric version")
    index_bytes = read_source("index.toml", 1024 * 1024)
    require(pack["index"]["file"] == "index.toml" and pack["index"]["hash-format"] == "sha256"
            and pack["index"]["hash"] == digest(index_bytes), "Pack/index checksum mismatch")
    index = tomllib.loads(index_bytes.decode("utf-8"))
    entries = {entry["file"]: entry for entry in index["files"]}
    require(len(entries) == len(index["files"]), "Duplicate index entry")
    files = {}
    for relative in PAYLOADS:
        payload = read_source(relative, 16 * 1024 * 1024)
        entry = entries[relative]
        require(not entry.get("metafile") and not entry.get("preserve") and not entry.get("alias"),
                f"Payload must be directly managed: {relative}")
        require(entry.get("hash-format", index["hash-format"]) == "sha256"
                and entry["hash"] == digest(payload), f"Indexed payload checksum mismatch: {relative}")
        files["minecraft/" + relative] = payload
    for stem, main in (("engine", "com.entegre.launcher.Launcher"),
                       ("bootstrap", "com.entegre.bootstrap.Bootstrap")):
        prefix = "minecraft/entegre-updater/" + stem
        payload, checksum = files[prefix + ".jar"], files[prefix + ".sha256"]
        require(re.fullmatch(rb"[0-9a-f]{64}\n?", checksum) is not None
                and checksum.decode().strip() == digest(payload), f"Runtime checksum mismatch: {stem}")
        with zipfile.ZipFile(io.BytesIO(payload)) as jar:
            require(jar.testzip() is None, f"Corrupt executable JAR: {stem}")
            require(len(jar.namelist()) == len(set(jar.namelist())), f"Duplicate JAR entry: {stem}")
            require("Main-Class: " + main in jar.read("META-INF/MANIFEST.MF").decode().splitlines(),
                    f"Unexpected JAR entrypoint: {stem}")
            header = jar.read(main.replace(".", "/") + ".class")[:8]
            require(header[:4] == b"\xca\xfe\xba\xbe" and struct.unpack(">H", header[6:8])[0] == 61,
                    f"Expected compiled Java 17 entrypoint: {stem}")
    files["minecraft/packwiz-installer-bootstrap.jar"] = files["minecraft/entegre-updater/bootstrap.jar"]
    icon = read_source("icon.png", 2 * 1024 * 1024)
    require(icon.startswith(b"\x89PNG\r\n\x1a\n"), "Expected a PNG instance icon")
    files["icon.png"] = icon
    command = COMMAND.replace('"', '\\"')
    files["instance.cfg"] = ("[General]\nInstanceType=OneSix\nOverrideCommands=true\n"
                             f"PreLaunchCommand={command}\niconKey=icon\nname=Entegre Client 26.2\n").encode()
    profile = {"formatVersion": 1, "components": [
        {"uid": "org.lwjgl3", "version": "3.4.1", "dependencyOnly": True},
        {"uid": "net.minecraft", "version": versions["minecraft"], "important": True},
        {"uid": "net.fabricmc.fabric-loader", "version": versions["fabric"]},
    ]}
    files["mmc-pack.json"] = (json.dumps(profile, indent=2, sort_keys=True) + "\n").encode()
    require(set(files) == ALLOWED, "Unexpected starter file set")
    return version, versions["minecraft"], files


def archive_bytes(files):
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for name, payload in sorted(files.items()):
            info = zipfile.ZipInfo(name, STAMP)
            info.create_system = 3
            info.external_attr = MODE
            info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, payload, compresslevel=9)
    return buffer.getvalue()


def verify_archive(payload, expected):
    require(len(payload) <= 32 * 1024 * 1024, "Starter archive exceeds the size limit")
    with zipfile.ZipFile(io.BytesIO(payload)) as archive:
        names = archive.namelist()
        require(set(names) == ALLOWED and len(names) == len(ALLOWED), "Unexpected or duplicate starter entry")
        require(names == sorted(names), "Non-deterministic entry order")
        require(archive.testzip() is None, "Starter ZIP checksum failure")
        for entry in archive.infolist():
            require(entry.date_time == STAMP and entry.create_system == 3 and entry.external_attr == MODE,
                    f"Unexpected archive metadata: {entry.filename}")
            require(entry.compress_type == zipfile.ZIP_DEFLATED, "Unexpected ZIP compression")
            require(entry.file_size == len(expected[entry.filename])
                    and archive.read(entry) == expected[entry.filename], f"Changed starter file: {entry.filename}")


def self_check(files, payload):
    require(archive_bytes(files) == payload, "Starter build is not reproducible")
    cases = []
    extra = dict(files)
    extra["minecraft/packwiz.json"] = b"{}"
    cases.append(extra)
    changed = dict(files)
    changed["minecraft/packwiz-installer-bootstrap.jar"] = b"wrong executable"
    cases.append(changed)
    missing = dict(files)
    del missing["minecraft/entegre-updater/engine.sha256"]
    cases.append(missing)
    for case in cases:
        try:
            verify_archive(archive_bytes(case), files)
        except ValueError:
            continue
        raise ValueError("Negative archive validation unexpectedly passed")
    print("PASS reproducibility; rejects installed state, changed executable and missing checksum")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", type=Path, help="Explicit artifact directory; never defaults to the repository")
    parser.add_argument("--self-check", action="store_true", help="Run offline validation and negative cases; no writes without --output-dir")
    args = parser.parse_args()
    if not args.output_dir and not args.self_check:
        parser.error("Specify --output-dir or --self-check")
    version, minecraft_version, files = starter_files()
    payload = archive_bytes(files)
    verify_archive(payload, files)
    if args.self_check:
        self_check(files, payload)
    if args.output_dir:
        directory = args.output_dir.resolve()
        require(not directory.is_relative_to(ROOT), "Release artifacts must remain outside the repository")
        directory.mkdir(parents=True, exist_ok=True)
        destination = directory / f"EntegreClient-v{version}-mc{minecraft_version}.zip"
        destination.write_bytes(payload)
        verify_archive(destination.read_bytes(), files)
        destination.with_suffix(".zip.sha256").write_text(digest(payload) + "  " + destination.name + "\n")
        print(f"Built {destination}")
    print(f"PASS {len(files)} allowlisted files; verified indexed JARs, runtime hashes and Java 17 entrypoints")
    print(f"SHA256 {digest(payload)}; {len(payload)} bytes")


if __name__ == "__main__":
    main()
