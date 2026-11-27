#!/usr/bin/env bash
# Downloads Cairo (variable font, OFL) into app/src/main/assets/fonts. The app falls back to IBM Plex / the system font if this fails.
set -u
DEST="$(dirname "$0")/../app/src/main/assets/fonts"
mkdir -p "$DEST"
URL="https://github.com/google/fonts/raw/main/ofl/cairo/Cairo%5Bslnt%2Cwght%5D.ttf"
curl -fsSL --retry 3 -o "$DEST/Cairo-VF.ttf" "$URL" && echo "ok Cairo" || { echo "failed Cairo"; rm -f "$DEST/Cairo-VF.ttf"; }
ls -la "$DEST"
