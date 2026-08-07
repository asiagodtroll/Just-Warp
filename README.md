# JustWarp

JustWarp is a server-side Fabric mod for managing grouped warps through commands and read-only chest menus. Vanilla
clients can use it without installing the mod.

[繁體中文](docs/README_zh-tw.md) · [Configuration](docs/config.md)

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.156.0+26.2
- Java 25

Place JustWarp and Fabric API in the server's `mods` directory. Configuration is created in `config/justwarp/`.

## Commands

| Command | Purpose | Permission |
| --- | --- | --- |
| `/jw` | Open the main menu | Everyone |
| `/jw help` | Show available commands | Everyone |
| `/jw back` | Return to the previous warp location | Everyone |
| `/jw warp` | Browse warps | Everyone |
| `/jw warp add <name> <icon> [group]` | Add the current player location | Admin |
| `/jw warp del <name>` | Delete a warp | Admin |
| `/jw warp set <name> <field> <value>` | Update a warp | Admin |
| `/jw warp set <name> position` | Update a warp location | Admin |
| `/jw group` | Browse groups | Everyone |
| `/jw group add <name> <icon>` | Add a group | Admin |
| `/jw group del <name>` | Delete a group | Admin |
| `/jw group set <name> <field> <value>` | Update a group | Admin |
| `/jw icon` | Browse custom icons | Everyone |
| `/jw icon add <name> <base64>` | Add a custom icon | Admin |
| `/jw icon del <name>` | Delete an unused custom icon | Admin |
| `/jw icon set <name> <base64>` | Update a custom icon | Admin |
| `/jw reload` | Validate and reload JSON data | Admin |

The default admin permission level is 2. Names are case-sensitive Unicode words without whitespace. Icons accept a
custom icon name or an item ID such as `minecraft:stone`. Use `none` to remove a warp from its group.

## Behavior

- Groups, warps, and icons retain their JSON array order in the GUI
- Menus are paginated and do not allow item movement
- Warp safety behavior is configurable
- Invalid reload data does not replace live state or overwrite the source files
- Command changes are saved immediately

See [the configuration reference](docs/config.md) for available settings and data files.

## Acceptance checklist

- A non-admin can browse and teleport but cannot use management commands
- An admin can manage data and reload; console use is limited to commands that do not require a player location
- Pagination, item locking, cross-dimension teleporting, and rotation work in game
- Invalid JSON and references leave the current live state available

## License

This project is available under the MIT License.
