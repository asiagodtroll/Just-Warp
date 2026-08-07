# JustWarp configuration

JustWarp stores configuration in `config/justwarp/`. Run `/jw reload` after editing files.

## Settings

```json
{
  "plugin": {
    "locale": "zh_TW",
    "admin-permission-level": 2
  },
  "warp": {
    "teleport-safety": 1,
    "safe-search-radius": 5,
    "safe-search-vertical-range": 3
  }
}
```

| Key | Range | Purpose |
| --- | --- | --- |
| `locale` | `en_US`, `zh_TW` | Server-wide language |
| `admin-permission-level` | `0`–`4` | Management command permission |
| `teleport-safety` | `1`–`3` | Exact, search-safe, or reject-unsafe teleporting |
| `safe-search-radius` | `0`–`64` | Horizontal safe-position range |
| `safe-search-vertical-range` | `0`–`32` | Vertical safe-position range |

## Data files

- `groups.json` stores ordered groups and their ordered warp names
- `warps.json` stores ordered warps, metadata, dimension, coordinates, and rotation
- `icons.json` stores ordered Base64 player-head textures

All data files use `schema-version: 2`. Warp and group icons accept custom icon names or namespaced item IDs.

## Safety

Reload validates all files before replacing live state. Invalid files remain untouched. Command changes are staged
before replacement, and coordinated group and warp writes roll back after handled write failures.
