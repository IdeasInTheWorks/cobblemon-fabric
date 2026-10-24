"""Fail closed on incomplete Fabric packages; this does not test gameplay."""
from __future__ import annotations

import argparse
import hashlib
import io
import json
from pathlib import Path
import shutil
import struct
import zipfile


def validate(path: Path, minecraft: str = "26.3", java: int = 25) -> dict:
    errors = []
    with zipfile.ZipFile(path) as jar:
        names = set(jar.namelist())
        if jar.testzip() is not None:
            errors.append("Corrupt ZIP entry")
        if "fabric.mod.json" not in names:
            return {"passed": False, "errors": ["Missing fabric.mod.json"]}
        raw = jar.read("fabric.mod.json").decode()
        metadata = json.loads(raw)
        if "${" in raw:
            errors.append("Unexpanded Fabric metadata placeholders")
        if metadata.get("id") != "cobblemon" or metadata.get("environment") != "*":
            errors.append("Expected Cobblemon for client and dedicated server")
        depends = metadata.get("depends", {})
        if depends.get("minecraft") != minecraft:
            errors.append(f"Expected exact Minecraft requirement {minecraft}")
        if depends.get("java") not in (str(java), f">={java}"):
            errors.append(f"Expected Java {java} requirement")
        for dependency in ("fabricloader", "fabric-api"):
            if not depends.get(dependency):
                errors.append(f"Missing mandatory dependency: {dependency}")
        if "fabric-custom" not in metadata.get("version", ""):
            errors.append("Version must identify this custom Fabric port")
        if "LICENSE" not in names:
            errors.append("Missing root licence")
        if not any(n.startswith("assets/cobblemon/") and n.endswith("/license") for n in names):
            errors.append("Missing original asset licence notices")
        if not any(n.startswith("assets/cobblemon/bedrock/pokemon/models/") and n.endswith(".json") for n in names):
            errors.append("Missing Pokémon models")
        if not any(n.startswith("assets/cobblemon/textures/") and n.endswith(".png") for n in names):
            errors.append("Missing textures")
        if "data/cobblemon/showdown.zip" not in names:
            errors.append("Missing bundled Showdown battle engine")
        else:
            with zipfile.ZipFile(io.BytesIO(jar.read("data/cobblemon/showdown.zip"))) as showdown:
                if not any(n.endswith("index.js") for n in showdown.namelist()):
                    errors.append("Showdown bundle has no index.js")
        for entrypoint in ("main", "client"):
            entries = metadata.get("entrypoints", {}).get(entrypoint, [])
            if not entries:
                errors.append(f"Missing {entrypoint} entrypoint")
            for entry in entries:
                value = entry if isinstance(entry, str) else entry["value"]
                classfile = value.split("::")[0].replace(".", "/") + ".class"
                if classfile not in names:
                    errors.append(f"Missing entrypoint class: {classfile}")
        for mixin in metadata.get("mixins", []):
            config = mixin if isinstance(mixin, str) else mixin["config"]
            if config not in names:
                errors.append(f"Missing mixin configuration: {config}")
                continue
            mixin_data = json.loads(jar.read(config))
            package = mixin_data.get("package", "").replace(".", "/")
            for kind in ("mixins", "client", "server"):
                for mixin_class in mixin_data.get(kind, []):
                    name = package + "/" + mixin_class.replace(".", "/") + ".class"
                    if name not in names:
                        errors.append(f"Missing mixin class: {name}")
        aw = metadata.get("accessWidener")
        if not aw or aw not in names:
            errors.append("Missing access widener")
        elif jar.read(aw).decode().splitlines()[0].split() != ["accessWidener", "v2", "official"]:
            errors.append("Access widener must use official 26.x namespace")
        nested_classes = set()
        for nested in metadata.get("jars", []):
            name = nested["file"]
            if name not in names:
                errors.append(f"Missing nested dependency: {name}")
                continue
            with zipfile.ZipFile(io.BytesIO(jar.read(name))) as dependency:
                nested_classes.update(dependency.namelist())
        for required in ("kotlin/Unit.class", "org/graalvm/polyglot/Context.class",
                         "com/oracle/truffle/js/lang/JavaScriptLanguage.class",
                         "com/mongodb/client/MongoClient.class"):
            if required not in nested_classes and required not in names:
                errors.append(f"Missing mandatory runtime class: {required}")
        for name in names:
            if name.startswith("com/cobblemon/") and name.endswith(".class"):
                header = jar.read(name)[:8]
                if len(header) < 8 or header[:4] != b"\xca\xfe\xba\xbe":
                    errors.append(f"Invalid classfile: {name}")
                elif struct.unpack(">H", header[6:8])[0] > java + 44:
                    errors.append(f"Classfile requires newer Java: {name}")
        if "META-INF/neoforge.mods.toml" in names or "quilt.mod.json" in names:
            errors.append("Unexpected other-loader descriptor")
    return {"passed": not errors, "scope": "package structure only; gameplay untested",
            "minecraft": minecraft, "java": java, "version": metadata.get("version"),
            "errors": errors, "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("--minecraft", default="26.3")
    parser.add_argument("--java", type=int, default=25)
    parser.add_argument("--output-dir", type=Path)
    args = parser.parse_args()
    try:
        result = validate(args.jar, args.minecraft, args.java)
    except (OSError, ValueError, KeyError, zipfile.BadZipFile) as error:
        result = {"passed": False, "errors": [str(error)]}
    print(json.dumps(result, indent=2))
    if not result["passed"]:
        return 1
    if args.output_dir:
        args.output_dir.mkdir(parents=True, exist_ok=True)
        destination = args.output_dir / args.jar.name
        if destination.resolve() != args.jar.resolve():
            shutil.copy2(args.jar, destination)
        (args.output_dir / (args.jar.name + ".sha256")).write_text(
            result["sha256"] + "  " + args.jar.name + "\n", encoding="utf-8")
        (args.output_dir / "package-validation.json").write_text(
            json.dumps(result, indent=2) + "\n", encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
