#!/usr/bin/env python3
"""Build a pinned Packwiz UI fork using only Python and the JDK."""
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import urllib.request
import zipfile
from localize_ui import LOCALIZED_ENTRIES, localize_entry

ROOT = Path(__file__).resolve().parent
VENDOR = ROOT / "vendor/packwiz-installer-0.5.14.jar"
SHA256 = "c9f646908d340d84773948a9a7d98bc1dae250d35e1016dc6e2b8459760b5598"
URL = "https://github.com/packwiz/packwiz-installer/releases/download/v0.5.14/packwiz-installer.jar"
REPLACED = "link/infra/packwiz/installer/ui/gui/InstallWindow.class"


def simple_jar(name, sources, classpath, resources=None, main_class=None):
    classes = ROOT / "build" / name
    if classes.exists():
        shutil.rmtree(classes)
    classes.mkdir(parents=True)
    subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-cp", classpath,
                    "-d", str(classes), *map(str, sources)], check=True)
    if resources:
        shutil.copytree(resources, classes, dirs_exist_ok=True)
    target = ROOT / "dist" / (name + ".jar")
    with zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as archive:
        manifest = "Manifest-Version: 1.0\r\nImplementation-Version: 0.2.0-local\r\n"
        if main_class:
            manifest += "Main-Class: " + main_class + "\r\n"
        archive.writestr("META-INF/MANIFEST.MF", manifest + "\r\n")
        for path in sorted(classes.rglob("*")):
            if path.is_file():
                archive.write(path, path.relative_to(classes).as_posix())
    target.with_suffix(".sha256").write_text(hashlib.sha256(target.read_bytes()).hexdigest() + "\n")
    return target


def build():
    if not VENDOR.exists():
        payload = urllib.request.urlopen(URL, timeout=60).read()
        if hashlib.sha256(payload).hexdigest() != SHA256:
            raise SystemExit("Upstream JAR checksum mismatch; nothing installed.")
        VENDOR.parent.mkdir(parents=True, exist_ok=True)
        VENDOR.write_bytes(payload)
    if hashlib.sha256(VENDOR.read_bytes()).hexdigest() != SHA256:
        raise SystemExit("Upstream JAR checksum mismatch; nothing built.")
    classes = ROOT / "build/classes"
    if classes.exists():
        shutil.rmtree(classes)
    classes.mkdir(parents=True)
    sources = sorted((ROOT / "src/main/java").rglob("*.java"))
    subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-cp", str(VENDOR),
                    "-d", str(classes), *map(str, sources)], check=True)
    resources = ROOT / "src/main/resources"
    if resources.exists():
        shutil.copytree(resources, classes, dirs_exist_ok=True)
    if not (classes / REPLACED).is_file():
        raise SystemExit("Missing Entegre window; refusing an unbranded build.")
    destination = ROOT / "dist/entegre-updater.jar"
    destination.parent.mkdir(exist_ok=True)
    temporary = destination.with_suffix(".tmp")
    with zipfile.ZipFile(VENDOR) as upstream, zipfile.ZipFile(temporary, "w", zipfile.ZIP_DEFLATED) as output:
        output.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\r\n"
                        "Main-Class: com.entegre.launcher.Launcher\r\n"
                        "Implementation-Version: 0.2.0-local\r\n"
                        "Packwiz-Version: v0.5.14\r\n\r\n")
        for entry in upstream.infolist():
            if entry.filename not in {"META-INF/MANIFEST.MF", REPLACED}:
                output.writestr(entry, localize_entry(entry.filename, upstream.read(entry.filename)))
        for path in sorted(classes.rglob("*")):
            if path.is_file():
                output.write(path, path.relative_to(classes).as_posix())
        output.write(ROOT / "vendor/LICENSE", "META-INF/ENTEGRE-PACKWIZ-LICENSE.txt")
    with zipfile.ZipFile(VENDOR) as upstream, zipfile.ZipFile(temporary) as output:
        for name in upstream.namelist():
            if name not in {"META-INF/MANIFEST.MF", REPLACED}:
                assert localize_entry(name, upstream.read(name)) == output.read(name), f"Unexpected engine change: {name}"
        assert len(output.namelist()) == len(set(output.namelist())), "Duplicate JAR entries"
    temporary.replace(destination)
    digest = hashlib.sha256(destination.read_bytes()).hexdigest()
    destination.with_suffix(".jar.sha256").write_text(digest + "  " + destination.name + "\n")
    destination.with_suffix(".sha256").write_text(digest + "\n")
    additional_classpath = []
    bootstrap_sources = sorted((ROOT / "src/bootstrap/java").rglob("*.java"))
    if bootstrap_sources:
        bootstrap = simple_jar("entegre-bootstrap", bootstrap_sources, ".", main_class="com.entegre.bootstrap.Bootstrap")
        additional_classpath.append(str(bootstrap))
    transition_sources = sorted((ROOT / "src/transition/java").rglob("*.java"))
    if transition_sources:
        configured = os.environ.get("FABRIC_LOADER_JAR")
        candidates = [Path(configured)] if configured else sorted((Path.home() / ".gradle/caches/modules-2/files-2.1/net.fabricmc/fabric-loader/0.18.4").glob("*/fabric-loader-0.18.4.jar"))
        if not candidates or not candidates[0].is_file():
            raise SystemExit("Fabric Loader 0.18.4 compile JAR required; set FABRIC_LOADER_JAR.")
        loader = candidates[0]
        helper = ROOT / "src/bootstrap/java/com/entegre/bootstrap/Payloads.java"
        transition = simple_jar("entegre-updater-transition-0.1.0", [*transition_sources, helper], str(loader),
                                ROOT / "src/transition/resources")
        additional_classpath.extend([str(transition), str(loader)])
    tests = sorted((ROOT / "tests").glob("*.java"))
    if tests:
        test_classes = ROOT / "build/test-classes"
        if test_classes.exists():
            shutil.rmtree(test_classes)
        test_classes.mkdir(parents=True)
        subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-cp", os.pathsep.join([str(destination), *additional_classpath]),
                        "-d", str(test_classes), *map(str, tests)], check=True)
    print("Built Turkish updater and Prism migration guard; pinned Packwiz engine preserved.")
    print("SHA256", digest)


if __name__ == "__main__":
    build()
