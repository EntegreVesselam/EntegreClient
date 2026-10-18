#!/usr/bin/env python3
"""Offline release wiring check; never launches Java or changes a player instance."""
import hashlib
import json
from pathlib import Path
import re
import tomllib
import zipfile


ROOT = Path(__file__).resolve().parent.parent


def main():
    index = tomllib.loads((ROOT / "index.toml").read_text())
    entries = {item["file"]: item for item in index["files"]}
    for path in entries:
        assert not path.startswith("launcher/"), "Updater source leaked into player distribution"
        assert "bootstrap-backup" not in path and ".entegre-updater-disabled" not in path, "Rollback state must remain local"
    for stem, main_class in [("engine", "com.entegre.launcher.Launcher"), ("bootstrap", "com.entegre.bootstrap.Bootstrap")]:
        name = f"entegre-updater/{stem}"
        for extension in ("jar", "sha256"):
            entry = entries[f"{name}.{extension}"]
            assert entry.get("metafile", False) is False and not entry.get("preserve"), "Payload must be directly managed"
        payload = (ROOT / f"{name}.jar").read_bytes()
        digest = (ROOT / f"{name}.sha256").read_text()
        assert re.fullmatch(r"[0-9a-f]{64}\n?", digest), "Runtime checksum must be plain SHA256"
        assert digest.strip() == hashlib.sha256(payload).hexdigest(), "Runtime payload checksum mismatch"
        with zipfile.ZipFile(ROOT / f"{name}.jar") as archive:
            assert archive.testzip() is None
            assert f"Main-Class: {main_class}" in archive.read("META-INF/MANIFEST.MF").decode().splitlines()
    mod = "mods/entegre-updater-transition-0.1.0.jar"
    assert entries[mod].get("metafile", False) is False and not entries[mod].get("preserve"), "Migration mod must be mandatory"
    with zipfile.ZipFile(ROOT / mod) as archive:
        metadata = json.loads(archive.read("fabric.mod.json"))
        assert metadata["id"] == "entegre_updater_transition" and metadata["environment"] == "client"
        assert metadata["entrypoints"]["client"] == ["com.entegre.transition.TransitionClient"]
        assert "com/entegre/transition/TransitionClient.class" in archive.namelist()
        assert "com/entegre/bootstrap/Payloads.class" in archive.namelist()
        assert archive.testzip() is None
    original = ROOT / "packwiz-installer-bootstrap.jar"
    assert hashlib.sha256(original.read_bytes()).hexdigest() == "a8fbb24dc604278e97f4688e82d3d91a318b98efc08d5dbfcbcbcab6443d116c", "Repository bootstrap must remain the known original"
    print("PASS updater release: five managed assets, matching SHA256, executable JARs, mandatory client entrypoint, excluded sources/rollback state")


if __name__ == "__main__":
    main()
