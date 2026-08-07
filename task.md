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

## Verification and project metadata

- [x] Cover command parsing, mutations, persistence failure safety, reload safety, and GUI formatting
- [x] Pass source formatting checks, `gradlew test`, and `gradlew build`
- [x] Publish as `JustWarp` by AsiaGodTroll under the MIT License
- [x] Keep `README.md`, `docs/README_zh-tw.md`, and `docs/config.md` current

## Pending in-game acceptance

- [ ] Complete the acceptance checklist in `README.md` with a dedicated server and vanilla client
