---
name: vaultbind-release
description: End-to-end automated release pipeline for KeybindPlus (VaultBind): builds multi-loader JARs (Fabric & NeoForge), calculates SemVer bumps, formats commits, creates annotated git tags, pushes to remote, and publishes GitHub Releases with attached binary assets.
---

# KeybindPlus (VaultBind) Release & Deployment Workflow

This skill encodes the complete, automated release runbook tailored specifically for the **KeybindPlus (VaultBind)** multi-loader Minecraft mod workspace.

---

## 1. Release Pipeline Architecture

```text
[1. Verify & Test] ──→ [2. SemVer & Manifest Sync] ──→ [3. Gradle Build Multi-Loader]
                                                                │
[6. Verify & Delivery] ←── [5. GitHub Release (gh CLI)] ←── [4. Commit, Tag & Push]
```

---

## 2. Standard Release Steps

### Step 1: Pre-Release Verification & Working Tree Check
1. Inspect git status:
   ```powershell
   git status
   git diff
   ```
2. Confirm there are no unstaged secrets or unwanted temporary debug residue.

### Step 2: SemVer Bump & Version Synchronization
1. Determine the version bump based on changes:
   - **MAJOR**: Breaking config changes or fundamental architecture rewrites.
   - **MINOR**: New GUI screens, new features, new profile serialization options.
   - **PATCH**: Bug fixes, layout alignments, conflict detection fixes, logic adjustments.
2. Synchronize version in manifest and docs:
   - `gradle.properties`: update `mod_version=X.Y.Z`
   - `VERSIONS.md`: update active version row `Supported (vX.Y.Z)`
   - `README.md`: verify badge or version references if applicable

### Step 3: Build Multi-Loader JAR Binaries
1. Run Gradle build:
   ```powershell
   .\gradlew build --no-daemon
   ```
2. Prepare collision-free asset copies for release packaging:
   ```powershell
   Copy-Item fabric/build/libs/keybindplus-<version>.jar fabric/build/libs/keybindplus-fabric-<version>.jar
   Copy-Item neoforge/build/libs/keybindplus-<version>.jar neoforge/build/libs/keybindplus-neoforge-<version>.jar
   ```

### Step 4: Staging, Formatted Commit, Tagging & Push
1. Stage modified files:
   ```powershell
   git add .
   ```
2. Commit using the `/commit-format` standard:
   - Prefix: `Patch Update:`, `Minor Update:`, `Major Update:`, `HotFix Update:`, or `Maintenance Update:`
   - Grouped body: Module headers (e.g., `KeybindEditor, GUI:`, `KeybindApplier:`) with categorized action types (`Added:`, `Fix:`, `Changes:`).
   ```powershell
   git commit -m "<Title>" -m "<Grouped Body>"
   ```
3. Create annotated git tag:
   ```powershell
   git tag -a v<version> -m "Release v<version>"
   ```
4. Push commits and tags to remote:
   ```powershell
   git push origin main --tags
   ```

### Step 5: Publish GitHub Release via `gh` CLI
1. Verify GitHub CLI auth: `gh auth status`.
2. Generate release notes markdown to a scratch file:
   ```markdown
   ## KeybindPlus v<version> (Minecraft <mc_version>+)

   ### What's Changed
   - **<Feature / Fix Area>**: <Concise summary>

   ### Supported Platforms
   - **Minecraft**: `<mc_version>+`
   - **Fabric Loader**: `>=0.19.0`
   - **NeoForge**: `>=26.2.0`
   - **Java**: `25+`
   ```
3. Publish release and attach binary JAR assets:
   ```powershell
   gh release create v<version> fabric/build/libs/keybindplus-fabric-<version>.jar neoforge/build/libs/keybindplus-neoforge-<version>.jar --title "v<version> (Minecraft <mc_version>+)" -F "<path-to-notes>"
   ```
4. Verify release status:
   ```powershell
   gh release view v<version>
   ```

---

## 3. Quick Checklist for the Agent

- [ ] Gradle build succeeds (`BUILD SUCCESSFUL`)
- [ ] Both Fabric and NeoForge JARs exist and are non-empty
- [ ] Version in `gradle.properties` matches tag `vX.Y.Z`
- [ ] Commit message strictly adheres to `/commit-format` rules
- [ ] Remote push includes both commit and tag (`--tags`)
- [ ] GitHub release URL is reported with direct asset download links
