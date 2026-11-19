package com.khatti.app.ui

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.khatti.app.R
import com.khatti.app.data.*
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate

private enum class Tab(val label: String, val icon: Int) {
    Home("الرئيسية", R.drawable.ic_home),
    Progress("تقدمي", R.drawable.ic_bars),
    Library("المكتبة", R.drawable.ic_book),
    Settings("الإعدادات", R.drawable.ic_gear),
}

private enum class Flow { Drill, Capture, Result }

// ───────────────────────── الهيكل الرئيسي ─────────────────────────

@Composable
fun KhattiApp() {
    val ctx = LocalContext.current
    val store = remember { Store(ctx) }
    var splash by remember { mutableStateOf(true) }
    var tab by remember { mutableStateOf(Tab.Home) }
    var flow by remember { mutableStateOf<Flow?>(null) }
    var photo by remember { mutableStateOf<File?>(null) }
    var version by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { delay(1700); splash = false }
    if (splash) { SplashScreen(); return }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (flow == null) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.values().forEach {
                    NavigationBarItem(
                        selected = tab == it, onClick = { tab = it },
                        icon = { Ico(it.icon, size = 26.dp) },
                        label = { Text(it.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Amber, selectedTextColor = Amber,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (flow) {
                Flow.Drill -> DrillScreen(store.drillIndex, onExit = { flow = null }, onFinish = { flow = Flow.Capture })
                Flow.Capture -> CaptureScreen(store, onClose = { flow = Flow.Drill }, onCaptured = { photo = it; flow = Flow.Result })
                Flow.Result -> ResultScreen(store, photo, onBack = { flow = Flow.Capture }, onDone = { failed ->
                    store.recordPractice(failed, DRILLS[store.drillIndex].letter)
                    store.drillIndex = store.drillIndex + 1
                    version++; flow = null; tab = Tab.Home
                })
                null -> key(version) {
                    when (tab) {
                        Tab.Home -> HomeScreen(store) { flow = Flow.Drill }
                        Tab.Progress -> ProgressScreen(store)
                        Tab.Library -> LibraryScreen()
                        Tab.Settings -> SettingsScreen(store) { version++ }
                    }
                }
            }
        }
    }
}

// ───────────────────────── مكونات مشتركة ─────────────────────────

/** أيقونة PNG شفافة من التصميم، تتلوّن مثل أي أيقونة عادية */
@Composable
private fun Ico(res: Int, tint: Color = LocalContentColor.current, size: Dp = 24.dp, modifier: Modifier = Modifier) =
    Icon(painterResource(res), null, modifier.size(size), tint = tint)

@Composable
private fun PrimaryButton(
    text: String, color: Color = Amber, trailing: ImageVector? = null, modifier: Modifier = Modifier, onClick: () -> Unit,
) = Button(
    onClick, modifier = modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(27.dp),
    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    if (trailing != null) { Spacer(Modifier.width(8.dp)); Icon(trailing, null) }
}

@Composable
private fun TopBar(
    title: String, onBack: (() -> Unit)? = null, endIcon: ImageVector? = null, onEnd: () -> Unit = {},
    tint: Color = MaterialTheme.colorScheme.onBackground,
) = Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(48.dp)) {
        if (endIcon != null) IconButton(onEnd) { Icon(endIcon, null, tint = tint) }
    }
    Text(title, Modifier.weight(1f), textAlign = TextAlign.Center, color = tint,
        style = MaterialTheme.typography.titleMedium)
    Box(Modifier.size(48.dp)) {
        if (onBack != null) IconButton(onBack) { Icon(Icons.Outlined.ChevronLeft, "رجوع", tint = tint) }
    }
}

@Composable
private fun Segments(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)) {
        options.forEachIndexed { i, o ->
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                    .background(if (i == selected) Ink else Color.Transparent)
                    .clickable { onSelect(i) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(o, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = if (i == selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = Card(
    modifier = modifier.fillMaxWidth(), shape = CardShape,
    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(2.dp),
) { Column(Modifier.padding(16.dp), content = content) }

private fun loadBitmap(file: File, maxSide: Int = 1000) = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
    val bmp = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return@runCatching null
    val deg = when (ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (deg == 0f) bmp else android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(deg) }, true)
}.getOrNull()?.asImageBitmap()

@Composable
private fun PhotoThumb(file: File?, label: String, modifier: Modifier = Modifier) {
    val bmp = remember(file) { file?.takeIf { it.exists() }?.let { loadBitmap(it) } }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (bmp != null) Image(bmp, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Text("لا توجد صورة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ───────────────────────── شاشة البداية ─────────────────────────

@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(Paper), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val wave = Path().apply {
                moveTo(0f, h * .84f)
                cubicTo(w * .30f, h * .76f, w * .60f, h * .94f, w, h * .82f)
                lineTo(w, h); lineTo(0f, h); close()
            }
            drawPath(wave, Ink)
            val swoosh = Path().apply {
                moveTo(0f, h * .815f)
                cubicTo(w * .30f, h * .735f, w * .60f, h * .915f, w, h * .795f)
            }
            drawPath(swoosh, Amber, style = Stroke(width = 8f, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("خطّي", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            Text("اكتب أجمل .. بثقة أكبر", fontSize = 15.sp, color = Ink.copy(alpha = .7f))
        }
    }
}

// ───────────────────────── 1. الرئيسية ─────────────────────────

@Composable
private fun HomeScreen(store: Store, onStart: () -> Unit) {
    val drill = DRILLS[store.drillIndex]
    val streak = store.streak()
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.Outlined.Notifications, null, tint = Ink)
            Icon(Icons.Outlined.Menu, null, tint = Ink)
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("خطّي", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            Text("اكتب أجمل .. بثقة أكبر", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Spacer(Modifier.height(4.dp))
        SurfaceCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("سلسلة اليوم", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (streak == 0) "ابدأ سلسلتك" else "$streak يومًا", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    if (streak > 0) Text("مستمرة", fontSize = 12.sp, color = Amber, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.size(56.dp).clip(CircleShape).background(Amber.copy(alpha = .15f)), contentAlignment = Alignment.Center) {
                    Ico(R.drawable.ic_flame, Amber, 34.dp)
                }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(Ink)) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("خط اليوم", color = Color.White.copy(alpha = .7f), fontSize = 12.sp)
                    Text("تدريب على ${drill.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("مع كلمات: " + drill.words.joinToString("، "), color = Color.White.copy(alpha = .8f), fontSize = 13.sp)
                }
                Ico(R.drawable.ic_calendar, Color.White, 30.dp)
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("بدء التدريب", trailing = Icons.Outlined.ChevronLeft, onClick = onStart)
        Spacer(Modifier.height(10.dp))
    }
}

// ───────────────────────── 2. شاشة التدريب ─────────────────────────

@Composable
private fun RuledPaper(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val step = 38.dp.toPx()
        var y = step * .6f
        while (y < size.height) {
            drawLine(Gray.copy(alpha = .35f), Offset(0f, y), Offset(size.width, y), 2f)
            y += step
        }
        drawLine(RuleRed, Offset(0f, size.height * .62f), Offset(size.width, size.height * .62f), 4f)
    }
}

@Composable
private fun DrillScreen(index: Int, onExit: () -> Unit, onFinish: () -> Unit) {
    val d = DRILLS[index]
    val steps = remember(index) { listOf(d.letter) + d.words }
    var step by remember(index) { mutableIntStateOf(0) }
    var info by remember { mutableStateOf(false) }
    if (info) AlertDialog(
        onDismissRequest = { info = false }, confirmButton = { TextButton({ info = false }) { Text("تمام") } },
        title = { Text("إزاي أتدرب؟") },
        text = { Text("انسخ الحرف أو الكلمة في دفترك على سطر كامل، وخلّي قاعدة الحروف على الخط الأحمر. بعد ما تخلّص كل الخطوات هتصوّر صفحتك.") },
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TopBar("تدريب اليوم", onBack = onExit, endIcon = Icons.Outlined.Info, onEnd = { info = true })
        Box(Modifier.fillMaxWidth().weight(1f).clip(CardShape).background(MaterialTheme.colorScheme.surface)) {
            RuledPaper(Modifier.fillMaxSize())
            Text(
                steps[step], fontSize = if (step == 0) 150.sp else 76.sp, fontWeight = FontWeight.ExtraBold, color = Ink,
                modifier = Modifier.align(Alignment.Center).padding(bottom = 30.dp),
            )
            Text("${step + 1} من ${steps.size}", Modifier.align(Alignment.TopStart).padding(12.dp), fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Ico(R.drawable.ic_bulb, Amber, 28.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("نصيحة:", fontWeight = FontWeight.Bold)
                    Text(d.tip, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 14.dp)) {
            OutlinedButton(
                onClick = { if (step > 0) step-- else onExit() },
                modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(26.dp),
            ) { Text("السابق", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
            Button(
                onClick = { if (step < steps.lastIndex) step++ else onFinish() },
                modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White),
            ) {
                Text(if (step < steps.lastIndex) "التالي" else "صوّر صفحتك", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp)); Icon(Icons.Outlined.ChevronLeft, null)
            }
        }
    }
}

// ───────────────────────── 3. شاشة الكاميرا ─────────────────────────

@Composable
private fun CaptureScreen(store: Store, onClose: () -> Unit, onCaptured: (File) -> Unit) {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<File?>(null) }
    var torch by remember { mutableStateOf(false) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pending
        if (ok && f != null) onCaptured(f) else f?.delete()
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val f = store.newPhotoFile()
            val ok = runCatching {
                ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } }; true
            }.getOrDefault(false)
            if (ok) onCaptured(f)
        }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF14171C))) {
        TopBar("صوّر صفحتك", onBack = null, endIcon = Icons.Outlined.FlashOn, tint = Color.White)
        Box(Modifier.fillMaxWidth().padding(start = 8.dp)) {
            IconButton(onClose) { Icon(Icons.Outlined.Close, "إغلاق", tint = Color.White) }
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color(0xFF2A2F38)))
            Canvas(Modifier.fillMaxSize()) {
                val l = 42.dp.toPx(); val sw = 5.dp.toPx(); val c = Color.White
                fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                    drawLine(c, Offset(x, y), Offset(x + dx * l, y), sw, StrokeCap.Round)
                    drawLine(c, Offset(x, y), Offset(x, y + dy * l), sw, StrokeCap.Round)
                }
                corner(0f, 0f, 1f, 1f); corner(size.width, 0f, -1f, 1f)
                corner(0f, size.height, 1f, -1f); corner(size.width, size.height, -1f, -1f)
            }
            Text("ضع الصفحة داخل الإطار وتأكد من الإضاءة الجيدة", color = Color.White.copy(alpha = .85f),
                textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
        }
        Text("صوّر الصفحة بوضوح وتأكد من الإضاءة الجيدة", color = Color.White, fontSize = 13.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp))
        Row(
            Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIcon(Icons.Outlined.PhotoLibrary, false) { gallery.launch("image/*") }
            Box(
                Modifier.size(78.dp).clip(CircleShape).border(4.dp, Amber, CircleShape).padding(7.dp).clip(CircleShape).background(Amber)
                    .clickable {
                        val f = store.newPhotoFile(); pending = f
                        camera.launch(FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", f))
                    },
            )
            RoundIcon(Icons.Outlined.FlashlightOn, torch) { torch = !torch }
        }
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, active: Boolean, onClick: () -> Unit) = Box(
    Modifier.size(52.dp).clip(CircleShape).background(if (active) Amber else Color.White.copy(alpha = .15f)).clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) { Icon(icon, null, tint = Color.White) }

// ───────────────────────── 4. شاشة النتيجة ─────────────────────────

private val RATING_LABELS = listOf("ضعيف", "جيد", "ممتاز")

@Composable
private fun ResultScreen(store: Store, current: File?, onBack: () -> Unit, onDone: (failed: Boolean) -> Unit) {
    val criteria = listOf("النقط", "الحجم", "الالتزام بالسطر")
    val rating = remember { mutableStateListOf(1, 1, 1) }
    val before = remember { store.photos().filter { it != current }.lastOrNull() }
    val score = rating.sum() / 6f
    val (headline, sub, accent) = when {
        score >= .8f -> Triple("أحسنت!", "أداء ممتاز", Good)
        score >= .5f -> Triple("جيد جدًا!", "كمّل على نفس المستوى", Good)
        else -> Triple("حاول تاني", "محتاج تدريب أكتر", Amber)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        TopBar("نتيجة التدريب", onBack = onBack)
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(84.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(52.dp))
            }
            Text(headline, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = accent, modifier = Modifier.padding(top = 8.dp))
            Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Text("قيّم صفحتك بصراحة بعد ما تقارنها بالنموذج:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        SurfaceCard {
            criteria.forEachIndexed { i, c ->
                if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Gray.copy(alpha = .25f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(c, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    RATING_LABELS.forEachIndexed { r, label ->
                        val on = rating[i] == r
                        Box(
                            Modifier.padding(start = 4.dp).clip(RoundedCornerShape(14.dp))
                                .background(if (on) (if (r == 0) Amber else Good) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { rating[i] = r }.padding(horizontal = 10.dp, vertical = 6.dp),
                        ) { Text(label, fontSize = 12.sp, color = if (on) Color.White else MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    Spacer(Modifier.width(10.dp))
                    Ico(R.drawable.ic_check, if (rating[i] >= 1) Good else Gray, 28.dp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PhotoThumb(before, "قبل", Modifier.weight(1f))
            Icon(Icons.Outlined.ArrowBack, null, tint = Good, modifier = Modifier.padding(horizontal = 8.dp))
            PhotoThumb(current, "بعد", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton("مواصلة التدريب", Ink) { onDone(rating.any { it == 0 }) }
        Spacer(Modifier.height(16.dp))
    }
}

// ───────────────────────── 5. شاشة التقدم ─────────────────────────

@Composable
private fun ProgressScreen(store: Store) {
    var range by remember { mutableIntStateOf(0) }
    val today = remember { LocalDate.now() }
    val (values, labels) = remember(range) {
        when (range) {
            0 -> store.weekCounts() to WEEK_DAYS
            1 -> (3 downTo 0).map { w -> (0..6).sumOf { d -> store.dayCount(today.minusDays((w * 7 + d).toLong())) } } to
                listOf("قبل 3 أسابيع", "قبل أسبوعين", "الأسبوع الماضي", "هذا الأسبوع")
            else -> (5 downTo 0).map { m ->
                val ym = today.minusMonths(m.toLong())
                (1..ym.lengthOfMonth()).sumOf { d -> store.dayCount(ym.withDayOfMonth(d)) }
            } to (5 downTo 0).map { today.minusMonths(it.toLong()).monthValue.toString() }
        }
    }
    val weak = remember { store.weakLetters() }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TopBar("تقدمي")
        Segments(listOf("أسبوع", "شهر", "كل الوقت"), range) { range = it }
        SurfaceCard {
            val max = maxOf(1, values.max())
            Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                values.forEachIndexed { i, v ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f)) {
                        if (v > 0) Text("$v", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Amber)
                        val h: Dp = maxOf(6f, 110f * v / max).dp
                        Box(Modifier.width(22.dp).height(h).clip(RoundedCornerShape(6.dp)).background(if (v > 0) Amber else Gray.copy(alpha = .35f)))
                        Spacer(Modifier.height(6.dp))
                        Text(labels[i], fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = CardShape, colors = CardDefaults.cardColors(Amber.copy(alpha = .15f))) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.EmojiEvents, null, tint = Amber, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("أعلى سلسلة", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${store.bestStreak()} يومًا", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        Text("الحروف الأكثر احتياجًا للتدريب", fontWeight = FontWeight.Bold)
        if (weak.isEmpty()) Text("لسه مفيش بيانات، أكمل أول تدريب وهتظهر هنا.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            weak.take(6).forEach {
                Box(Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ───────────────────────── 6. المكتبة ─────────────────────────

private val TOPIC_ICONS = listOf(Icons.Outlined.Science, Icons.Outlined.Work, Icons.Outlined.Home)

@Composable
private fun LibraryScreen() {
    var mode by remember { mutableIntStateOf(0) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TopBar("المكتبة", endIcon = Icons.Outlined.MenuBook) }
        item { Segments(listOf("حسب الموضوع", "حسب الحرف"), mode) { mode = it } }
        if (mode == 0) {
            items(LIBRARY.size) { i ->
                val (topic, words) = LIBRARY[i]
                var open by remember { mutableStateOf(false) }
                SurfaceCard(Modifier.clickable { open = !open }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(Ink.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                            Icon(TOPIC_ICONS[i % TOPIC_ICONS.size], null, tint = Ink)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(topic, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${words.size} كلمة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(if (open) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.ChevronLeft, null, tint = Gray)
                    }
                    if (open) words.forEach { WordLine(it) }
                }
            }
        } else {
            items(DRILLS.size) { i ->
                val d = DRILLS[i]
                var open by remember { mutableStateOf(false) }
                SurfaceCard(Modifier.clickable { open = !open }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(CircleShape).background(Amber.copy(alpha = .15f)), contentAlignment = Alignment.Center) {
                            Text(d.letter, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Amber)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(d.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(d.words.joinToString("، "), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(if (open) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.ChevronLeft, null, tint = Gray)
                    }
                    if (open) d.words.forEach { WordLine(it) }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun WordLine(word: String) {
    Text(word, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = .85f), modifier = Modifier.padding(top = 12.dp))
    HorizontalDivider(color = RuleRed.copy(alpha = .45f))
}

// ───────────────────────── الإعدادات ─────────────────────────

@Composable
private fun SettingsScreen(store: Store, onReset: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("مسح كل التقدم؟") },
        text = { Text("هيتمسح السلسلة والإحصائيات والصور المحفوظة، ومينفعش تتسترجع.") },
        confirmButton = { TextButton({ store.reset(); confirm = false; onReset() }) { Text("امسح", color = RuleRed) } },
        dismissButton = { TextButton({ confirm = false }) { Text("إلغاء") } },
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TopBar("الإعدادات")
        SurfaceCard {
            Text("عن التطبيق", fontWeight = FontWeight.Bold)
            Text("خطّي: تطبيق لتحسين الخط العربي للكبار. اكتب في دفترك، صوّر صفحتك، وقيّم نفسك وتابع تقدمك.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            Text("الإصدار 1.2", fontSize = 12.sp, color = Gray, modifier = Modifier.padding(top = 8.dp))
        }
        OutlinedButton(
            onClick = { confirm = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(26.dp),
        ) { Text("مسح كل التقدم", color = RuleRed, fontWeight = FontWeight.Bold) }
    }
}
