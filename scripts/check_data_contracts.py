"""Execute actual production NBT compatibility helpers against verified game libraries."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--game-jar", type=Path, required=True)
    parser.add_argument("--gradle-cache", type=Path, required=True)
    parser.add_argument("--javac", type=Path, required=True)
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    evidence = json.loads((root / "porting/version-evidence.json").read_text())
    sha1 = hashlib.sha1(args.game_jar.read_bytes()).hexdigest()
    if sha1 != evidence["target"]["client"]["sha1"]:
        raise SystemExit("Game JAR differs from recorded official Mojang SHA-1")
    libraries = []
    for coordinate in evidence["target"]["libraries"]:
        group, artifact, version, *_ = coordinate.split(":")
        libraries.extend((args.gradle_cache / "caches/modules-2/files-2.1" / group / artifact / version).rglob("*.jar"))
    classpath = os.pathsep.join(str(p.resolve()) for p in [args.game_jar, *libraries])
    out = root / "build/data-contracts"
    out.mkdir(parents=True, exist_ok=True)
    quoted = lambda value: '"' + str(value).replace("\\", "/").replace('"', '\\"') + '"'
    sources = [root / "common/src/main/java/com/cobblemon/mod/common/util/LegacyNbt.java",
               root / "common/src/main/java/com/cobblemon/mod/common/util/RegistrationProperties.java",
               root / "common/src/main/java/com/cobblemon/mod/common/item/CobblemonSmithingTemplateItem.java",
               root / "common/src/test/java/com/cobblemon/mod/common/porting/Minecraft263DataContracts.java"]
    argfile = out / "javac.args"
    argfile.write_text("\n".join(["--release", "25", "-proc:none", "-classpath", quoted(classpath),
                                  "-d", quoted(out), *map(quoted, sources)]), encoding="utf-8")
    compile_result = subprocess.run([str(args.javac), "@" + str(argfile)], capture_output=True, text=True)
    report = {"passed": False, "game_sha1": sha1, "compile_exit": compile_result.returncode,
              "compile_stdout": compile_result.stdout, "compile_stderr": compile_result.stderr,
              "inventory_fixture": "Only missing MAX_STACK_SIZE components are bound for two test items; game/datapack defaults and gameplay are not tested"}
    code = compile_result.returncode
    if code == 0:
        java_args = out / "java.args"
        java_args.write_text("\n".join(["-classpath", quoted(str(out) + os.pathsep + classpath),
                                       "com.cobblemon.mod.common.porting.Minecraft263DataContracts"]), encoding="utf-8")
        run = subprocess.run([str(args.javac.with_name("java.exe" if os.name == "nt" else "java")),
                              "@" + str(java_args)], capture_output=True, text=True)
        code = run.returncode
        report.update({"passed": code == 0, "runtime_stdout": run.stdout, "runtime_stderr": run.stderr})
        if code == 0:
            # Vanilla bootstrap redirects stdout through the game logger.
            result_line = next(line for line in reversed(run.stdout.splitlines()) if '{"passed":' in line)
            report["result"] = json.loads(result_line[result_line.index('{"passed":'):])
    (root / "porting/logs/sprint-03-data-contracts.json").write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
