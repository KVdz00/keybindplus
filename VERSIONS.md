# Version Support & Branching Strategy

KeybindPlus follows standard Minecraft modding practices by maintaining version-specific branches.

---

## Version Support Matrix

| Minecraft Version | Branch | Mod Loader(s) | Status | Java Version |
| :--- | :--- | :--- | :--- | :--- |
| **26.2 (Active)** | `main` | Fabric, NeoForge | Supported (v1.1.0) | Java 25 |
| **1.21.x** | `1.21.x` | Fabric, NeoForge | Planned | Java 21 |
| **1.20.x** | `1.20.x` | Fabric, Forge / NeoForge | Planned | Java 17 |
| **1.19.x** | `1.19.x` | Fabric, Forge | Planned | Java 17 |
| **1.18.x** | `1.18.x` | Fabric, Forge | Planned | Java 17 |
| **1.17.x** | `1.17.x` | Fabric, Forge | Planned | Java 16 / 17 |
| **1.8.9 (Old PvP)** | `1.8.9` | Forge 1.8.9 | Planned | Java 8 |

---

## Branching Conventions

- **`main`**: The primary branch targeting the latest modern Minecraft snapshot/release.
- **`<mc-version>`** (e.g. `1.20.x`, `1.19.x`): Branches maintaining compatibility for specific modern Minecraft minor releases.
- **`1.8.9`**: Dedicated legacy branch using Forge 1.8.9 / MCP / RetroLoom toolchain targeting legacy PvP clients.

---

## Tagging & Release Conventions

Releases are tagged using the format:
```
v<mod-version>-mc<minecraft-version>
```
Examples:
- `v1.1.0-mc26.2` (or root `v1.1.0`)
- `v1.1.0-mc1.20.4`
- `v1.0.0-mc1.8.9`

Each release includes compiled binary JAR files for all supported loaders:
- `keybindplus-fabric-<version>.jar`
- `keybindplus-neoforge-<version>.jar`
- `keybindplus-forge-<version>.jar` (for legacy versions)
