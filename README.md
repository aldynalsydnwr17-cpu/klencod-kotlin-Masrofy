# FNF Mod Manager

تطبيق أندرويد (Kotlin + Jetpack Compose) لتثبيت مودات V-Slice في Friday Night Funkin' الرسمية.
يختار المستخدم ملف المود ويكتب إصدار لعبته، والتطبيق ينسخ المود إلى `mods`، يضبط `api_version`، ويتأكد أن لا ملف وصل 0 B.

- التصميم: `design/` (المرجع + ملفات SVG)، محوّلة إلى VectorDrawable بواسطة `scripts/build_assets.py`.
- الخط: IBM Plex Sans Arabic (يُحمَّل في الـ workflow عبر `scripts/fetch_fonts.sh`).
- الـ APK يظهر في Releases بعد كل Push على main.
