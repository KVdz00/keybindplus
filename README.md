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
- **In-Game Editor**: Rebind or unbind keys (`NONE`) directly within profiles without opening vanilla menus.
- **Visual Conflict Resolver**: Automatically detects key overlaps and filters conflicting keybinds in red.
- **Quick Load & Default**: Set a primary profile that loads automatically on startup or via a single hotkey.
- **Safety First**: Instant one-click undo and automatic rolling backups before loading profiles.
- **Seamless File I/O**: Native file dialog for importing and exporting `.json` profiles with schema validation.
- **Zero Chat Clutter**: Silent operation with no chat spam; toast notifications are restricted to file imports/exports.

---

## Controls

| Action | Default Key | Description |
| :--- | :--- | :--- |
| **Open KeybindPlus** | `V` | Opens the main profile management and editor interface. |
| **Quick Load Default** | *Unbound* | Instantly applies your default profile during gameplay. |

*Rebindable in Minecraft Options -> Controls -> Key Binds -> KeybindPlus.*

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
