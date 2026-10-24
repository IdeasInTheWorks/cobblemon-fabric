"""Regression tests for package rejection, using synthetic ZIP fixtures only."""
import io
import json
from pathlib import Path
import struct
import tempfile
import unittest
import zipfile

from validate_jar import validate


class PackageValidationTests(unittest.TestCase):
    def fixture(self, root, *, minecraft="26.3", omit=(), class_major=69):
        metadata = {
            "id": "cobblemon", "version": "1.8.2-fabric-custom.1+26.3",
            "environment": "*", "depends": {"minecraft": minecraft, "java": "25",
                "fabricloader": ">=0.20.1", "fabric-api": ">=0.165.0+26.3"},
            "entrypoints": {"main": ["fixture.Main"], "client": ["fixture.Client"]},
            "accessWidener": "fixture.accesswidener", "jars": [{"file": "META-INF/jars/runtime.jar"}],
        }
        showdown = io.BytesIO()
        with zipfile.ZipFile(showdown, "w") as engine:
            engine.writestr("index.js", "// Synthetic packaging test fixture only")
        runtime = io.BytesIO()
        with zipfile.ZipFile(runtime, "w") as dependencies:
            for name in ("kotlin/Unit.class", "org/graalvm/polyglot/Context.class",
                         "com/oracle/truffle/js/lang/JavaScriptLanguage.class",
                         "com/mongodb/client/MongoClient.class"):
                dependencies.writestr(name, b"fixture")
        entries = {
            "fabric.mod.json": json.dumps(metadata), "LICENSE": "fixture",
            "assets/cobblemon/bedrock/pokemon/models/license": "fixture",
            "assets/cobblemon/bedrock/pokemon/models/fixture/model.json": "{}",
            "assets/cobblemon/textures/fixture.png": b"fixture",
            "data/cobblemon/showdown.zip": showdown.getvalue(),
            "fixture/Main.class": b"fixture", "fixture/Client.class": b"fixture",
            "com/cobblemon/Fixture.class": b"\xca\xfe\xba\xbe\0\0" + struct.pack(">H", class_major),
            "fixture.accesswidener": "accessWidener v2 official\n",
            "META-INF/jars/runtime.jar": runtime.getvalue(),
        }
        path = Path(root) / "fixture.jar"
        with zipfile.ZipFile(path, "w") as jar:
            for name, content in entries.items():
                if name not in omit:
                    jar.writestr(name, content)
        return path

    def test_complete_structural_fixture(self):
        with tempfile.TemporaryDirectory() as root:
            result = validate(self.fixture(root))
            self.assertTrue(result["passed"], result["errors"])
            self.assertIn("gameplay untested", result["scope"])

    def test_wrong_minecraft_is_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            self.assertFalse(validate(self.fixture(root, minecraft="1.21.1"))["passed"])

    def test_missing_battle_engine_is_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            result = validate(self.fixture(root, omit=("data/cobblemon/showdown.zip",)))
            self.assertIn("Missing bundled Showdown battle engine", result["errors"])

    def test_missing_runtime_dependency_is_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            self.assertFalse(validate(self.fixture(root, omit=("META-INF/jars/runtime.jar",)))["passed"])

    def test_missing_asset_licence_is_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            self.assertFalse(validate(self.fixture(root, omit=(
                "assets/cobblemon/bedrock/pokemon/models/license",)))["passed"])

    def test_unsupported_classfile_is_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            self.assertFalse(validate(self.fixture(root, class_major=70))["passed"])


if __name__ == "__main__":
    unittest.main()
