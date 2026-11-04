@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.masrofy.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/* ---------------- بيانات ---------------- */

data class Tx(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    val isIncome: Boolean,
    val createdAt: Long,
    val ref: String = ""
)

class Store(context: Context) {
    val prefs = context.getSharedPreferences("masrofy", Context.MODE_PRIVATE)

    var budget: Double
        get() = prefs.getString("budget", "0")?.toDoubleOrNull() ?: 0.0
        set(value) {
            prefs.edit().putString("budget", value.toString()).apply()
        }

    fun load(): List<Tx> {
        val result = mutableListOf<Tx>()
        try {
            val arr = JSONArray(prefs.getString("tx", "[]"))
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                result.add(
                    Tx(
                        id = o.getLong("id"),
                        title = o.getString("title"),
                        amount = o.getDouble("amount"),
                        category = o.getString("category"),
                        isIncome = o.getBoolean("isIncome"),
                        createdAt = o.getLong("createdAt"),
                        ref = o.optString("ref", "")
                    )
                )
            }
        } catch (e: Exception) {
            // بيانات تالفة: نبدأ من الأول
        }
        return result.sortedByDescending { it.createdAt }
    }

    fun save(list: List<Tx>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(
                JSONObject()
                    .put("id", t.id)
                    .put("title", t.title)
                    .put("amount", t.amount)
                    .put("category", t.category)
                    .put("isIncome", t.isIncome)
                    .put("createdAt", t.createdAt)
                    .put("ref", t.ref)
            )
        }
        prefs.edit().putString("tx", arr.toString()).apply()
    }
}

enum class Cat(val key: String, val label: String, val icon: ImageVector, val color: Color) {
    FOOD("food", "أكل وشرب", Icons.Rounded.Restaurant, Color(0xFFF2A33A)),
    SHOPPING("shopping", "تسوق", Icons.Rounded.ShoppingBag, Color(0xFFE0568A)),
    BILLS("bills", "فواتير", Icons.Rounded.Receipt, Color(0xFF3B82C4)),
    TRANSPORT("transport", "مواصلات", Icons.Rounded.DirectionsBus, Color(0xFF0E9AA7)),
    FUN("fun", "ترفيه", Icons.Rounded.SportsEsports, Color(0xFF8A63D2)),
    HEALTH("health", "صحة", Icons.Rounded.Favorite, Color(0xFFD8512D)),
    EDUCATION("education", "تعليم", Icons.Rounded.School, Color(0xFF5B8C2A)),
    OTHER("other", "أخرى", Icons.Rounded.MoreHoriz, Color(0xFF6B7C76));

    companion object {
        fun of(key: String): Cat = entries.firstOrNull { it.key == key } ?: OTHER
    }
}

fun money(v: Double): String =
    if (v % 1.0 == 0.0) String.format(Locale.US, "%,.0f", v)
    else String.format(Locale.US, "%,.2f", v)

fun String.westernDigits(): String = map { c ->
    when (c) {
        in '٠'..'٩' -> '0' + (c - '٠')
        '٫' -> '.'
        else -> c
    }
}.joinToString("")

/** بداية ونهاية شهر معين. offset = 0 الشهر الحالي، 1 الشهر اللي فات، وهكذا. */
fun monthRange(offset: Int): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MONTH, -offset)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    return Pair(start, cal.timeInMillis)
}

val latinArabic: Locale = Locale.forLanguageTag("ar-u-nu-latn")

fun monthLabel(offset: Int): String =
    SimpleDateFormat("MMMM yyyy", latinArabic).format(Date(monthRange(offset).first))

/* ---------------- الثيم ---------------- */

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E7C66),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F0E7),
    onPrimaryContainer = Color(0xFF06382E),
    secondaryContainer = Color(0xFFD5F0E7),
    onSecondaryContainer = Color(0xFF06382E),
    background = Color(0xFFF4F7F5),
    onBackground = Color(0xFF12211D),
    surface = Color.White,
    onSurface = Color(0xFF12211D),
    surfaceVariant = Color(0xFFE6EDE9),
    onSurfaceVariant = Color(0xFF5E6F69),
    outline = Color(0xFF8A9B95),
    outlineVariant = Color(0xFFDCE5E0),
    error = Color(0xFFD8512D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FD1B0),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF17352E),
    onPrimaryContainer = Color(0xFFD5F0E7),
    secondaryContainer = Color(0xFF17352E),
    onSecondaryContainer = Color(0xFFD5F0E7),
    background = Color(0xFF0F1815),
    onBackground = Color(0xFFE7F0EC),
    surface = Color(0xFF17221E),
    onSurface = Color(0xFFE7F0EC),
    surfaceVariant = Color(0xFF22302B),
    onSurfaceVariant = Color(0xFF93A59E),
    outline = Color(0xFF6C7F78),
    outlineVariant = Color(0xFF243430),
    error = Color(0xFFFF7A57)
)

@Composable
fun incomeColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFF4FD19A) else Color(0xFF17935F)

@Composable
fun MasrofyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}

/* ---------------- النشاط الرئيسي ---------------- */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = Store(applicationContext)
        val invStore = InvoiceStore(applicationContext)
        setContent {
            MasrofyTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var splashDone by rememberSaveable { mutableStateOf(false) }
                    Crossfade(
                        targetState = splashDone,
                        animationSpec = tween(500),
                        label = "splash"
                    ) { done ->
                        if (done) {
                            MasrofyApp(store, invStore)
                        } else {
                            SplashScreen { splashDone = true }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MasrofyApp(store: Store, invStore: InvoiceStore) {
    var items by remember { mutableStateOf(applyRecurring(store)) }
    var budget by remember { mutableStateOf(store.budget) }
    var catBudgets by remember { mutableStateOf(loadCatBudgets(store)) }
    var invoices by remember { mutableStateOf(invStore.loadInvoices()) }
    var clients by remember { mutableStateOf(invStore.loadClients()) }
    var biz by remember { mutableStateOf(invStore.loadBusiness()) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<Tx?>(null) }
    var prefill by remember { mutableStateOf<Double?>(null) }
    var invScreen by remember { mutableStateOf<InvScreen?>(null) }
    var moreScreen by rememberSaveable { mutableStateOf("") }
    var showBiz by remember { mutableStateOf(false) }

    fun reloadAll() {
        items = applyRecurring(store)
        budget = store.budget
        catBudgets = loadCatBudgets(store)
        invoices = invStore.loadInvoices()
        clients = invStore.loadClients()
        biz = invStore.loadBusiness()
    }

    /** الفاتورة المدفوعة بتتسجل كدخل تلقائي، ولو اتلغى الدفع أو اتحذفت بيتشال الدخل. */
    fun syncIncome(inv: Invoice) {
        val ref = "inv:" + inv.id
        val existing = items.firstOrNull { it.ref == ref }
        items = if (inv.paid && inv.total > 0) {
            var newId = existing?.id ?: System.currentTimeMillis()
            if (existing == null) {
                while (items.any { it.id == newId }) newId++
            }
            val tx = Tx(
                id = newId,
                title = "فاتورة " + inv.number + " - " + inv.client,
                amount = inv.total,
                category = Cat.OTHER.key,
                isIncome = true,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                ref = ref
            )
            val merged = if (existing != null) {
                items.map { if (it.id == existing.id) tx else it }
            } else {
                listOf(tx) + items
            }
            merged.sortedByDescending { it.createdAt }
        } else {
            items.filter { it.ref != ref }
        }
        store.save(items)
    }

    val screen = invScreen
    if (moreScreen.isNotEmpty()) {
        BackHandler { moreScreen = "" }
        when (moreScreen) {
            "calc" -> CalculatorScreen(
                onBack = { moreScreen = "" },
                onUseAmount = { v ->
                    prefill = v
                    editingTx = null
                    showSheet = true
                    tab = 0
                    moreScreen = ""
                }
            )
            "rec" -> RecurringScreen(
                store = store,
                onBack = { moreScreen = "" },
                onChanged = { items = applyRecurring(store) }
            )
            "catb" -> CategoryBudgetsScreen(
                store = store,
                txs = items,
                onBack = { moreScreen = "" },
                onChanged = { catBudgets = loadCatBudgets(store) }
            )
            "clients" -> ClientsScreen(
                clients = clients,
                invoices = invoices,
                onBack = { moreScreen = "" },
                onSave = {
                    clients = it.sortedBy { c -> c.name }
                    invStore.saveClients(clients)
                }
            )
            "backup" -> BackupScreen(
                store = store,
                invStore = invStore,
                onBack = { moreScreen = "" },
                onRestored = {
                    reloadAll()
                    moreScreen = ""
                }
            )
            else -> moreScreen = ""
        }
    } else if (screen != null) {
        BackHandler {
            invScreen = if (screen is InvScreen.Edit && screen.id != null) InvScreen.Show(screen.id) else null
        }
        when (screen) {
            is InvScreen.Edit -> {
                val existing = invoices.firstOrNull { it.id == screen.id }
                InvoiceEditor(
                    existing = existing,
                    number = existing?.number ?: nextInvoiceNumber(invoices),
                    clients = clients,
                    onBack = {
                        invScreen = if (existing != null) InvScreen.Show(existing.id) else null
                    },
                    onSave = { inv ->
                        invoices = (invoices.filter { it.id != inv.id } + inv).sortedByDescending { it.date }
                        invStore.saveInvoices(invoices)
                        if (clients.none { it.name.equals(inv.client, ignoreCase = true) }) {
                            clients = (clients + Client(System.currentTimeMillis(), inv.client, inv.phone))
                                .sortedBy { it.name }
                            invStore.saveClients(clients)
                        } else if (inv.phone.isNotBlank()) {
                            clients = clients.map { c ->
                                if (c.name.equals(inv.client, ignoreCase = true) && c.phone.isBlank()) {
                                    c.copy(phone = inv.phone)
                                } else {
                                    c
                                }
                            }
                            invStore.saveClients(clients)
                        }
                        syncIncome(inv)
                        invScreen = InvScreen.Show(inv.id)
                    }
                )
            }
            is InvScreen.Show -> {
                val inv = invoices.firstOrNull { it.id == screen.id }
                if (inv == null) {
                    LaunchedEffect(screen) { invScreen = null }
                } else {
                    InvoiceView(
                        inv = inv,
                        biz = biz,
                        onBack = { invScreen = null },
                        onEdit = { invScreen = InvScreen.Edit(inv.id) },
                        onTogglePaid = {
                            val updated = inv.copy(paid = !inv.paid)
                            invoices = invoices.map { if (it.id == inv.id) updated else it }
                            invStore.saveInvoices(invoices)
                            syncIncome(updated)
                        },
                        onDelete = {
                            invoices = invoices.filter { it.id != inv.id }
                            invStore.saveInvoices(invoices)
                            syncIncome(inv.copy(paid = false))
                            invScreen = null
                        }
                    )
                }
            }
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                        label = { Text("الرئيسية") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Rounded.Receipt, contentDescription = null) },
                        label = { Text("الفواتير") }
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Icon(Icons.Rounded.PieChart, contentDescription = null) },
                        label = { Text("الإحصائيات") }
                    )
                    NavigationBarItem(
                        selected = tab == 3,
                        onClick = { tab = 3 },
                        icon = { Icon(Icons.Rounded.MoreHoriz, contentDescription = null) },
                        label = { Text("المزيد") }
                    )
                }
            },
            floatingActionButton = {
                if (tab == 1) {
                    ExtendedFloatingActionButton(
                        onClick = { invScreen = InvScreen.Edit(null) },
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                        text = { Text("فاتورة جديدة") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(18.dp)
                    )
                } else if (tab != 3) {
                    FloatingActionButton(
                        onClick = {
                            editingTx = null
                            prefill = null
                            showSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "إضافة عملية")
                    }
                }
            }
        ) { pad ->
            when (tab) {
                0 -> HomeScreen(
                    txs = items,
                    monthOffset = monthOffset,
                    onMonthChange = { monthOffset = it },
                    budget = budget,
                    catBudgets = catBudgets,
                    onSetBudget = { value ->
                        budget = value
                        store.budget = value
                    },
                    onEdit = { tx ->
                        editingTx = tx
                        prefill = null
                        showSheet = true
                    },
                    onDelete = { tx ->
                        items = items.filter { it.id != tx.id }
                        store.save(items)
                    },
                    modifier = Modifier.padding(pad)
                )
                1 -> InvoicesScreen(
                    invoices = invoices,
                    biz = biz,
                    onOpen = { invScreen = InvScreen.Show(it.id) },
                    onEditBusiness = { showBiz = true },
                    modifier = Modifier.padding(pad)
                )
                2 -> StatsScreen(
                    txs = items,
                    monthOffset = monthOffset,
                    onMonthChange = { monthOffset = it },
                    catBudgets = catBudgets,
                    modifier = Modifier.padding(pad)
                )
                else -> MoreScreen(
                    onOpen = { moreScreen = it },
                    modifier = Modifier.padding(pad)
                )
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    prefill = null
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                AddSheet(
                    initial = editingTx,
                    prefillAmount = prefill,
                    onSave = { tx ->
                        val exists = items.any { it.id == tx.id }
                        items = (if (exists) items.map { if (it.id == tx.id) tx else it } else listOf(tx) + items)
                            .sortedByDescending { it.createdAt }
                        store.save(items)
                        showSheet = false
                        prefill = null
                    }
                )
            }
        }

        if (showBiz) {
            BusinessDialog(
                current = biz,
                onDismiss = { showBiz = false },
                onSave = {
                    biz = it
                    invStore.saveBusiness(it)
                    showBiz = false
                }
            )
        }
    }
}

/* ---------------- الرئيسية ---------------- */

@Composable
fun MonthNav(offset: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { if (offset < 60) onChange(offset + 1) }) {
            Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "الشهر السابق")
        }
        Text(monthLabel(offset), fontWeight = FontWeight.SemiBold)
        IconButton(onClick = { onChange(offset - 1) }, enabled = offset > 0) {
            Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = "الشهر التالي")
        }
    }
}

@Composable
fun HomeScreen(
    txs: List<Tx>,
    monthOffset: Int,
    onMonthChange: (Int) -> Unit,
    budget: Double,
    catBudgets: Map<String, Double>,
    onSetBudget: (Double) -> Unit,
    onEdit: (Tx) -> Unit,
    onDelete: (Tx) -> Unit,
    modifier: Modifier = Modifier
) {
    val range = remember(monthOffset) { monthRange(monthOffset) }
    val income = txs.filter { it.isIncome }.sumOf { it.amount }
    val spent = txs.filter { !it.isIncome }.sumOf { it.amount }
    val monthTx = txs.filter { it.createdAt >= range.first && it.createdAt < range.second }
    val monthIncome = monthTx.filter { it.isIncome }.sumOf { it.amount }
    val monthSpent = monthTx.filter { !it.isIncome }.sumOf { it.amount }

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("all") }
    var pendingDelete by remember { mutableStateOf<Tx?>(null) }
    var editBudget by remember { mutableStateOf(false) }

    val overs = Cat.entries.filter { c ->
        val lim = catBudgets[c.key] ?: 0.0
        lim > 0 && monthTx.filter { !it.isIncome && it.category == c.key }.sumOf { it.amount } > lim
    }

    val q = query.trim()
    val shown = monthTx.filter { t ->
        val okFilter = when (filter) {
            "all" -> true
            "income" -> t.isIncome
            else -> !t.isIncome && t.category == filter
        }
        val okQuery = q.isEmpty() ||
            t.title.contains(q, ignoreCase = true) ||
            (if (t.isIncome) "دخل" else Cat.of(t.category).label).contains(q, ignoreCase = true)
        okFilter && okQuery
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text(
                    "أهلاً بيك",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "مصروفي",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item { MonthNav(monthOffset, onMonthChange) }
        item { BalanceCard(income - spent, monthIncome, monthSpent) }
        item { BudgetCard(budget, monthSpent) { editBudget = true } }
        if (overs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "عديت ميزانية: " + overs.joinToString("، ") { it.label },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("ابحث في العمليات") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "مسح البحث")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filter == "all",
                        onClick = { filter = "all" },
                        label = { Text("الكل") }
                    )
                }
                item {
                    FilterChip(
                        selected = filter == "income",
                        onClick = { filter = "income" },
                        label = { Text("دخل") }
                    )
                }
                items(Cat.entries.toList()) { c ->
                    FilterChip(
                        selected = filter == c.key,
                        onClick = { filter = c.key },
                        label = { Text(c.label) },
                        leadingIcon = {
                            Icon(c.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
        }
        item {
            Text(
                "العمليات (${shown.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (shown.isEmpty()) {
            item {
                EmptyState(
                    if (monthTx.isEmpty() && txs.isEmpty()) "لسه مفيش عمليات"
                    else if (monthTx.isEmpty()) "مفيش عمليات في الشهر ده"
                    else "مفيش نتائج",
                    if (txs.isEmpty()) "دوس على + وسجّل أول مصروف" else "جرّب تغيّر البحث أو الفلتر"
                )
            }
        } else {
            items(shown, key = { it.id }) { tx ->
                TxRow(tx, onClick = { onEdit(tx) }, onDeleteClick = { pendingDelete = tx })
            }
        }
    }

    if (editBudget) {
        BudgetDialog(
            current = budget,
            onDismiss = { editBudget = false },
            onSave = {
                onSetBudget(it)
                editBudget = false
            }
        )
    }

    pendingDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف العملية؟") },
            text = { Text(tx.title) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(tx)
                    pendingDelete = null
                }) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun BalanceCard(balance: Double, income: Double, spent: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0E7C66), Color(0xFF0A4F57))))
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = size.width * 0.38f,
                    center = Offset(size.width * 0.1f, -size.height * 0.05f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.width * 0.28f,
                    center = Offset(size.width * 0.95f, size.height)
                )
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "رصيدك الحالي",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelLarge
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(money(balance), color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text(
                "ج.م",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) { MiniStat("دخل الشهر", income, Icons.Rounded.ArrowDownward) }
            Box(modifier = Modifier.weight(1f)) { MiniStat("مصروف الشهر", spent, Icons.Rounded.ArrowUpward) }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: Double, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
            Text(
                money(value),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun BudgetCard(budget: Double, spent: Double, onClick: () -> Unit) {
    val hasBudget = budget > 0
    val ratio = if (hasBudget) (spent / budget).toFloat() else 0f
    val barColor = when {
        ratio >= 1f -> MaterialTheme.colorScheme.error
        ratio >= 0.8f -> Color(0xFFF2A33A)
        else -> MaterialTheme.colorScheme.primary
    }
    val left = budget - spent
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ميزانية الشهر", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (hasBudget) "تعديل" else "حدد ميزانية",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (hasBudget) {
                LinearProgressIndicator(
                    progress = { ratio.coerceIn(0f, 1f) },
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Text(
                    if (left >= 0) "صرفت ${money(spent)} من ${money(budget)}، باقي ${money(left)} ج.م"
                    else "عديت الميزانية بـ ${money(-left)} ج.م",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (left >= 0) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    "حدد مبلغ تصرفه في الشهر وتابع كام فاضل لك.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BudgetDialog(
    current: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
    title: String = "ميزانية الشهر"
) {
    var text by remember { mutableStateOf(if (current > 0) money(current).replace(",", "") else "") }
    val value = text.westernDigits().replace(',', '.').toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("المبلغ") },
                suffix = { Text("ج.م") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(value ?: 0.0) },
                enabled = text.isBlank() || value != null
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun TxRow(tx: Tx, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    val cat = Cat.of(tx.category)
    val tint = if (tx.isIncome) incomeColor() else cat.color
    val amountColor = if (tx.isIncome) incomeColor() else MaterialTheme.colorScheme.error
    val date = remember(tx.createdAt) { SimpleDateFormat("d MMM", latinArabic).format(Date(tx.createdAt)) }
    val sign = if (tx.isIncome) "+" else "−"
    val tag = when {
        tx.ref.startsWith("inv:") -> " · من فاتورة"
        tx.ref.startsWith("rec:") -> " · متكررة"
        else -> ""
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.isIncome) Icons.Rounded.AccountBalanceWallet else cat.icon,
                    contentDescription = null,
                    tint = tint
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${if (tx.isIncome) "دخل" else cat.label} · $date$tag",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "‎$sign${money(tx.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState(title: String, hint: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        }
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(
            hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ---------------- الإحصائيات ---------------- */

@Composable
fun StatsScreen(
    txs: List<Tx>,
    monthOffset: Int,
    onMonthChange: (Int) -> Unit,
    catBudgets: Map<String, Double> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val range = remember(monthOffset) { monthRange(monthOffset) }
    val monthItems = txs.filter { !it.isIncome && it.createdAt >= range.first && it.createdAt < range.second }
    val total = monthItems.sumOf { it.amount }
    val parts = monthItems
        .groupBy { Cat.of(it.category) }
        .map { entry -> entry.key to entry.value.sumOf { it.amount } }
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "الإحصائيات",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        item { MonthNav(monthOffset, onMonthChange) }
        item { DonutCard(total, parts) }
        if (parts.isEmpty()) {
            item {
                Text(
                    "مفيش مصروفات في الشهر ده.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            val largest = parts.first().second
            items(parts, key = { it.first.key }) { part ->
                CategoryRow(part.first, part.second, total, largest, catBudgets[part.first.key] ?: 0.0)
            }
        }
    }
}

@Composable
private fun DonutCard(total: Double, parts: List<Pair<Cat, Double>>) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(190.dp)) {
                val stroke = 26.dp.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(
                    color = track,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke)
                )
                if (total > 0) {
                    var startAngle = -90f
                    parts.forEach { part ->
                        val sweep = (part.second / total * 360.0).toFloat()
                        drawArc(
                            color = part.first.color,
                            startAngle = startAngle,
                            sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(stroke)
                        )
                        startAngle += sweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "مصروف الشهر",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(money(total), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(
                    "ج.م",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(cat: Cat, value: Double, total: Double, largest: Double, limit: Double) {
    val percent = if (total > 0) (value / total * 100).roundToInt() else 0
    val over = limit > 0 && value > limit
    Surface(
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
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
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
                        if (limit > 0) "$percent% · الحد ${money(limit)}" else "$percent%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (over) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { (value / largest).toFloat() },
                    color = if (over) MaterialTheme.colorScheme.error else cat.color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
            Text(money(value), fontWeight = FontWeight.Bold)
        }
    }
}

/* ---------------- إضافة وتعديل عملية ---------------- */

@Composable
fun AddSheet(initial: Tx?, onSave: (Tx) -> Unit, prefillAmount: Double? = null) {
    var isIncome by remember { mutableStateOf(initial?.isIncome ?: false) }
    var amountText by remember {
        mutableStateOf(
            if (initial != null) money(initial.amount).replace(",", "")
            else if (prefillAmount != null) money(prefillAmount).replace(",", "")
            else ""
        )
    }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var cat by remember { mutableStateOf(if (initial != null) Cat.of(initial.category) else Cat.FOOD) }

    val amount = amountText.westernDigits().replace(',', '.').toDoubleOrNull()
    val canSave = amount != null && amount > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (initial == null) "إضافة عملية" else "تعديل العملية",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            TypeButton("مصروف", !isIncome, Modifier.weight(1f)) { isIncome = false }
            TypeButton("دخل", isIncome, Modifier.weight(1f)) { isIncome = true }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("المبلغ") },
            suffix = { Text("ج.م") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.headlineSmall,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("الوصف (اختياري)") },
            placeholder = { Text("مثلاً: غدا مع الشباب") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (!isIncome) {
            Text(
                "الفئة",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

        Button(
            onClick = {
                if (amount != null && amount > 0) {
                    val now = System.currentTimeMillis()
                    val finalCat = if (isIncome) Cat.OTHER else cat
                    val finalTitle = title.trim().ifBlank { if (isIncome) "دخل" else finalCat.label }
                    onSave(
                        Tx(
                            id = initial?.id ?: now,
                            title = finalTitle,
                            amount = amount,
                            category = finalCat.key,
                            isIncome = isIncome,
                            createdAt = initial?.createdAt ?: now,
                            ref = initial?.ref ?: ""
                        )
                    )
                }
            },
            enabled = canSave,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                if (initial != null) "حفظ التعديل" else if (isIncome) "حفظ الدخل" else "حفظ المصروف",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TypeButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
