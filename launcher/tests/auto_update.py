#!/usr/bin/env python3
"""Actual old bootstrap -> transition core -> new GUI -> engine self-update.

Only temporary synthetic instances and loopback HTTP are used. Requires a desktop.
The Fabric adapter itself is compiled/inspected, not loaded by a real Minecraft JVM.
"""
import hashlib
import io
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import time
import zipfile

from integration import Fixture, PROJECT


def sha(data):
    return hashlib.sha256(data).hexdigest()


def main():
    java = os.environ.get("JAVA_BIN") or (
        str(Path(os.environ["JAVA_HOME"]) / "bin/java") if os.environ.get("JAVA_HOME") else shutil.which("java"))
    assert java, "Java required"
    engine = (PROJECT / "dist/entegre-updater.jar").read_bytes()
    shim = (PROJECT / "dist/entegre-bootstrap.jar").read_bytes()
    transition = PROJECT / "dist/entegre-updater-transition-0.1.0.jar"
    original = (PROJECT / "vendor/packwiz-installer-bootstrap.jar").read_bytes()
    fixture = Fixture()
    try:
        with tempfile.TemporaryDirectory(prefix="entegre-auto-update-") as temporary:
            parent = Path(temporary)
            game = parent / "minecraft"
            game.mkdir()
            target = game / "packwiz-installer-bootstrap.jar"
            target.write_bytes(original)
            shutil.copyfile(PROJECT / "vendor/packwiz-installer-0.5.14.jar", game / "packwiz-installer.jar")
            # Exact public EntegreClient-v2.0.zip instance.cfg value, including QSettings escapes.
            command = ('"$INST_JAVA" -jar "$INST_DIR/minecraft/packwiz-installer-bootstrap.jar" '
                       'https://github.com/EntegreVesselam/EntegreClient/raw/main/pack.toml').replace('"', '\\"')
            config = '[General]\nOverrideCommands=true\nPreLaunchCommand=' + command + '\n'
            (parent / "instance.cfg").write_text(config)
            personal = b"SYNTHETIC PERSONAL SETTINGS\x00\xff"
            (game / "options.txt").write_bytes(personal)
            url = fixture.base + "/pack/pack.toml"
            count = 0

            def run(arguments, expected=0):
                nonlocal count
                count += 1
                start = time.monotonic()
                result = subprocess.run([java, *arguments], cwd=game, stdout=subprocess.PIPE,
                                        stderr=subprocess.STDOUT, text=True, timeout=35)
                assert result.returncode == expected, result.stdout[-8000:]
                return time.monotonic() - start, result.stdout

            def publish(version, payload):
                assets = {
                    "entegre-updater/engine.jar": payload,
                    "entegre-updater/engine.sha256": (sha(payload) + "\n").encode(),
                    "entegre-updater/bootstrap.jar": shim,
                    "entegre-updater/bootstrap.sha256": (sha(shim) + "\n").encode(),
                    "mods/entegre-updater-transition-0.1.0.jar": transition.read_bytes(),
                    "config/personal.txt": ("synthetic defaults " + version).encode(),
                }
                index = 'hash-format = "sha256"\n'
                for name, data in assets.items():
                    fixture.files["/pack/" + name] = data
                    index += f'\n[[files]]\nfile = "{name}"\nhash = "{sha(data)}"\nmetafile = false\n'
                    if name == "config/personal.txt":
                        index += 'preserve = true\n'
                # Existing permanent bootstrap ownership row must remain exactly this shape.
                index += ('\n[[files]]\nfile = "packwiz-installer-bootstrap.jar"\nalias = "."\n'
                          'metafile = false\npreserve = true\nhash-format = "sha256"\n'
                          f'hash = "{sha(b"")}"\n')
                fixture.files["/pack/index.toml"] = index.encode()
                fixture.files["/pack/pack.toml"] = (
                    'name = "Synthetic automatic Entegre update"\npack-format = "packwiz:1.1.0"\n'
                    f'version = "{version}"\n[index]\nfile = "index.toml"\nhash-format = "sha256"\n'
                    f'hash = "{sha(index.encode())}"\n').encode()

            publish("1", engine)
            # The genuine old bootstrap uses a pinned local installer cache; no internet request.
            # No optional mods exist in this fixture, so headless does not change any preference.
            run(["-Djava.awt.headless=true", "-jar", str(target), "--bootstrap-no-update", "-g", url])
            assert target.read_bytes() == original
            assert (game / "entegre-updater/engine.jar").read_bytes() == engine
            assert (game / "mods" / transition.name).read_bytes() == transition.read_bytes()
            original_ownership = json.loads((game / "packwiz.json").read_text())["cachedFiles"]["packwiz-installer-bootstrap.jar"]
            (game / "config/personal.txt").write_bytes(personal)
            print("PASS actual old bootstrap downloads all transition assets; original remains untouched", flush=True)

            classpath = os.pathsep.join([str(PROJECT / "build/test-classes"), str(transition)])
            _, output = run(["-cp", classpath, "TransitionApply", str(game)])
            assert "TRANSITION INSTALLED" in output, output
            assert target.read_bytes() == shim
            assert (game / "entegre-updater/bootstrap-backup.jar").read_bytes() == original
            assert (parent / "instance.cfg").read_text() == config
            elapsed, _ = run(["-jar", str(target), url])
            assert elapsed < 8, f"Unchanged fixture unexpectedly took {elapsed:.2f}s"
            print(f"PASS migrated same bootstrap filename opens actual Turkish GUI and completes in {elapsed:.2f}s", flush=True)

            # A real second engine artifact; only a synthetic resource changes, keeping production code.
            buffer = io.BytesIO(engine)
            with zipfile.ZipFile(buffer, "a", zipfile.ZIP_DEFLATED) as archive:
                archive.writestr("synthetic-engine-revision.txt", "revision 2")
            next_engine = buffer.getvalue()
            publish("2", next_engine)
            run(["-jar", str(target), url])
            assert (game / "entegre-updater/engine.jar").read_bytes() == next_engine
            assert (game / "entegre-updater/engine.sha256").read_text().strip() == sha(next_engine)
            assert target.read_bytes() == shim
            run(["-jar", str(target), url])
            _, output = run(["-cp", classpath, "TransitionApply", str(game)])
            assert "TRANSITION ALREADY_INSTALLED" in output, output
            assert (game / "options.txt").read_bytes() == personal
            assert (game / "config/personal.txt").read_bytes() == personal
            assert (parent / "instance.cfg").read_text() == config
            state = json.loads((game / "packwiz.json").read_text())
            assert state["cachedFiles"]["packwiz-installer-bootstrap.jar"] == original_ownership
            print("PASS actual engine updates its managed payload, next launch succeeds; tombstone/settings/backup preserved", flush=True)

            # Manual rollback: suppress re-migration, restore the verified backup, use old engine.
            (game / ".entegre-updater-disabled").touch()
            shutil.copyfile(game / "entegre-updater/bootstrap-backup.jar", target)
            _, output = run(["-cp", classpath, "TransitionApply", str(game)])
            assert "TRANSITION SKIPPED" in output, output
            run(["-Djava.awt.headless=true", "-jar", str(target), "--bootstrap-no-update", "-g", url])
            assert target.read_bytes() == original
            assert (game / "config/personal.txt").read_bytes() == personal
            print(f"PASS verified rollback remains disabled and original bootstrap works; {count} real Java processes", flush=True)
    finally:
        fixture.close()


if __name__ == "__main__":
    main()
