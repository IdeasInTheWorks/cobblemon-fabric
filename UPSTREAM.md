# Upstream and maintenance

This repository is an independent compatibility port maintained by IdeasInTheWorks. It is not an official Cobblemon release. Original development and all original contributor attribution belong to the [Cobblemon project](https://gitlab.com/cable-mc/cobblemon).

- Upstream fetch URL: `https://gitlab.com/cable-mc/cobblemon.git`
- Imported release: tag `1.8.2`, commit `643bf9068b51c829316429916373e5aa9864b8b9` (Minecraft 1.21.1).
- Upstream default branch: `main`; inspected head on 2026-10-24: `f9f4745e933b18204a6f92bd9d7a349093711c79` (1.8.3-SNAPSHOT).
- Development destination: <https://github.com/IdeasInTheWorks/cobblemon-fabric>
- Development branch: `feature/latest-minecraft-fabric`.
- The initial GitHub `main` and development branch point to the unmodified release commit. Its ancestor history is preserved. The full GitLab clone, including other remote branches and tags, is available locally; unrelated upstream branches were not published to GitHub.

## Configure a fresh development clone

```sh
git clone https://github.com/IdeasInTheWorks/cobblemon-fabric.git
cd cobblemon-fabric
git remote add upstream https://gitlab.com/cable-mc/cobblemon.git
git remote set-url --push upstream DISABLED
git config remote.pushDefault origin
git config push.default current
git config core.hooksPath .githooks
git config cobblemon.claimPython /absolute/path/to/python3
git switch feature/latest-minecraft-fabric
git config branch.feature/latest-minecraft-fabric.pushRemote origin
git remote -v
```

Verify that the origin push URL is the IdeasInTheWorks repository and the upstream push URL is `DISABLED`. The repository pre-push hook also rejects other remote names and URLs. Hooks are local safeguards; Git configuration and explicit destination checks remain required.

Acquire a scoped work lease before modifying source, following [AGENTS.md](AGENTS.md). The separate `coordination/work-claims` branch contains the append-only lease registry; do not merge it into source branches or rewrite its history. Keep each worker in an isolated checkout. The native roadmap, effort ranges and handoff are recorded in `porting/tracker.json`; reuse that same Sheet and preserve its sharing. Coordination-only commits do not launch build CI.

## Fetch future upstream changes

```sh
git fetch upstream --tags
git log --oneline HEAD..upstream/main
git diff HEAD...upstream/main -- common fabric gradle
git switch feature/latest-minecraft-fabric
```

Review upstream changes before merging or cherry-picking. Preserve attribution and notices in copied code. Reconcile Minecraft-version differences deliberately and run all build/gameplay/data migration gates again. Push only with `git push origin feature/latest-minecraft-fabric`. Never push branches or tags to upstream, open upstream merge requests, force-push, or merge this project's pull request without the owner's authorization.

## Port policy

Only common and Fabric are in the build scope. Preserve other imported files and history. Keep existing gameplay, Showdown, assets, and extension integration code; compilation is not permission to remove features. Documentation must distinguish intended 26.3 support from executed validation. Development CI must not publish releases. Any future validated release must make its exact corresponding source available here under the applicable licences.

The root MPL-2.0 licence and all nested asset licences are retained. Pokémon model directories have additional Creative Commons notices, including noncommercial restrictions in some notices. Preserve individual contributor credits and verify every bundled dependency's licence before a binary release. No trademark affiliation or official endorsement is claimed.
