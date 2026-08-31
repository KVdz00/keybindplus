<div align="center">
  <img src="common/src/main/resources/assets/keybindplus/icon.png" width="128" height="128" alt="KeybindPlus Icon" />
  <h1>KeybindPlus</h1>
  <p><strong>Client-side keybind profile manager, in-game editor, and conflict resolver for Minecraft.</strong></p>

  [![Build Status](https://github.com/KVdz00/keybindplus/actions/workflows/build.yml/badge.svg)](https://github.com/KVdz00/keybindplus/actions)
  [![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
  [![Minecraft Version](https://img.shields.io/badge/Minecraft-26.2+-brightgreen.svg)](VERSIONS.md)
</div>

---

> [!IMPORTANT]
> **Version Stability Status**:
> - **Minecraft 26.2 (`main`)** is the primary stable production release.
> - Backport branches (`1.21.x`, `1.20.x`, `1.19.x`, `1.18.x`, `1.17.x`, and `1.8.9`) are currently **experimental builds in active development**. They are published as automated test builds via GitHub Actions. See [VERSIONS.md](VERSIONS.md) for full branch details and download links.

---

## Features

- **Named Profiles**: Save, rename, duplicate, and switch complete keybind configurations on the fly.
- **In-Game Editor**: Rebind or unbind keys (`NONE`) directly within profiles with real-time conflict highlights and hover tooltips.
- **Visual Compare & 1-Click Sync**: Compare two profiles side-by-side, filter differences, and copy keybindings across profiles with `<` and `>` sync buttons.
- **Sorting Modes**: Organize profiles by A-Z, Z-A, Newest, Oldest, Imported Only, or Local Only.
- **Clean Vanilla-Style UI**: Visual indicators with green active profiles, cyan imported profiles, and gold star (`★`) default indicators.
- **Quick Load Default**: Set or unset a default profile loaded on startup or via a single hotkey.
- **Undo & Rolling Backups**: Revert to previous keybindings with one click or restore from automatic rolling backups.
- **File Import & Export**: Import and export `.json` profiles directly via native file dialogs.
- **Silent Operation**: Zero chat message spam; status toasts appear only during file dialog actions.

---

## Controls & Keyboard Shortcuts

### Global Keybindings (In-Game)
| Action | Default Key | Description |
| :--- | :--- | :--- |
| **Open KeybindPlus** | `V` | Opens the main profile management and editor interface. |
| **Quick Load Default** | *Unbound* | Instantly applies your default profile during gameplay. |

*Rebindable in Minecraft Options -> Controls -> Key Binds -> KeybindPlus.*

### Menu Shortcuts (Inside KeybindPlus GUI)
| Shortcut | Action | Description |
| :--- | :--- | :--- |
| `Enter` | **Load** | Apply and activate selected profile. |
| `E` | **Edit** | Open in-game keybind editor for selected profile. |
| `C` | **Compare** | Choose a secondary profile to compare and sync keys. |
| `R` | **Rename** | Rename selected profile. |
| `D` | **Default / Unset** | Toggle setting or clearing default profile status. |
| `Delete` / `Backspace` | **Delete** | Delete selected profile with confirmation. |
| `Ctrl + S` | **Save** | Save current active keybinds to a new profile. |
| `Ctrl + D` | **Duplicate** | Clone selected profile. |
| `Ctrl + Z` | **Undo** | Revert keybinds prior to last loaded profile. |
| `V` / `Escape` | **Close** | Close KeybindPlus GUI and resume gameplay. |

---

## Installation & Requirements

- **Primary Supported Release**: Minecraft **26.2+** (Stable v1.2.0)
- **Supported Loaders**: Fabric Loader (`>=0.19.0`) & NeoForge (`>=26.2`)
- **Required Dependencies**: Architectury API, Fabric API (Fabric only)
- **Java Runtime**: Java 25+ (for 26.2)
- **Legacy & Backport Versions**: 1.21.x, 1.20.x, 1.19.x, 1.18.x, 1.17.x, and 1.8.9 are currently in **active development / experimental** state. See [VERSIONS.md](VERSIONS.md) for branch matrices and CI builds.

Place the compiled `.jar` file into your `.minecraft/mods/` directory and launch the game.

---

## Storage Structure

Configuration files and profiles are stored in:
```text
.minecraft/config/keybindplus/
├── profiles/    # Saved keybind profiles (.json)
├── backups/     # Automatic rolling backups (up to 5)
├── exports/     # Exported profiles for sharing
└── imports/     # Default folder for imported profiles
```

---

## Building from Source

```bash
git clone https://github.com/KVdz00/keybindplus.git
cd keybindplus
./gradlew build
```

Compiled JARs will be generated in `fabric/build/libs/` and `neoforge/build/libs/`.

---

## License & Contributing

- Distributed under the [MIT License](LICENSE).
- Contribution guidelines and code standards are documented in [CONTRIBUTING.md](CONTRIBUTING.md).
