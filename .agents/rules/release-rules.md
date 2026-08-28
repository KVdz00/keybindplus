# KeybindPlus (VaultBind) Workspace Guidelines

## Release & Deployment Rule
When the user asks to commit, push, tag, or release changes in this workspace:
1. Automatically activate the specialized skill `vaultbind-release` (`.agents/skills/vaultbind-release/SKILL.md`).
2. Run `./gradlew build --no-daemon` to ensure both Fabric and NeoForge artifacts build cleanly.
3. Enforce the `/commit-format` standard with appropriate Update Type Prefixes (`Patch Update:`, `Minor Update:`, etc.) and categorized action headers.
4. Prepare multi-loader binary JAR copies (`keybindplus-fabric-<version>.jar` and `keybindplus-neoforge-<version>.jar`).
5. Create annotated git tag `v<version>` and push with `--tags`.
6. Publish to GitHub Releases using `gh release create` attaching both Fabric and NeoForge JARs.
