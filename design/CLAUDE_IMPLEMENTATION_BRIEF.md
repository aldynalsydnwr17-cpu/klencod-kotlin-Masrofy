# Claude handoff: FNF Local Mod Installer

Redesign the existing app around local mod installation. This is NOT an online mod downloader/catalog.

## Main flow
1. User downloads a FNF mod separately and stores it on their phone.
2. User taps **استيراد مود** / **اختيار مود من الملفات**.
3. Android file picker opens for supported ZIP or folder.
4. User enters/selects their installed FNF game version.
5. User selects the actual Mods/game folder using Android's folder picker.
6. Inspect and validate the selected mod safely; show name/contents/cover preview.
7. Confirm, then install into the user-selected target folder.
8. Show progress and success/failure; add it to the local library.

## Product rules
- No online mod downloads, no web catalog, no bundled mods.
- Library contains only locally imported/installed mods.
- Never assume one game version or fixed storage path.
- Use Android Storage Access Framework and persist URI access when possible.
- Do not promise writing into protected game folders when Android/game edition blocks it; explain and provide supported folder selection.
- Validate ZIP entries against path traversal; reject unsafe paths.
- Never overwrite files silently; detect conflicts and ask/backup.
- Uninstall only files recorded in an app-owned install manifest; never delete base game files.
- Extract cover art if available, but preserve source image quality; allow manual cover selection.

## Navigation redesign
Bottom bar: الرئيسية | مكتبتي | استيراد مودات | الإعدادات
Remove “اكتشف المودات” as an online discovery page; replace it with local import instructions and a prominent file-picker CTA.

## Main screen reference
Use attached concept PNG as visual guidance only:
- Dark charcoal background, red accent, restrained FNF/graffiti theme.
- Game status card: game name, selected version, selected Mods path, detection/permission status.
- Large import panel with ZIP/folder icon and button “اختيار مود من الملفات”.
- “المودات المثبتة” section showing only local library items.
- Mod card: cover, title, mod version if known, target game version, install status, Details and More.
- Settings: game version, target folder, appearance, file access, app info.
- More menu: details, change cover, verify, safe uninstall.

## Design
Colors: #101014 background, #17151D surface, #25232B cards, #F21D3D accent, #F5F5F7 text, #A69EAE muted.
Font: IBM Plex Sans Arabic https://fonts.google.com/specimen/IBM+Plex+Sans+Arabic
Arabic RTL, consistent icon weight and spacing. Use native UI components for rounded cards/gradients/borders/shadows; do not stretch SVGs as backgrounds.
Do not use the mockup as a screen background. Build real responsive UI components.

## Deliverables
Update the existing project, complete the local import/install workflow, connect all controls, include empty/loading/validation/progress/success/permission/error states, build and test Android, and report platform restrictions honestly. Keep the existing stack unless necessary.
