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
    parser.add_argument("--legacy-game-jar", type=Path)
    parser.add_argument("--legacy-version-json", type=Path)
    parser.add_argument("--report", type=Path, default=Path("porting/logs/data-contracts.json"))
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    evidence = json.loads((root / "porting/version-evidence.json").read_text())
    sha1 = hashlib.sha1(args.game_jar.read_bytes()).hexdigest()
    if sha1 != evidence["target"]["client"]["sha1"]:
        raise SystemExit("Game JAR differs from recorded official Mojang SHA-1")
    legacy_sha1 = None
    if bool(args.legacy_game_jar) != bool(args.legacy_version_json):
        raise SystemExit("Legacy JAR and its official version metadata must be supplied together")
    if args.legacy_game_jar:
        legacy_metadata = json.loads(args.legacy_version_json.read_text())
        legacy_sha1 = hashlib.sha1(args.legacy_game_jar.read_bytes()).hexdigest()
        if legacy_metadata["id"] != "1.21.1" or legacy_sha1 != legacy_metadata["downloads"]["client"]["sha1"]:
            raise SystemExit("Legacy fixture requires the checksum-verified official 1.21.1 client")
    libraries = []
    for coordinate in evidence["target"]["libraries"]:
        group, artifact, version, *_ = coordinate.split(":")
        libraries.extend((args.gradle_cache / "caches/modules-2/files-2.1" / group / artifact / version).rglob("*.jar"))
    classpath = os.pathsep.join(str(p.resolve()) for p in [args.game_jar, *libraries])
    out = root / "build/data-contracts"
    out.mkdir(parents=True, exist_ok=True)
    quoted = lambda value: '"' + str(value).replace("\\", "/").replace('"', '\\"') + '"'
    sources = [root / "common/src/main/java/com/cobblemon/mod/common/util/HiveOccupants.java",
               root / "common/src/main/java/com/cobblemon/mod/common/entity/ai/LegacyRandomPos.java",
               root / "common/src/main/java/com/cobblemon/mod/common/entity/ai/LegacyEntityQueries.java",
               root / "common/src/main/java/com/cobblemon/mod/common/util/LegacyMovement.java",
               root / "common/src/main/java/com/cobblemon/mod/common/util/ShoulderData.java",
               root / "common/src/main/java/com/cobblemon/mod/common/api/net/serializers/ShoulderTagDataSerializer.java",
               root / "common/src/main/java/com/cobblemon/mod/common/world/gamerules/CobblemonGameRuleDefinitions.java",
               root / "common/src/main/java/com/cobblemon/mod/common/world/gamerules/LegacyGameRules.java",
               root / "common/src/main/java/com/cobblemon/mod/common/util/LegacyNbt.java",
               root / "common/src/main/java/com/cobblemon/mod/common/util/LegacyBlockState.java",
               root / "common/src/main/java/com/cobblemon/mod/common/api/net/serializers/OptionalUuidDataSerializer.java",
               root / "common/src/main/java/com/cobblemon/mod/common/entity/ai/NullableGroundPathNavigation.java",
               root / "common/src/main/java/com/cobblemon/mod/common/util/ScriptedBrain.java",
               root / "common/src/main/java/com/cobblemon/mod/common/util/ItemUseResults.java",
               root / "common/src/main/java/com/cobblemon/mod/common/util/RegistrationProperties.java",
               root / "common/src/main/java/com/cobblemon/mod/common/item/CobblemonSmithingTemplateItem.java",
               root / "common/src/main/java/com/cobblemon/mod/common/item/CobblemonConsumables.java",
               root / "common/src/test/java/com/cobblemon/mod/common/porting/Minecraft263DataContracts.java",
               root / "common/src/test/java/com/cobblemon/mod/common/porting/Minecraft121RandomContracts.java"]
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
    if code == 0 and args.legacy_game_jar:
        legacy_args = out / "legacy-java.args"
        legacy_args.write_text("\n".join(["-classpath", quoted(str(out) + os.pathsep + classpath + os.pathsep +
                                         str(args.legacy_game_jar.resolve())),
                                         "com.cobblemon.mod.common.porting.Minecraft121RandomContracts"]), encoding="utf-8")
        run = subprocess.run([str(args.javac.with_name("java.exe" if os.name == "nt" else "java")),
                              "@" + str(legacy_args)], capture_output=True, text=True)
        code = run.returncode
        report.update({"passed": code == 0, "legacy_game_sha1": legacy_sha1,
                       "legacy_random_stdout": run.stdout, "legacy_random_stderr": run.stderr})
        if code == 0:
            report["legacy_random_result"] = json.loads(run.stdout.strip().splitlines()[-1])
    (root / args.report).write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
