# FNF Mod Manager — UI Assets

هذه حزمة أصول شفافة قابلة للاستخدام في تطبيق Android.

## المحتويات
- `fnf-logo-transparent.svg`: شعار شفاف.
- `icons/`: أيقونات SVG منفصلة، لكل أيقونة نسختان red و white.
- `components/glass-card-background.svg`: خلفية بطاقة شفافة.
- `components/primary-button-background.svg`: خلفية زر أحمر.
- `components/mod-image-placeholder.svg`: صورة افتراضية للمود عند عدم وجود غلاف.

## الخط
IBM Plex Sans Arabic:
https://fonts.google.com/specimen/IBM+Plex+Sans+Arabic

## الألوان
- Background: `#101014`
- Surface: `#17151D`
- Card: `#25232B`
- Accent: `#F21D3D`
- Text: `#F5F5F7`
- Muted: `#A69EAE`

## ملاحظات للمطور
- جميع ملفات SVG بخلفية شفافة حيثما يلزم، ويمكن تغيير لونها أو حجمها.
- استخدم SVG/Vector مباشرة أو حوّلها إلى Android Vector Drawable.
- لا تستخدم لقطة الشاشة كخلفية للتطبيق؛ ابنِ الواجهة من مكونات فعلية.
- صور أغلفة المودات يجب استخراجها من ملفات المودات عند الاستيراد، مع الاحتفاظ بالأصل وعدم ضغطه أو قصه بشكل دائم.
- ملفات الأصول هنا عناصر واجهة وأيقونات وليست صور شخصيات للمودات.
