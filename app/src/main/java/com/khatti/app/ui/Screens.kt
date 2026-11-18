package com.khatti.app.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.khatti.app.data.*
import java.io.File

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("الرئيسية", Icons.Filled.Home),
    Progress("تقدمي", Icons.Filled.BarChart),
    Library("المكتبة", Icons.Filled.MenuBook),
}
private enum class Flow { Drill, Capture, Result }

@Composable
fun KhattiApp() {
    val ctx = LocalContext.current
    val store = remember { Store(ctx) }
    var tab by remember { mutableStateOf(Tab.Home) }
    var flow by remember { mutableStateOf<Flow?>(null) }
    var photo by remember { mutableStateOf<File?>(null) }
    var version by remember { mutableIntStateOf(0) } // لتحديث الشاشات بعد الحفظ

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (flow == null) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.values().forEach {
                    NavigationBarItem(
                        selected = tab == it, onClick = { tab = it },
                        icon = { Icon(it.icon, null) }, label = { Text(it.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Amber, selectedTextColor = Amber,
                            indicatorColor = Color.Transparent),
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (flow) {
                Flow.Drill -> DrillScreen(store.drillIndex, onBack = { flow = null }, onNext = { flow = Flow.Capture })
                Flow.Capture -> CaptureScreen(store, onBack = { flow = Flow.Drill },
                    onCaptured = { photo = it; flow = Flow.Result })
                Flow.Result -> ResultScreen(store, photo, onDone = { failed ->
                    store.recordPractice(failed, DRILLS[store.drillIndex].letter)
                    store.drillIndex = store.drillIndex + 1
                    version++; flow = null
                })
                null -> key(version) {
                    when (tab) {
                        Tab.Home -> HomeScreen(store) { flow = Flow.Drill }
                        Tab.Progress -> ProgressScreen(store)
                        Tab.Library -> LibraryScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun Title(text: String) =
    Text(text, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

@Composable
private fun PrimaryButton(text: String, color: Color = Amber, enabled: Boolean = true, onClick: () -> Unit) =
    Button(onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }

@Composable
private fun HomeScreen(store: Store, onStart: () -> Unit) {
    val drill = DRILLS[store.drillIndex]
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp))
        Text("خطّي", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("اكتب أجمل .. بثقة أكبر", color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Card(shape = CardShape, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalFireDepartment, null, tint = Amber, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("سلسلة اليوم", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${store.streak()} يومًا", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        Card(shape = CardShape, colors = CardDefaults.cardColors(Ink), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("خط اليوم", color = Color.White.copy(alpha = .7f))
                Text("تدريب على ${drill.title}", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text("مع كلمات: " + drill.words.joinToString("، "), color = Color.White.copy(alpha = .8f))
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("بدء التدريب", onClick = onStart)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) = Row(
    Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onBack) { Icon(Icons.Filled.ArrowForward, "رجوع") }
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    Spacer(Modifier.size(48.dp))
}

@Composable
private fun GuideLines(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val dash = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
        fun line(y: Float, c: Color, w: Float, d: Boolean = false) =
            drawLine(c, Offset(0f, y), Offset(size.width, y), w, pathEffect = if (d) dash else null)
        line(size.height * .20f, Gray, 2f, true)
        line(size.height * .45f, Gray, 2f, true)
        line(size.height * .70f, RuleRed, 4f)
        line(size.height * .92f, Gray, 2f, true)
    }
}

@Composable
private fun DrillScreen(index: Int, onBack: () -> Unit, onNext: () -> Unit) {
    val d = DRILLS[index]
    var wordIdx by remember { mutableIntStateOf(-1) } // -1 = الحرف نفسه
    val shown = if (wordIdx < 0) d.letter else d.words[wordIdx]
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        TopBar("تدريب اليوم: ${d.title}", onBack)
        Box(Modifier.fillMaxWidth().height(260.dp).clip(CardShape).background(MaterialTheme.colorScheme.surface)) {
            GuideLines(Modifier.fillMaxSize())
            Text(shown, fontSize = if (wordIdx < 0) 120.sp else 80.sp, fontFamily = NaskhFont,
                fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = wordIdx < 0, onClick = { wordIdx = -1 }, label = { Text("الحرف") })
            d.words.forEachIndexed { i, w -> FilterChip(selected = wordIdx == i, onClick = { wordIdx = i }, label = { Text(w) }) }
        }
        Spacer(Modifier.height(16.dp))
        Card(shape = CardShape, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Lightbulb, null, tint = Amber)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("نصيحة", fontWeight = FontWeight.Bold)
                    Text(d.tip)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("اكتب الحرف والكلمات في دفترك على سطر كامل لكل واحد، ثم اضغط التالي لتصوير الصفحة.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
        PrimaryButton("التالي: صوّر صفحتك", Ink, onClick = onNext)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CaptureScreen(store: Store, onBack: () -> Unit, onCaptured: (File) -> Unit) {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<File?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pending
        if (ok && f != null) onCaptured(f) else f?.delete()
    }
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar("صوّر صفحتك", onBack)
        Box(Modifier.fillMaxWidth().weight(1f).clip(CardShape).background(Color(0xFF1B1B1B)), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize(0.85f)) {
                val s = 6f; val c = Color.White; val l = 60f
                listOf(Offset(0f, 0f) to (1f to 1f), Offset(size.width, 0f) to (-1f to 1f),
                    Offset(0f, size.height) to (1f to -1f), Offset(size.width, size.height) to (-1f to -1f)).forEach { (o, d) ->
                    drawLine(c, o, Offset(o.x + d.first * l, o.y), s)
                    drawLine(c, o, Offset(o.x, o.y + d.second * l), s)
                }
            }
            Text("صوّر الصفحة بوضوح وتأكد من الإضاءة الجيدة", color = Color.White, textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp))
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton("صوّر صفحتك") {
            val f = store.newPhotoFile(); pending = f
            launcher.launch(FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", f))
        }
    }
}

@Composable
private fun PhotoThumb(file: File?, label: String, modifier: Modifier = Modifier) {
    val bmp = remember(file) {
        file?.takeIf { it.exists() }?.let {
            BitmapFactory.decodeFile(it.path, BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap()
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center) {
            if (bmp != null) Image(bmp, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Text("لا توجد صورة", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ResultScreen(store: Store, current: File?, onDone: (failed: Boolean) -> Unit) {
    val checks = listOf("النقط في أماكنها الصحيحة", "حجم الحروف متقارب", "الالتزام بالسطر")
    val state = remember { mutableStateListOf(false, false, false) }
    val photos = remember { store.photos() }
    val before = photos.filter { it != current }.lastOrNull()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Title("قيّم صفحتك بنفسك")
        Text("قارن خطك بالنموذج بصراحة، وعلّم على اللي حققته:", color = MaterialTheme.colorScheme.onSurfaceVariant)
        checks.forEachIndexed { i, t ->
            Card(shape = CardShape, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().clickable { state[i] = !state[i] }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(state[i], { state[i] = it }, colors = CheckboxDefaults.colors(checkedColor = Good))
                    Text(t, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PhotoThumb(before, "قبل", Modifier.weight(1f))
            PhotoThumb(current, "بعد", Modifier.weight(1f))
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("حفظ ومواصلة التدريب", Ink) { onDone(state.any { !it }) }
    }
}

@Composable
private fun ProgressScreen(store: Store) {
    val counts = store.weekCounts()
    val weak = store.weakLetters()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Title("تقدمي")
        Card(shape = CardShape, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("تدريبات هذا الأسبوع", style = MaterialTheme.typography.titleMedium)
                val max = maxOf(1, counts.max())
                Row(Modifier.fillMaxWidth().height(160.dp).padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    counts.forEachIndexed { i, c ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            Box(Modifier.width(22.dp).height((100.dp * c / max).coerceAtLeast(4.dp)).clip(RoundedCornerShape(6.dp)).background(if (c > 0) Amber else Gray.copy(alpha = .4f)))
                            Text(WEEK_DAYS[i].take(3), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        Card(shape = CardShape, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, null, tint = Amber)
                Spacer(Modifier.width(8.dp))
                Text("أعلى سلسلة: ${store.bestStreak()} يومًا", style = MaterialTheme.typography.titleMedium)
            }
        }
        Text("الحروف الأكثر احتياجًا للتدريب", style = MaterialTheme.typography.titleMedium)
        if (weak.isEmpty()) Text("لسه مفيش بيانات، ابدأ أول تدريب.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            weak.forEach {
                Box(Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 26.sp, fontFamily = NaskhFont, color = Ink)
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Title("مكتبة الكلمات") }
        items(LIBRARY) { (topic, words) ->
            var open by remember { mutableStateOf(false) }
            Card(shape = CardShape, colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().clickable { open = !open }) {
                Column(Modifier.padding(16.dp)) {
                    Text(topic, style = MaterialTheme.typography.titleMedium)
                    Text("${words.size} كلمة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (open) words.forEach {
                        Text(it, fontSize = 26.sp, fontFamily = NaskhFont, modifier = Modifier.padding(top = 10.dp))
                        HorizontalDivider(color = RuleRed.copy(alpha = .5f))
                    }
                }
            }
        }
    }
}
