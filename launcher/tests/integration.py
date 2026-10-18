#!/usr/bin/env python3
"""Real Swing + pinned Packwiz engine acceptance tests, using synthetic temp files.

Run `python3 build.py` first, then `python3 tests/integration.py`. Requires a
graphical desktop and Java 17+. Does not use or change a real Prism instance.
All downloads come from the loopback HTTP server; all install writes use a new
temporary directory. JAVA_BIN or JAVA_HOME may select the Java executable.
"""

import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


PROJECT = Path(__file__).resolve().parents[1]


def sha(data):
    return hashlib.sha256(data).hexdigest()


class Fixture:
    def __init__(self):
        self.files = {}
        self.requests = []
        self.delays = {}
        fixture = self

        class Handler(BaseHTTPRequestHandler):
            def do_GET(self):
                fixture.requests.append(self.path)
                payload = fixture.files.get(self.path)
                time.sleep(fixture.delays.get(self.path, 0))
                self.send_response(200 if payload is not None else 404)
                self.send_header("Content-Length", str(len(payload or b"")))
                self.end_headers()
                if payload is not None:
                    try:
                        self.wfile.write(payload)
                    except (BrokenPipeError, ConnectionResetError):
                        pass  # Expected when the cancellation test closes its connection.

            def log_message(self, *args):
                pass

        self.server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        self.base = f"http://127.0.0.1:{self.server.server_port}"
        threading.Thread(target=self.server.serve_forever, daemon=True).start()

    def pack(self, version, *, label="pack", corrupt_index=False, corrupt_download=False):
        managed = f"synthetic managed mod {version}\n".encode()
        defaults = f"synthetic stock settings {version}\n".encode()
        optional = b"synthetic optional mod\n"
        self.files[f"/{label}/mods/managed.jar"] = managed
        self.files[f"/{label}/config/personal.txt"] = defaults
        self.files["/binary/optional.jar"] = optional
        metadata = (
            'name = "Synthetic optional mod"\nfilename = "optional.jar"\nside = "client"\n'
            f'[download]\nurl = "{self.base}/binary/optional.jar"\nhash-format = "sha256"\n'
            f'hash = "{sha(optional)}"\n[option]\noptional = true\ndefault = false\n'
            'description = "A synthetic optional mod used only by the integration test."\n'
        ).encode()
        self.files[f"/{label}/mods/optional.pw.toml"] = metadata
        index = 'hash-format = "sha256"\n'
        for filename, payload, metafile, preserve in [
            ("mods/managed.jar", b"wrong hash" if corrupt_download else managed, False, False),
            ("mods/optional.pw.toml", metadata, True, False),
            ("config/personal.txt", defaults, False, True),
        ]:
            index += (f'\n[[files]]\nfile = "{filename}"\nhash = "{sha(payload)}"\n'
                      f'metafile = {str(metafile).lower()}\n')
            if preserve:
                index += 'preserve = true\n'
        index = index.encode()
        self.files[f"/{label}/index.toml"] = index
        index_hash = sha(b"wrong index") if corrupt_index else sha(index)
        self.files[f"/{label}/pack.toml"] = (
            'name = "Synthetic Entegre Integration"\npack-format = "packwiz:1.1.0"\n'
            f'version = "{version}"\n[index]\nfile = "index.toml"\n'
            f'hash-format = "sha256"\nhash = "{index_hash}"\n'
        ).encode()
        return managed

    def close(self):
        self.server.shutdown()
        self.server.server_close()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--keep", action="store_true", help="Keep synthetic fixtures and logs")
    args = parser.parse_args()
    jar = PROJECT / "dist/entegre-updater.jar"
    classes = PROJECT / "build/test-classes"
    if not jar.is_file() or not (classes / "IntegrationDriver.class").is_file():
        parser.error("Run python3 build.py to create the updater and test classes first")
    java = os.environ.get("JAVA_BIN") or (
        str(Path(os.environ["JAVA_HOME"]) / "bin/java") if os.environ.get("JAVA_HOME")
        else shutil.which("java")
    )
    if not java:
        parser.error("Java is required; configure JAVA_BIN or JAVA_HOME")
    root = Path(tempfile.mkdtemp(prefix="entegre-updater-integration-"))
    fixture = Fixture()
    run_count = 0

    def run(instance, mode="accept", *, settings=False, label="pack", expected=0):
        nonlocal run_count
        run_count += 1
        instance.mkdir(parents=True, exist_ok=True)
        command = [java, "-Djava.awt.headless=false", "-cp", os.pathsep.join([str(classes), str(jar)]),
                   "IntegrationDriver", mode]
        if settings:
            command.append("--settings")
        command.append(f"{fixture.base}/{label}/pack.toml")
        start = time.monotonic()
        result = subprocess.run(command, cwd=instance, stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT, timeout=25, text=True)
        elapsed = time.monotonic() - start
        (root / f"run-{run_count:02d}-{mode}.log").write_text(result.stdout)
        assert result.returncode == expected, (
            f"{mode}: expected exit {expected}, got {result.returncode}\n{result.stdout[-7000:]}"
        )
        return elapsed, result.stdout

    def optional_value(instance):
        manifest = json.loads((instance / "packwiz.json").read_text())
        return manifest["cachedFiles"]["mods/optional.pw.toml"]["optionValue"]

    try:
        instance = root / "synthetic-instance"
        (instance / "config").mkdir(parents=True)
        personal = b"SYNTHETIC PERSONAL SETTINGS\x00\xff"
        unmanaged = b"SYNTHETIC UNMANAGED SENTINEL"
        (instance / "config/unmanaged.txt").write_bytes(unmanaged)
        managed = fixture.pack("1")
        _, output = run(instance)
        assert "INTEGRATION OPTIONS initial=false" in output
        assert sha((instance / "mods/managed.jar").read_bytes()) == sha(managed)
        assert not (instance / "mods/optional.jar").exists()
        assert optional_value(instance) is False
        assert (instance / "config/personal.txt").read_bytes() == b"synthetic stock settings 1\n"
        assert "/binary/optional.jar" not in fixture.requests
        (instance / "config/personal.txt").write_bytes(personal)
        print("PASS first GUI install: managed/default files correct; default-off option remains off", flush=True)

        before = len(fixture.requests)
        elapsed, output = run(instance)
        assert elapsed < 8, f"Unchanged pack took {elapsed:.2f}s; expected no 10-second wait"
        assert "INTEGRATION OPTIONS" not in output
        assert optional_value(instance) is False
        assert (instance / "config/personal.txt").read_bytes() == personal
        assert fixture.requests[before:] == ["/pack/pack.toml"]
        print(f"PASS unchanged optional pack: {elapsed:.2f}s, no main Continue click, no optional dialog/download", flush=True)

        before = len(fixture.requests)
        managed = fixture.pack("2")
        elapsed, output = run(instance)
        assert elapsed < 8, f"Update took {elapsed:.2f}s; expected no optional countdown"
        assert sha((instance / "mods/managed.jar").read_bytes()) == sha(managed)
        assert (instance / "config/personal.txt").read_bytes() == personal
        assert (instance / "config/unmanaged.txt").read_bytes() == unmanaged
        assert not (instance / "mods/optional.jar").exists()
        assert optional_value(instance) is False
        assert not any("/config/" in path or path == "/binary/optional.jar"
                       for path in fixture.requests[before:])
        print("PASS changed pack: managed file updated; personal/unmanaged hashes and optional-off preference retained", flush=True)

        _, output = run(instance, "settings-on", settings=True)
        assert "INTEGRATION OPTIONS initial=false" in output
        assert (instance / "mods/optional.jar").read_bytes() == b"synthetic optional mod\n"
        assert optional_value(instance) is True
        elapsed, output = run(instance)
        assert elapsed < 8 and "INTEGRATION OPTIONS" not in output
        assert optional_value(instance) is True
        _, output = run(instance, "settings-off", settings=True)
        assert "INTEGRATION OPTIONS initial=true" in output
        assert not (instance / "mods/optional.jar").exists()
        assert optional_value(instance) is False
        print("PASS --settings on unchanged pack: option can be enabled, retained across launch, then disabled", flush=True)

        cancelled = root / "cancelled-option"
        _, output = run(cancelled, "cancel-optional", expected=1)
        assert "INTEGRATION OPTIONS initial=false" in output
        assert not (cancelled / "mods/managed.jar").exists()
        assert not (cancelled / "packwiz.json").exists()
        print("PASS optional-dialog cancellation: exit 1 and no install committed", flush=True)

        fixture.pack("slow", label="slow")
        fixture.delays["/slow/pack.toml"] = 2
        for mode, marker in [("cancel-main", "CANCEL_MAIN"), ("close-main", "CLOSE_MAIN")]:
            stopped = root / mode
            _, output = run(stopped, mode, label="slow", expected=1)
            assert f"INTEGRATION {marker}" in output
            assert not (stopped / "mods/managed.jar").exists()
            assert not (stopped / "packwiz.json").exists()
        print("PASS main Cancel and window close: exit 1, no successful installation", flush=True)

        fixture.pack("invalid-index", label="invalid-index", corrupt_index=True)
        broken = root / "invalid-index"
        _, output = run(broken, label="invalid-index", expected=1)
        assert "INTEGRATION QUIT" in output
        assert "index file hash is invalid" in output
        assert not (broken / "packwiz.json").exists()
        print("PASS corrupt index hash: visible error, Quit exits 1, no successful manifest", flush=True)

        fixture.pack("invalid-download", label="invalid-download", corrupt_download=True)
        broken = root / "invalid-download"
        _, output = run(broken, label="invalid-download", expected=1)
        assert "INTEGRATION ERROR_CANCEL" in output
        assert not (broken / "packwiz.json").exists()
        print("PASS corrupt download hash: engine rejects download; Cancel launch exits 1", flush=True)
        print(f"PASS {run_count} real installer processes; loopback fixtures and temporary instances only", flush=True)
    finally:
        fixture.close()
        if args.keep:
            print(f"Synthetic evidence: {root}")
        else:
            shutil.rmtree(root)


if __name__ == "__main__":
    main()
