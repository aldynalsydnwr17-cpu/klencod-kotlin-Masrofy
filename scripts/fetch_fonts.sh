#!/usr/bin/env bash
# Downloads IBM Plex Sans Arabic (OFL) into app/src/main/assets/fonts. The app falls back to the system font if this fails.
set -u
DEST="$(dirname "$0")/../app/src/main/assets/fonts"
mkdir -p "$DEST"
BASE="https://github.com/google/fonts/raw/main/ofl/ibmplexsansarabic"
for w in Regular Medium SemiBold Bold; do
  curl -fsSL --retry 3 -o "$DEST/IBMPlexSansArabic-$w.ttf" "$BASE/IBMPlexSansArabic-$w.ttf" \
    && echo "ok $w" || { echo "failed $w"; rm -f "$DEST/IBMPlexSansArabic-$w.ttf"; }
done
ls -la "$DEST"
