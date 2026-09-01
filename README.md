# NoFlyZone

Paper plugin that blocks elytra flight with firework rockets (and optionally all elytra gliding) inside a configurable 2D zone.

## Requirements

- **Paper 26.2+** server
- **Java 25+** to build (Paper API 26.2 requires JDK 25)

## Build

```powershell
.\build.ps1 package
```

Output: `target/NoFlyZone-1.0.0.jar` — copy it into your server's `plugins/` folder.

## Commands

| Command | Description |
|---------|-------------|
| `/noflyzone set <radius> <x> <z>` | Sets the zone and enables it |
| `/noflyzone world <name>` | Sets the zone world (default: `world`) |
| `/noflyzone elytra <on\|off>` | Toggles full elytra flight block in the zone |
| `/noflyzone enable` | Enables the zone (keeps current coordinates) |
| `/noflyzone disable` | Disables the zone |
| `/noflyzone info` | Shows current configuration |
| `/noflyzone help` | Shows help |

Alias: `/nfz`

Permission: `noflyzone.admin` (default: OP)

## Configuration (`plugins/NoFlyZone/config.yml`)

```yaml
debug: false

zone:
  enabled: false
  world: world
  radius: 100
  x: 0
  z: 0
  block-elytra-flight: false
```

Set `debug: true` to log every blocked firework boost or elytra flight to the console.

Change the world with `/noflyzone world spawn` or edit `config.yml` manually (reload/restart required).

## Console logging

On startup the plugin logs the loaded zone configuration. Admin commands (`set`, `enable`, `world`, `elytra`, etc.) are logged with the executor name. Player blocks are logged only when `debug: true`.

## Behavior

- Firework blocking applies **only** while gliding on an elytra (`isGliding`).
- On the ground, firework rockets work normally.
- The zone uses **2D** distance (Y axis is ignored).
- With `elytra on`, gliding is stopped in the zone (`setGliding(false)`); the elytra stays equipped.

## Project structure

```
src/main/java/pl/noflyzone/
  NoFlyZonePlugin.java   — main class, config
  ElytraListener.java    — events (boost, glide, move)
  SetZoneCommand.java    — admin commands
```
