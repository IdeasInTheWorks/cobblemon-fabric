# Building the independent Fabric port

Status: migration in progress. These commands reproduce the current build attempt; they do not yet produce a validated Minecraft 26.3 mod. See [COMPATIBILITY.md](COMPATIBILITY.md) and committed logs before installing anything.

## Requirements

- JDK **25** (a JDK, including `javac`, not only a Java runtime). Baseline 1.21.1 uses JDK 21.
- Official Gradle wrapper, upgraded to **9.6.0** with a pinned distribution SHA-256. No global Gradle install is needed.
- Kotlin Gradle plugin/standard library **2.4.21**, resolved automatically by Gradle.
- Architectury Loom **1.17.493**, using `dev.architectury.loom-no-remap` for unobfuscated game distributions.
- Git, Python 3 for package checks, internet access to Mojang/Fabric/Gradle/Maven repositories, and enough memory for the configured Gradle heap (`-Xmx8G` by default).

Use a short path without spaces, outside OneDrive, for a fresh checkout where practical; upstream Architectury tooling warns about paths containing spaces and current Loom warns about OneDrive. The first local attempt is in the user-provided workspace and retains its diagnostics.

## Checkout and configure

```sh
git clone https://github.com/IdeasInTheWorks/cobblemon-fabric.git
cd cobblemon-fabric
git switch feature/latest-minecraft-fabric
git remote add upstream https://gitlab.com/cable-mc/cobblemon.git
git remote set-url --push upstream DISABLED
git config remote.pushDefault origin
git config core.hooksPath .githooks
git config cobblemon.claimPython /absolute/path/to/python3
java -version
javac -version
```

Both Java commands must identify version 25. Set `JAVA_HOME` to the JDK root and include its `bin` directory in PATH. No project-specific secrets are required. Only common and Fabric projects are included in settings; imported NeoForge files remain preserved for history/reference.

Reading/building needs no lease. Editing and pushing require the scoped lease protocol in [AGENTS.md](AGENTS.md), using existing IdeasInTheWorks Git authentication. Never put tokens in commands, tracked files or logs. Each concurrent worker uses an isolated worktree and branch; the shared development branch requires an exclusive integration resource lease.

## Exact build commands

Linux/macOS:

```sh
bash ./gradlew :common:compileKotlin :common:compileJava :common:test :fabric:build --stacktrace --console=plain
python3 -m unittest discover -s scripts -p 'test_*.py' -v
python3 scripts/validate_jar.py fabric/build/libs/cobblemon-1.8.2-fabric-custom.1-mc26.3-$(git rev-parse --short=7 HEAD).jar --output-dir dist
```

PowerShell:

```powershell
.\gradlew.bat :common:compileKotlin :common:compileJava :common:test :fabric:build --stacktrace --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Fabric build failed' }
python -m unittest discover -s scripts -p 'test_*.py' -v
$commit = git rev-parse --short=7 HEAD
python scripts/validate_jar.py "fabric/build/libs/cobblemon-1.8.2-fabric-custom.1-mc26.3-$commit.jar" --output-dir dist
```

The Kotlin toolchain is downloaded as a Gradle dependency. Game classes use Mojang's official names directly; there is no Yarn/Parchment remapping stage for 26.3. The common source output is merged into the Fabric Shadow JAR and mandatory runtime dependencies are nested with Loom. A build must retain the bundled Showdown engine, Pokémon assets and individual asset licences.

## Outputs and test scope

The shared Java regression fixture and JUnit entry point are in `common/src/test/java/com/cobblemon/mod/common/porting/`. S03 executed 48 checks; S04 extended the fixture to 76. S05 passes 127 assertions, adding actual scripted-brain construction, live runtime memories/expiry, sensor requirements, activity transitions, item-use result semantics and food/drink/model component metadata. S06 passes 170 assertions, adding registered battle UUID framing/truncation, native owner storage, all 16 wool registry IDs and legacy teleport-support policy. It also compiles the Java nullable-navigation bridge against the actual game API. Run it against the checksum-verified target JAR and matching libraries, choosing a new report path to preserve prior evidence:

```sh
python3 scripts/check_data_contracts.py --game-jar /path/to/minecraft-26.3-client.jar --gradle-cache /path/to/gradle-user-home --javac /path/to/jdk25/bin/javac --report porting/logs/data-contracts-new-run.json
bash ./gradlew :common:test --tests '*Minecraft263DataMigrationTest' --stacktrace --console=plain
```

The Gradle/JUnit command is blocked until common compilation succeeds; its JUnit wrapper has not executed. Existing CI already runs `:common:test`, so it will include this regression after the compile gate is fixed. The standalone runner compiles the actual production registration/NBT/template helpers and runs the same fixture without compiling the rest of Cobblemon. It bootstraps vanilla registries, uses a synthetic registry and minimal components for two inventory test items, and exercises IDs, legacy UUID arrays, malformed/default reads, typed lists, binary NBT, registry codecs, root ValueInput/ValueOutput merging and inventory slot/count round trips. These are unit fixtures, not client/server startup, gameplay, vanilla datapack-default validation or production-world migration tests. JOML/Unsafe and the root MapCodec adapter emit warnings on Java 25; the successful result records them.

Initial standalone probes exposed frozen intrusive registries and unbound item components. Their failed reports are retained as `sprint-03-data-contracts-01.json` and `-02.json`; the fixture now inspects construction metadata and initializes only missing test-item components. No runtime registry or component bypass was added to the mod itself.

Sprint S02 executed 20 offline Python tests: six synthetic package-validator tests and 14 work-claim protocol tests. These do not run Minecraft. Additional signature and identifier wire-format checks use the checksum-verified official target JAR and its exact cached runtime-library versions:

```sh
python3 scripts/check_api_contracts.py --game-jar /path/to/minecraft-26.3-client.jar --gradle-cache /path/to/gradle-user-home --javac /path/to/jdk25/bin/javac
```

The checker compares the game SHA-1 with `porting/version-evidence.json`, compiles four source contracts and executes four identifier round-trip/wire checks against real game libraries. It neither launches a game nor validates Cobblemon networking end to end. The live two-contender claim-race probe is an explicit network test (`scripts/check_live_claim_race.py`), not an automatic CI mutation.

- Candidate package: `fabric/build/libs/cobblemon-1.8.2-fabric-custom.1-mc26.3-<commit>.jar`, also copied to root `build/libs/` by assemble.
- Other jars marked `dev-slim` or `sources` are development products; do not install them.
- Structurally checked candidate, SHA-256 and JSON report: `dist/`, only after the validator succeeds.
- Unit results: `common/build/test-results/test/` and `common/build/reports/tests/test/`.
- Local migration attempts: `porting/logs/`.
- GitHub Actions uploads candidate packages only after build, tests and structural checks pass. Diagnostic reports are uploaded on failures. It never publishes a release automatically.

Package validation checks metadata, entrypoint/mixin classes, runtime dependencies, licences, assets, Showdown presence, namespace and classfile compatibility. It does not establish game startup or gameplay correctness. Reproducibility between two clean successful builds has not been demonstrated. Stable build/dependency pins and deterministic archive ordering/timestamps are configured, but a reproducible release requires completing and comparing those builds.

## Troubleshooting and release gates

S05 local source compilation used the following PowerShell command after generated Kotlin caches were locked. Quote the properties to keep PowerShell from splitting them. This changes compiler execution only; it does not bypass source or release checks.

```powershell
.\gradlew.bat :common:compileKotlin '-Pkotlin.incremental=false' '-Pkotlin.compiler.execution.strategy=in-process' --stacktrace --console=plain
.\gradlew.bat :fabric:validateAccessWidener :common:checkLicenses --continue '-Pkotlin.incremental=false' '-Pkotlin.compiler.execution.strategy=in-process' --stacktrace --console=plain
```

Stop Gradle/IDE compilers before moving generated caches. Verify absolute paths stay inside the checkout, preserve old outputs in an ignored backup directory, and regenerate only build outputs. S05 preserved old `common/build` and `gradle/build-logic/build` under ignored `build/sprint-05-cache-backups/`; no source or release artifacts were deleted. Prefer a fresh checkout outside OneDrive for future builds.

S07 repeated the source compile (6,005 lines /940 files, still failed) and executed286 focused assertions plus20 tooling tests. Both common and Fabric licence checks pass;49 widener targets remain. The default build-logic output directory hit locked/read-only OneDrive files. Attempts to preserve generated output partly succeeded; nothing in source was deleted. Use this optional local-only init script to regenerate included-build outputs under ignored `build/local-build-logic`:

```powershell
.\gradlew.bat --init-script scripts/isolated_build_logic.gradle :fabric:validateAccessWidener :common:checkLicenses :fabric:checkLicenses --continue '-Pkotlin.incremental=false' '-Pkotlin.compiler.execution.strategy=in-process' --stacktrace --console=plain
```

It changes generated output location only. Validation, source compilation and release gates remain enabled; CI uses the normal wrapper. The standalone runner now compiles shoulder/gamerule/movement helpers too. Its286 assertions do not run full entities, mixin transformation, JUnit, Minecraft or world migration. See `porting/sprint-07-summary.json` and the same native roadmap for current estimates, post-push CI and continuation.

Read the first error in the Gradle log before the stack trace. Do not suppress missing Minecraft APIs or widen Fabric's version requirement to make an older binary load. Resolve obsolete access-widener entries against the actual target classes. Verify integration coordinates and official-name compatibility; old intermediary-mapped mods cannot be assumed compatible.

The old Cadix licence plugin crashes on Gradle 9; it was replaced by Yumi Licenser with header checks retained. If the checker reports a mismatch, preserve existing copyright notices while fixing the rule or missing headers; do not bulk rewrite attribution. A missing Adorn release or other integration is a real dependency issue, not permission to remove its source.

After compilation, use separate disposable `fabric/runClient` and `fabric/runServer` directories. Dedicated server testing requires the server operator's Minecraft EULA acceptance. Keep test databases isolated. Do not use production worlds or change a running production server. Complete the gameplay, two-player networking and persistence matrix in COMPATIBILITY.md before tagging or publishing a release.
