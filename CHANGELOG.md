# Changelog

## [1.0.1] - 2026-08-08

### Added

- Added quoted Unicode and special-character command values for warp, group, icon, author, and description fields.
- Added automatic quoting and escaping for dynamic command suggestions.
- Added localized suggestion tooltips for warps, groups, custom icons, vanilla item icons, and `none`.
- Added continued Brigadier field completion after quoted Chinese names.
- Added centralized command argument policy, player-only validation, and player location capture.
- Added integration coverage for quoted Chinese suggestions, localized tooltips, typed text values, error cursors,
  vanilla-client argument synchronization, and matching bundled locale keys.
- Added English and Traditional Chinese documentation for the quoted command syntax.

### Fixed

- Fixed Chinese names interrupting Brigadier parsing and preventing later command fields from being suggested.
- Fixed command argument conflicts caused by parallel typed and greedy parsing branches.
- Fixed inconsistent parsing between names, authors, descriptions, icon references, and optional groups.
- Fixed repeated traversal of the vanilla item registry by caching immutable item suggestion names.
- Fixed duplicated player-only checks and player-to-warp-location conversion.

### Removed

- Removed the manual greedy-string command parser and its duplicate execution paths.
- Removed unquoted Unicode and special-character command values; tab completion now inserts the required quotes.
- Removed duplicated command argument extraction and nullable-group conversion helpers.
- Removed obsolete generic argument-error translations superseded by Brigadier's positioned syntax errors.
