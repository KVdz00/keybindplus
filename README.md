# KeybindPlus

KeybindPlus is a client-side Minecraft mod that lets you create, manage, and switch keybind profiles with conflict detection and an in-game keybind editor. Built with Architectury for Fabric and NeoForge.

---

## Features

- **Keybind Profiles**: Save your entire keybind setup into named profiles and switch between them at any time.
- **Default Profile**: Set a profile as default to automatically apply on game launch or via a dedicated Quick Load hotkey.
- **In-Game Keybind Editor**: Edit keybindings directly inside any profile, rebind keys with keyboard or mouse, and unbind unwanted features to free up keys.
- **Visual Conflict Resolver**: Detects overlapping key assignments and provides a dedicated filter to resolve conflicts in one place.
- **Profile Comparison**: Compare any profile side-by-side against your default profile to see differences.
- **One-Click Undo**: Revert to your previous keybind configuration immediately after loading a profile.
- **Rolling Auto-Backup**: Automatically creates rolling backups (up to 5) before applying a new profile.
- **Import & Export**: Export profiles to JSON files and import them using your operating system's native file chooser with schema validation.

---

## Requirements

- **Minecraft**: 26.2+
- **Java**: 25+
- **Mod Loader**: Fabric Loader (>= 0.19.0) or NeoForge (>= 26.2)
- **Dependencies**: Architectury API, Fabric API (on Fabric)

---

## Default Controls

| Action | Default Key | Description |
| :--- | :--- | :--- |
| Open KeybindPlus Menu | `V` | Opens the profile manager and editor screen. |
| Quick Load Default Profile | *Unbound* | Instantly applies the default profile in-game. |

*Keys can be reconfigured in Options -> Controls -> Key Binds -> KeybindPlus.*

---

## Profile Storage

Profiles and configuration files are stored locally in:
- `.minecraft/config/keybindplus/profiles/` (Active profiles)
- `.minecraft/config/keybindplus/exports/` (Exported profiles)
- `.minecraft/config/keybindplus/imports/` (Import folder)
- `.minecraft/config/keybindplus/backups/` (Automatic rolling backups)

---

## Building from Source

To build KeybindPlus locally:

```bash
# Clone the repository
git clone https://github.com/KVdz00/keybindplus.git
cd keybindplus

# Build all platforms
./gradlew build

# Build Fabric only
./gradlew :fabric:build

# Build NeoForge only
./gradlew :neoforge:build
```

Compiled JAR files will be located in `<loader>/build/libs/`.

---

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code style, branching strategy, and pull request process.

---

## License

This project is licensed under the [MIT License](LICENSE).
