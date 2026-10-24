"""Compile selected migrated API contracts against checksum-verified real game classes."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--game-jar", required=True, type=Path)
    parser.add_argument("--gradle-cache", required=True, type=Path)
    parser.add_argument("--javac", required=True)
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    evidence = json.loads((root / "porting/version-evidence.json").read_text())
    actual = hashlib.sha1(args.game_jar.read_bytes()).hexdigest()
    if actual != evidence["target"]["client"]["sha1"]:
        raise SystemExit("Target client JAR does not match recorded official Mojang SHA-1")
    libs = []
    for coordinate in evidence["target"]["libraries"]:
        group, artifact, version, *_ = coordinate.split(":")
        libs.extend((args.gradle_cache / "caches/modules-2/files-2.1" / group / artifact / version).rglob("*.jar"))
    import os
    classpath = os.pathsep.join(str(p.resolve()) for p in [args.game_jar, *libs])
    out = root / "build/api-contracts"
    out.mkdir(parents=True, exist_ok=True)
    argfile = out / "javac.args"
    quote = lambda value: '"' + str(value).replace("\\", "/").replace('"', '\\"') + '"'
    argfile.write_text("\n".join(["--release", "25", "-proc:none", "-classpath", quote(classpath),
        "-d", quote(out), quote(root / "scripts/fixtures/Minecraft263ApiContracts.java")]), encoding="utf-8")
    result = subprocess.run([args.javac, "@" + str(argfile)], capture_output=True, text=True)
    report = {"passed": result.returncode == 0, "target": "26.3", "game_sha1": actual,
        "scope": "javac signature contracts and identifier wire checks against real target classes; no game launch/gameplay test",
        "contracts": ["entity resource key builder", "block noCollision", "identifier payload read/write", "entity holder tag test"],
        "stdout": result.stdout, "stderr": result.stderr}
    if result.returncode == 0:
        runtime_args = out / "java.args"
        runtime_args.write_text("\n".join(["-classpath", quote(str(out) + os.pathsep + classpath),
            "Minecraft263ApiContracts"]), encoding="utf-8")
        java = str(Path(args.javac).with_name("java.exe" if os.name == "nt" else "java"))
        runtime = subprocess.run([java, "@" + str(runtime_args)], capture_output=True, text=True)
        report.update({"passed": runtime.returncode == 0, "wire_checks_passed": 4 if runtime.returncode == 0 else 0,
            "runtime_stdout": runtime.stdout, "runtime_stderr": runtime.stderr})
        result = runtime
    (root / "porting/logs/sprint-02-api-contracts.json").write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))
    return result.returncode


if __name__ == "__main__":
    raise SystemExit(main())
