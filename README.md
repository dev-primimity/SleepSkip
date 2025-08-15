# SleepSkip v1.0

<img width="854" height="480" alt="2025-08-15_13 40 47" src="https://github.com/user-attachments/assets/ef752c29-f060-4195-9407-20e930f1732a" />

A lightweight, configurable Minecraft plugin that skips the night when a set percentage of players are in bed.

## 📌 Features
- Configurable **percentRequired** for skipping
- **Delay** before skipping (lets sleep animation play)
- Customizable **Action bar** messages for:
  - Progress message for criteria
  - Skipping message when threshold is met
- Placeholders for `{sleeping}`, `{needed}`, `{total}`
- Supports Minecraft color codes (`&a`, `&b`, `&l`, etc.)

## 📥 Installation
1. Download the latest release from the [Releases](../../releases) page.
2. Place the `.jar` file into your server's `plugins` folder.
3. Restart your server to generate the `config.yml`.

## 📂 Config
Default `config.yml`:
```yml
settings:
  percentRequired: 0.5
  timeOfDay: 1000
  sleepDelay: 5   # set 0 to completely skip sleep!

messages:
  progress: "&f{sleeping}/{total} players sleeping"
  skipped: "&fSleeping through this night"
```

## 🔗 Links
Need a custom plugin? Message me on Discord:
https://discord.com/users/primimity
