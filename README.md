<div align="center">
  <img src="common/src/main/resources/assets/keybindplus/icon.png" width="128" height="128" alt="KeybindPlus Icon" />
  <h1>KeybindPlus</h1>
  <p><strong>Client-side keybind profile manager, in-game editor, and conflict resolver for Minecraft.</strong></p>

  [![Build Status](https://github.com/KVdz00/keybindplus/actions/workflows/build.yml/badge.svg)](https://github.com/KVdz00/keybindplus/actions)
  [![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
  [![Minecraft Version](https://img.shields.io/badge/Minecraft-26.2+-brightgreen.svg)](VERSIONS.md)
</div>

---

## Features

- **Named Profiles**: Save, rename, duplicate, and switch complete keybind configurations on the fly.
- **In-Game Editor**: Rebind or unbind keys (`NONE`) directly within profiles with real-time conflict highlights and hover tooltips.
- **Visual Compare & 1-Click Sync**: Compare any two profiles side-by-side, filter differences, and copy keybindings across profiles with single-click `<` and `>` sync buttons.
- **Flexible Sorting & Indexing**: Cycle through 6 organization modes (`A-Z`, `Z-A`, `Newest`, `Oldest`, `Imported Only`, `Local Only`).
- **Clean Vanilla-First UI**: Minimalist aesthetic with green active profiles, cyan imported profiles, and subtle gold star (`★`) default indicators.
- **Quick Load & Default**: Set or unset a default profile loaded automatically on startup or via a single hotkey.
- **Safety First**: Instant one-click undo and automatic rolling backups before loading profiles.
- **Seamless File I/O**: Native file dialog for importing and exporting `.json` profiles with schema validation.
- **Zero Chat Clutter**: Silent operation with no chat spam; system toasts are restricted to async file dialogs.

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

- **Supported Loaders**: Fabric Loader (`>=0.19.0`) & NeoForge (`>=26.2`)
- **Required Dependencies**: Architectury API, Fabric API (Fabric only)
- **Java Runtime**: Java 25+
- **Minecraft Version**: 26.2+ *(See [VERSIONS.md](VERSIONS.md) for 1.17–1.21 and 1.8.9 legacy roadmap)*

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
