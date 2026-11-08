@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.masrofy.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/* ---------------- بيانات العمليات المتكررة والميزانيات ---------------- */

data class Recurring(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    val isIncome: Boolean,
    val day: Int,
    val active: Boolean,
    val lastKey: Int
)

fun monthKey(cal: Calendar): Int = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)

fun loadRecurring(store: Store): List<Recurring> {
    val out = mutableListOf<Recurring>()
    try {
        val arr = JSONArray(store.prefs.getString("recurring", "[]"))
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Recurring(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    amount = o.getDouble("amount"),
                    category = o.getString("category"),
                    isIncome = o.getBoolean("isIncome"),
                    day = o.getInt("day"),
                    active = o.optBoolean("active", true),
                    lastKey = o.optInt("lastKey", 0)
                )
            )
        }
    } catch (e: Exception) {
        // بيانات تالفة: نتجاهلها
    }
    return out
}

fun saveRecurring(store: Store, list: List<Recurring>) {
    val arr = JSONArray()
    list.forEach { r ->
        arr.put(
            JSONObject()
                .put("id", r.id)
                .put("title", r.title)
                .put("amount", r.amount)
                .put("category", r.category)
                .put("isIncome", r.isIncome)
                .put("day", r.day)
                .put("active", r.active)
                .put("lastKey", r.lastKey)
        )
    }
    store.prefs.edit().putString("recurring", arr.toString()).apply()
}

fun loadCatBudgets(store: Store): Map<String, Double> {
    val m = mutableMapOf<String, Double>()
    try {
        val o = JSONObject(store.prefs.getString("catBudgets", "{}"))
        val keys = o.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val v = o.optDouble(k, 0.0)
            if (v > 0) m[k] = v
        }
    } catch (e: Exception) {
        // بيانات تالفة: نتجاهلها
    }
    return m
}

fun saveCatBudgets(store: Store, map: Map<String, Double>) {
    val o = JSONObject()
    map.forEach { (k, v) -> if (v > 0) o.put(k, v) }
    store.prefs.edit().putString("catBudgets", o.toString()).apply()
}

/** يسجّل العمليات المتكررة المستحقة (لحد 12 شهر فاتت) ويرجع قائمة العمليات المحدثة. */
fun applyRecurring(store: Store): List<Tx> {
    var items = store.load()
    val recs = loadRecurring(store)
    if (recs.none { it.active }) return items

    val now = Calendar.getInstance()
    val nowKey = monthKey(now)
    val today = now.get(Calendar.DAY_OF_MONTH)
    val added = mutableListOf<Tx>()

    val updated = recs.map { r ->
        if (!r.active) {
            r
        } else {
            var last = r.lastKey
            var k = maxOf(r.lastKey + 1, nowKey - 11)
            while (k <= nowKey) {
                if (k < nowKey || today >= r.day) {
                    val ref = "rec:${r.id}:$k"
                    if (items.none { it.ref == ref } && added.none { it.ref == ref }) {
                        val c = Calendar.getInstance()
                        c.clear()
                        c.set(k / 12, k % 12, r.day, 9, 0, 0)
                        var id = System.currentTimeMillis()
                        while (items.any { it.id == id } || added.any { it.id == id }) id++
                        added.add(
                            Tx(
                                id = id,
                                title = r.title,
                                amount = r.amount,
                                category = r.category,
                                isIncome = r.isIncome,
                                createdAt = c.timeInMillis,
                                ref = ref
                            )
                        )
                    }
                    last = k
                }
                k++
            }
            r.copy(lastKey = last)
        }
    }

    if (added.isNotEmpty()) {
        items = (added + items).sortedByDescending { it.createdAt }
        store.save(items)
    }
    saveRecurring(store, updated)
    return items
}

/* ---------------- النسخ الاحتياطي ---------------- */

object Backup {
    private fun dump(p: SharedPreferences): JSONObject {
        val o = JSONObject()
        for ((k, v) in p.all) {
            if (v is String) o.put(k, v)
        }
        return o
    }

    private fun write(p: SharedPreferences, o: JSONObject) {
        val e = p.edit().clear()
        val keys = o.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            e.putString(k, o.getString(k))
        }
        e.apply()
    }

    fun create(context: Context, store: Store, inv: InvoiceStore): File {
        val root = JSONObject()
            .put("app", "masrofy")
            .put("version", 2)
            .put("created", System.currentTimeMillis())
            .put("main", dump(store.prefs))
            .put("invoices", dump(inv.prefs))
        val dir = File(context.cacheDir, "backups")
        dir.mkdirs()
        val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = File(dir, "masrofy-backup-$stamp.json")
        file.writeText(root.toString())
        return file
    }

    /** يرجع true لو الملف سليم واتسترجعت البيانات. */
    fun restore(text: String, store: Store, inv: InvoiceStore): Boolean {
        return try {
            val root = JSONObject(text)
            if (root.optString("app") != "masrofy") return false
            val main = root.getJSONObject("main")
            val invo = root.getJSONObject("invoices")
            JSONArray(main.optString("tx", "[]"))
            JSONArray(invo.optString("invoices", "[]"))
            write(store.prefs, main)
            write(inv.prefs, invo)
            true
        } catch (e: Exception) {
            false
        }
    }
}

fun shareBackup(context: Context, store: Store, inv: InvoiceStore): Boolean {
    return try {
        val file = Backup.create(context, store, inv)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "حفظ النسخة الاحتياطية"))
        true
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر إنشاء النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
        false
    }
}

/* ---------------- شاشة المزيد ---------------- */

@Composable
fun MoreScreen(onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "المزيد",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        item {
            MoreItem(
                Ic.Receipt, Color(0xFF0FA968), "الفواتير",
                "أنشئ فواتير وابعتها PDF وتابع المدفوع والمستحق"
            ) { onOpen("invoices") }
        }
        item {
            MoreItem(
                Ic.Calc, Color(0xFF0FA968), "الحاسبة",
                "حاسبة سريعة وتسجّل الناتج كمصروف"
            ) { onOpen("calc") }
        }
        item {
            MoreItem(
                Ic.Flag, Color(0xFF0FA968), "أهداف الادخار",
                "حدد هدف وتابع تقدمك لحد ما توصله"
            ) { onOpen("goals") }
        }
        item {
            MoreItem(
                Ic.Repeat, Color(0xFF0FA968), "العمليات المتكررة",
                "إيجار واشتراكات ومرتب بتتسجل لوحدها كل شهر"
            ) { onOpen("rec") }
        }
        item {
            MoreItem(
                Ic.Target, Color(0xFF0FA968), "ميزانيات الفئات",
                "حدد حد لكل فئة وهنبهك لو عديته"
            ) { onOpen("catb") }
        }
        item {
            MoreItem(
                Ic.Users, Color(0xFF0FA968), "العملاء",
                "عملاءك المحفوظين لإنشاء الفواتير أسرع"
            ) { onOpen("clients") }
        }
        item {
            MoreItem(
                Ic.Bell, Color(0xFF0FA968), "التنبيهات",
                "تذكير يومي بتسجيل مصروفاتك"
            ) { onOpen("notif") }
        }
        item {
            MoreItem(
                Ic.Cloud, Color(0xFF0FA968), "النسخ الاحتياطي",
                "صدّر بياناتك أو استرجعها لو غيّرت تليفونك"
            ) { onOpen("backup") }
        }
    }
}

@Composable
private fun MoreItem(icon: ImageVector, tint: Color, title: String, sub: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Ic.ChevLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ---------------- العمليات المتكررة ---------------- */

@Composable
fun RecurringScreen(store: Store, onBack: () -> Unit, onChanged: () -> Unit) {
    var list by remember { mutableStateOf(loadRecurring(store)) }
    var editing by remember { mutableStateOf<Recurring?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Recurring?>(null) }

    fun persist(newList: List<Recurring>) {
        saveRecurring(store, newList)
        onChanged()
        list = loadRecurring(store)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ScreenBar("العمليات المتكررة", onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editing = null
                    showForm = true
                },
                icon = { Icon(Ic.Plus, contentDescription = null) },
                text = { Text("عملية متكررة") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { pad ->
        if (list.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState(
                    "مفيش عمليات متكررة",
                    "ضيف الإيجار أو الاشتراكات أو المرتب وهتتسجل لوحدها كل شهر"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "العمليات دي بتتسجل تلقائي في يومها كل شهر أول ما تفتح التطبيق.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(list, key = { it.id }) { r ->
                    RecurringRow(
                        r,
                        onClick = {
                            editing = r
                            showForm = true
                        },
                        onToggle = { on -> persist(list.map { if (it.id == r.id) it.copy(active = on) else it }) },
                        onDelete = { deleting = r }
                    )
                }
            }
        }
    }

    if (showForm) {
        RecurringDialog(
            initial = editing,
            onDismiss = { showForm = false },
            onSave = { r ->
                val exists = list.any { it.id == r.id }
                persist(if (exists) list.map { if (it.id == r.id) r else it } else list + r)
                showForm = false
            }
        )
    }

    deleting?.let { r ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("حذف العملية المتكررة؟") },
            text = { Text("هتتشال من المتكررة بس، والعمليات اللي اتسجلت قبل كده هتفضل زي ما هي.") },
            confirmButton = {
                TextButton(onClick = {
                    persist(list.filter { it.id != r.id })
                    deleting = null
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun RecurringRow(r: Recurring, onClick: () -> Unit, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val cat = Cat.of(r.category)
    val tint = if (r.isIncome) incomeColor() else cat.color
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (r.isIncome) Ic.Wallet else cat.icon,
                    contentDescription = null,
                    tint = tint
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(r.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${if (r.isIncome) "دخل" else cat.label} · يوم ${r.day} كل شهر",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    (if (r.isIncome) "+" else "−") + money(r.amount) + " ج.م",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (r.isIncome) incomeColor() else MaterialTheme.colorScheme.error
                )
            }
            Switch(checked = r.active, onCheckedChange = onToggle)
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Ic.Trash,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RecurringDialog(initial: Recurring?, onDismiss: () -> Unit, onSave: (Recurring) -> Unit) {
    var isIncome by remember { mutableStateOf(initial?.isIncome ?: false) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var amountText by remember { mutableStateOf(if (initial != null) money(initial.amount).replace(",", "") else "") }
    var dayText by remember { mutableStateOf(initial?.day?.toString() ?: "1") }
    var cat by remember { mutableStateOf(if (initial != null) Cat.of(initial.category) else Cat.BILLS) }

    val amount = parseNum(amountText)
    val day = dayText.westernDigits().trim().toIntOrNull()
    val valid = amount != null && amount > 0 && day != null && day in 1..28

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "عملية متكررة جديدة" else "تعديل العملية المتكررة") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    TypeButton("مصروف", !isIncome, Modifier.weight(1f)) { isIncome = false }
                    TypeButton("دخل", isIncome, Modifier.weight(1f)) { isIncome = true }
                }
                FormField(title, { title = it }, "الوصف (مثلاً: إيجار الشقة)")
                FormField(amountText, { amountText = it }, "المبلغ", keyboard = KeyboardType.Decimal)
                FormField(dayText, { dayText = it }, "يوم الشهر (من 1 لـ 28)", keyboard = KeyboardType.Number)
                if (!isIncome) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Cat.entries.forEach { item ->
                            FilterChip(
                                selected = cat == item,
                                onClick = { cat = item },
                                label = { Text(item.label) },
                                leadingIcon = {
                                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    if (amount != null && day != null && amount > 0 && day in 1..28) {
                        val finalCat = if (isIncome) Cat.OTHER else cat
                        val cal = Calendar.getInstance()
                        val nowKey = monthKey(cal)
                        val today = cal.get(Calendar.DAY_OF_MONTH)
                        onSave(
                            Recurring(
                                id = initial?.id ?: System.currentTimeMillis(),
                                title = title.trim().ifBlank { if (isIncome) "دخل متكرر" else finalCat.label },
                                amount = amount,
                                category = finalCat.key,
                                isIncome = isIncome,
                                day = day,
                                active = initial?.active ?: true,
                                lastKey = initial?.lastKey ?: (if (day < today) nowKey else nowKey - 1)
                            )
                        )
                    }
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

/* ---------------- ميزانيات الفئات ---------------- */

@Composable
fun CategoryBudgetsScreen(store: Store, txs: List<Tx>, onBack: () -> Unit, onChanged: () -> Unit) {
    var map by remember { mutableStateOf(loadCatBudgets(store)) }
    var editing by remember { mutableStateOf<Cat?>(null) }
    val range = remember { monthRange(0) }
    val spentBy = txs
        .filter { !it.isIncome && it.createdAt >= range.first && it.createdAt < range.second }
        .groupBy { it.category }
        .mapValues { e -> e.value.sumOf { it.amount } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ScreenBar("ميزانيات الفئات", onBack) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "حدد أقصى مبلغ تصرفه على كل فئة في الشهر. دوس على الفئة لتعديل الحد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(Cat.entries.toList(), key = { it.key }) { c ->
                CatBudgetRow(c, spentBy[c.key] ?: 0.0, map[c.key] ?: 0.0) { editing = c }
            }
        }
    }

    editing?.let { c ->
        BudgetDialog(
            current = map[c.key] ?: 0.0,
            title = "ميزانية " + c.label,
            onDismiss = { editing = null },
            onSave = { v ->
                val m = map.toMutableMap()
                if (v > 0) m[c.key] = v else m.remove(c.key)
                map = m
                saveCatBudgets(store, m)
                onChanged()
                editing = null
            }
        )
    }
}

@Composable
private fun CatBudgetRow(cat: Cat, spent: Double, limit: Double, onClick: () -> Unit) {
    val has = limit > 0
    val ratio = if (has) (spent / limit).toFloat() else 0f
    val barColor = when {
        ratio >= 1f -> MaterialTheme.colorScheme.error
        ratio >= 0.8f -> Color(0xFFF2A33A)
        else -> cat.color
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(cat.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(cat.icon, contentDescription = null, tint = cat.color)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(cat.label, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (has) "${money(spent)} / ${money(limit)}" else "تحديد حد",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (has && ratio >= 1f) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (has) {
                    LinearProgressIndicator(
                        progress = { ratio.coerceIn(0f, 1f) },
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                } else {
                    Text(
                        "مفيش حد لسه",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ---------------- العملاء ---------------- */

@Composable
fun ClientsScreen(
    clients: List<Client>,
    invoices: List<Invoice>,
    onBack: () -> Unit,
    onSave: (List<Client>) -> Unit
) {
    var editing by remember { mutableStateOf<Client?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Client?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ScreenBar("العملاء", onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editing = null
                    showForm = true
                },
                icon = { Icon(Ic.Plus, contentDescription = null) },
                text = { Text("عميل جديد") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { pad ->
        if (clients.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState(
                    "مفيش عملاء محفوظين",
                    "أي عميل تعمله فاتورة هيتحفظ هنا لوحده، أو ضيفه بإيدك"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(clients, key = { it.id }) { c ->
                    val mine = invoices.filter { it.client.equals(c.name, ignoreCase = true) }
                    ClientRow(
                        c,
                        count = mine.size,
                        total = mine.sumOf { it.total },
                        onClick = {
                            editing = c
                            showForm = true
                        },
                        onDelete = { deleting = c }
                    )
                }
            }
        }
    }

    if (showForm) {
        ClientDialog(
            initial = editing,
            onDismiss = { showForm = false },
            onSave = { c ->
                val exists = clients.any { it.id == c.id }
                onSave(if (exists) clients.map { if (it.id == c.id) c else it } else clients + c)
                showForm = false
            }
        )
    }

    deleting?.let { c ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("حذف العميل؟") },
            text = { Text(c.name + "\nالفواتير القديمة هتفضل زي ما هي.") },
            confirmButton = {
                TextButton(onClick = {
                    onSave(clients.filter { it.id != c.id })
                    deleting = null
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun ClientRow(c: Client, count: Int, total: Double, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    c.name.take(1),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(c.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (c.phone.isBlank()) "بدون رقم" else c.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (count > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(money(total) + " ج.م", fontWeight = FontWeight.Bold)
                    Text(
                        "$count فاتورة",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Ic.Trash,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ClientDialog(initial: Client?, onDismiss: () -> Unit, onSave: (Client) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "عميل جديد" else "تعديل العميل") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField(name, { name = it }, "اسم العميل")
                FormField(phone, { phone = it }, "رقم التليفون (اختياري)", keyboard = KeyboardType.Phone)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(Client(initial?.id ?: System.currentTimeMillis(), name.trim(), phone.trim()))
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

/* ---------------- النسخ الاحتياطي ---------------- */

@Composable
fun BackupScreen(store: Store, invStore: InvoiceStore, onBack: () -> Unit, onRestored: () -> Unit) {
    val context = LocalContext.current
    var last by remember { mutableStateOf(store.prefs.getString("last_backup", "")?.toLongOrNull() ?: 0L) }
    var pendingText by remember { mutableStateOf<String?>(null) }
    val txCount = remember { store.load().size }
    val invCount = remember { invStore.loadInvoices().size }
    val clientCount = remember { invStore.loadClients().size }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = try {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            } catch (e: Exception) {
                null
            }
            if (text == null) {
                Toast.makeText(context, "تعذر قراءة الملف", Toast.LENGTH_SHORT).show()
            } else {
                pendingText = text
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ScreenBar("النسخ الاحتياطي", onBack) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "بياناتك على تليفونك",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "$txCount عملية · $invCount فاتورة · $clientCount عميل",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        if (last > 0L) "آخر نسخة: " +
                            SimpleDateFormat("d MMMM yyyy", latinArabic).format(Date(last))
                        else "لسه ماعملتش نسخة احتياطية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("حفظ نسخة", fontWeight = FontWeight.Bold)
                    Text(
                        "هنعمل ملف فيه كل بياناتك (من غير صور الإيصالات)، ابعته لنفسك على واتساب أو احفظه في درايف.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            if (shareBackup(context, store, invStore)) {
                                val now = System.currentTimeMillis()
                                store.prefs.edit().putString("last_backup", now.toString()).apply()
                                last = now
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Ic.Upload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("تصدير نسخة احتياطية", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("استرجاع نسخة", fontWeight = FontWeight.Bold)
                    Text(
                        "اختار ملف النسخة اللي حفظته قبل كده. بياناتك الحالية هتتستبدل بيها.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = { picker.launch(arrayOf("*/*")) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Ic.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("استرجاع من ملف", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    pendingText?.let { text ->
        AlertDialog(
            onDismissRequest = { pendingText = null },
            title = { Text("استبدال البيانات؟") },
            text = { Text("كل العمليات والفواتير الحالية هتتمسح وتتحط مكانها بيانات النسخة.") },
            confirmButton = {
                TextButton(onClick = {
                    val ok = Backup.restore(text, store, invStore)
                    pendingText = null
                    if (ok) {
                        Toast.makeText(context, "تم استرجاع البيانات", Toast.LENGTH_SHORT).show()
                        onRestored()
                    } else {
                        Toast.makeText(context, "الملف ده مش نسخة مصروفي صحيحة", Toast.LENGTH_LONG).show()
                    }
                }) { Text("استرجاع") }
            },
            dismissButton = { TextButton(onClick = { pendingText = null }) { Text("إلغاء") } }
        )
    }
}
