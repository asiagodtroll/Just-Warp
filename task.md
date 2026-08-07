# JustWarp current specification

## Architecture

- [x] Keep immutable domain values and one shared service facade for commands and GUI
- [x] Delegate warp, group, and icon mutations to focused package-private services
- [x] Keep JSON handling in persistence and replace live state only after successful validation and writes
- [x] Keep the mod server-only under `io.github.asiagodtroll.justwarp`

## Compatibility and behavior

- [x] Support the documented commands, Unicode names, vanilla clients, and configurable permissions
- [x] Preserve JSON order, namespaced identifiers, paginated read-only menus, and teleport safety
- [x] Keep malformed reload data untouched and retain the previous live state
- [x] Use consistent localized GUI and chat presentation
- [x] Quote and autocomplete Unicode command names with precise syntax errors
- [x] Show localized tooltips for dynamic command suggestions
- [x] Use one quotable-string policy for names, authors, and descriptions
- [x] Centralize command argument policy, player-only validation, and player location capture

## Verification and project metadata

- [x] Cover command parsing, mutations, persistence failure safety, reload safety, and GUI formatting
- [x] Pass source formatting checks, `gradlew test`, and `gradlew build`
- [x] Publish as `JustWarp` by AsiaGodTroll under the MIT License
- [x] Keep `README.md`, `docs/README_zh-tw.md`, and `docs/config.md` current

## Balanced best-practice review

- [x] Cache immutable vanilla item suggestions instead of rebuilding them for every completion request
- [x] Cover quoted Chinese suggestions and localized tooltips through reloaded service state
- [x] Enforce matching translation keys across bundled locales with an automated test
- [x] Publish the complete v1.0.1 Added, Fixed, and Removed changelog relative to v1.0.0
- [x] Keep GUI action handling local while only two flows need it; a shared abstraction would add more indirection
- [x] Keep explicit persistence parsing while the schema remains small and strongly validated
- [x] Keep mutation services separate because their atomic write boundaries differ

## Pending in-game acceptance

- [ ] Complete the acceptance checklist in `README.md` with a dedicated server and vanilla client
