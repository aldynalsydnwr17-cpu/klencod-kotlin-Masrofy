@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.masrofy.app

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.core.view.WindowCompat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
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
    val ref: String = "",
    val photo: String = ""
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
                        ref = o.optString("ref", ""),
                        photo = o.optString("photo", "")
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
                    .put("photo", t.photo)
            )
        }
        prefs.edit().putString("tx", arr.toString()).apply()
    }
}

enum class Cat(val key: String, val label: String, val icon: ImageVector, val color: Color) {
    FOOD("food", "أكل وشرب", Ic.Food, Color(0xFFF2A33A)),
    SHOPPING("shopping", "تسوق", Ic.Cart, Color(0xFFE0568A)),
    BILLS("bills", "فواتير", Ic.Receipt, Color(0xFF3B82C4)),
    TRANSPORT("transport", "مواصلات", Ic.Car, Color(0xFF0E9AA7)),
    FUN("fun", "ترفيه", Ic.Gamepad, Color(0xFF8A63D2)),
    HEALTH("health", "صحة", Ic.Heart, Color(0xFFD8512D)),
    EDUCATION("education", "تعليم", Ic.Book, Color(0xFF5B8C2A)),
    CAFE("cafe", "كافيه", Ic.Cafe, Color(0xFFA8741A)),
    GIFTS("gifts", "هدايا", Ic.Gift, Color(0xFFD94A7E)),
    PHONE("phone", "موبايل", Ic.Mobile, Color(0xFF4F6BDB)),
    TRAVEL("travel", "سفر", Ic.Plane, Color(0xFF2A9BD1)),
    OTHER("other", "أخرى", Ic.Dots, Color(0xFF6B7C76));

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
    primary = Color(0xFF0FA968),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F5EC),
    onPrimaryContainer = Color(0xFF0A5A3C),
    secondaryContainer = Color(0xFFE3F5EC),
    onSecondaryContainer = Color(0xFF0A5A3C),
    background = Color(0xFFF7FAF8),
    onBackground = Color(0xFF13201A),
    surface = Color.White,
    onSurface = Color(0xFF13201A),
    surfaceVariant = Color(0xFFE9EFEC),
    onSurfaceVariant = Color(0xFF5B6862),
    outline = Color(0xFF8A9B95),
    outlineVariant = Color(0xFFDCE5E0),
    error = Color(0xFFD8512D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2FD08A),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF16352A),
    onPrimaryContainer = Color(0xFFD5F0E7),
    secondaryContainer = Color(0xFF16352A),
    onSecondaryContainer = Color(0xFFD5F0E7),
    background = Color(0xFF0E1512),
    onBackground = Color(0xFFE7F0EC),
    surface = Color(0xFF17211D),
    onSurface = Color(0xFFE7F0EC),
    surfaceVariant = Color(0xFF22302B),
    onSurfaceVariant = Color(0xFF93A59E),
    outline = Color(0xFF6C7F78),
    outlineVariant = Color(0xFF243430),
    error = Color(0xFFFF7A57)
)

/** ألوان الهيدر الأخضر المتدرج. */
val HeaderBrush: Brush = Brush.linearGradient(listOf(Color(0xFF0FA968), Color(0xFF0B6B4B)))

@Composable
fun incomeColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFF4FD19A) else Color(0xFF0A8F5A)

@Composable
fun MasrofyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = remember { cairoTypography() },
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
        Reminders.schedule(applicationContext)
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
    var txSearch by remember { mutableStateOf(false) }
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<Tx?>(null) }
    var prefill by remember { mutableStateOf<Double?>(null) }
    var invScreen by remember { mutableStateOf<InvScreen?>(null) }
    var moreScreen by rememberSaveable { mutableStateOf("") }
    var showBiz by remember { mutableStateOf(false) }
    var goals by remember { mutableStateOf(loadGoals(store)) }
    var userName by remember { mutableStateOf(store.prefs.getString("user_name", "") ?: "") }
    var showName by remember { mutableStateOf(false) }
    var sheetIncome by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun reloadAll() {
        goals = loadGoals(store)
        userName = store.prefs.getString("user_name", "") ?: ""
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

    val darkTheme = isSystemInDarkTheme()
    val view = LocalView.current
    val greenTop = tab == 0 && moreScreen.isEmpty() && invScreen == null && !showSheet
    SideEffect {
        val w = (view.context as? android.app.Activity)?.window
        if (w != null) {
            // أيقونات شريط الحالة بيضا على الهيدر الأخضر بتاع الرئيسية، وغامقة في باقي الصفحات
            WindowCompat.getInsetsController(w, view).isAppearanceLightStatusBars = false
        }
    }

    val screen = invScreen
    if (showSheet) {
        BackHandler {
            showSheet = false
            prefill = null
        }
        AddSheet(
            initial = editingTx,
            prefillAmount = prefill,
            initialIncome = sheetIncome,
            onBack = {
                showSheet = false
                prefill = null
            },
            onSave = { tx ->
                val old = items.firstOrNull { it.id == tx.id }
                if (old != null && old.photo != tx.photo) Receipts.delete(context, old.photo)
                val exists = items.any { it.id == tx.id }
                items = (if (exists) items.map { if (it.id == tx.id) tx else it } else listOf(tx) + items)
                    .sortedByDescending { it.createdAt }
                store.save(items)
                showSheet = false
                prefill = null
            }
        )
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
    } else if (moreScreen.isNotEmpty()) {
        BackHandler { moreScreen = "" }
        when (moreScreen) {
            "calc" -> CalculatorScreen(
                onBack = { moreScreen = "" },
                onUseAmount = { v ->
                    prefill = v
                    editingTx = null
                    showSheet = true
                    sheetIncome = false
                    tab = 0
                    moreScreen = ""
                }
            )
            "goals" -> GoalsScreen(
                goals = goals,
                onBack = { moreScreen = "" },
                onSave = {
                    goals = it
                    saveGoals(store, it)
                }
            )
            "notif" -> NotificationsScreen(onBack = { moreScreen = "" })
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
            "invoices" -> {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        ScreenBar("الفواتير", { moreScreen = "" }) {
                            IconButton(onClick = { invScreen = InvScreen.Edit(null) }) {
                                Icon(Ic.Plus, contentDescription = "فاتورة جديدة", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                ) { pad ->
                    InvoicesScreen(
                        invoices = invoices,
                        biz = biz,
                        onOpen = { invScreen = InvScreen.Show(it.id) },
                        onEditBusiness = { showBiz = true },
                        modifier = Modifier.padding(pad)
                    )
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
            else -> moreScreen = ""
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            topBar = {
                if (tab != 0) {
                    ScreenBar(
                        when (tab) {
                            1 -> "العمليات"
                            2 -> "التحليلات"
                            else -> "المزيد"
                        },
                        { tab = 0 }
                    ) {
                        if (tab == 1) {
                            IconButton(onClick = {
                                txSearch = !txSearch
                            }) {
                                Icon(if (txSearch) Ic.Close else Ic.Search, contentDescription = "بحث", tint = Color.White)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                MasrofyBottomBar(
                    tab = tab,
                    onTab = { tab = it },
                    onCenter = {
                        editingTx = null
                        prefill = null
                        sheetIncome = false
                        showSheet = true
                    }
                )
            }
        ) { pad ->
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 24 }) togetherWith fadeOut(tween(120))
                },
                label = "tabs"
            ) { currentTab ->
            when (currentTab) {
                0 -> HomeScreen(
                    txs = items,
                    userName = userName,
                    onEditName = { showName = true },
                    onQuickAdd = { income ->
                        editingTx = null
                        prefill = null
                        sheetIncome = income
                        showSheet = true
                    },
                    onNewInvoice = { invScreen = InvScreen.Edit(null) },
                    onOpenCalc = { moreScreen = "calc" },
                    onOpenNotif = { moreScreen = "notif" },
                    onOpenMore = { moreScreen = it },
                    onSeeAll = { tab = 1 },
                    onEdit = { tx ->
                        editingTx = tx
                        prefill = null
                        showSheet = true
                    },
                    modifier = Modifier.padding(pad)
                )
                1 -> TransactionsScreen(
                    txs = items,
                    searching = txSearch,
                    monthOffset = monthOffset,
                    onMonthChange = { monthOffset = it },
                    onEdit = { tx ->
                        editingTx = tx
                        prefill = null
                        showSheet = true
                    },
                    onDelete = { tx ->
                        Receipts.delete(context, tx.photo)
                        items = items.filter { it.id != tx.id }
                        store.save(items)
                    },
                    modifier = Modifier.padding(pad)
                )
                2 -> StatsScreen(
                    txs = items,
                    monthOffset = monthOffset,
                    onMonthChange = { monthOffset = it },
                    catBudgets = catBudgets,
                    budget = budget,
                    onSetBudget = { value ->
                        budget = value
                        store.budget = value
                    },
                    modifier = Modifier.padding(pad)
                )
                else -> MoreScreen(
                    onOpen = { moreScreen = it },
                    modifier = Modifier.padding(pad)
                )
            }
            }
        }

        if (showName) {
            NameDialog(
                current = userName,
                onDismiss = { showName = false },
                onSave = {
                    userName = it
                    store.prefs.edit().putString("user_name", it).apply()
                    showName = false
                }
            )
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

/* ---------------- الشريط السفلي ---------------- */

@Composable
private fun MasrofyBottomBar(tab: Int, onTab: (Int) -> Unit, onCenter: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(92.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(66.dp)
                .shadow(10.dp, RectangleShape, clip = false)
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarItem("الرئيسية", Ic.Home, tab == 0, Modifier.weight(1f)) { onTab(0) }
            BarItem("العمليات", Ic.Receipt, tab == 1, Modifier.weight(1f)) { onTab(1) }
            Spacer(Modifier.weight(1f))
            BarItem("التحليلات", Ic.BarChart, tab == 2, Modifier.weight(1f)) { onTab(2) }
            BarItem("المزيد", Ic.More, tab == 3, Modifier.weight(1f)) { onTab(3) }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .bounceClick(onCenter)
                .size(64.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Ic.Plus,
                contentDescription = "إضافة",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun BarItem(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .fillMaxHeight()
            .bounceClick(onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val pop by animateFloatAsState(
            if (selected) 1.18f else 1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "tabpop"
        )
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = pop
                    scaleY = pop
                }
        )
        Text(
            label,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1
        )
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
            Icon(Ic.ChevRight, contentDescription = "الشهر السابق")
        }
        Text(monthLabel(offset), fontWeight = FontWeight.SemiBold)
        IconButton(onClick = { onChange(offset - 1) }, enabled = offset > 0) {
            Icon(Ic.ChevLeft, contentDescription = "الشهر التالي")
        }
    }
}

@Composable
fun HomeScreen(
    txs: List<Tx>,
    userName: String,
    onEditName: () -> Unit,
    onQuickAdd: (Boolean) -> Unit,
    onNewInvoice: () -> Unit,
    onOpenCalc: () -> Unit,
    onOpenNotif: () -> Unit,
    onOpenMore: (String) -> Unit,
    onSeeAll: () -> Unit,
    onEdit: (Tx) -> Unit,
    modifier: Modifier = Modifier
) {
    val income = txs.filter { it.isIncome }.sumOf { it.amount }
    val spent = txs.filter { !it.isIncome }.sumOf { it.amount }
    val recent = txs.sortedByDescending { it.createdAt }.take(5)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            HomeTop(
                userName = userName,
                onEditName = onEditName,
                onOpenNotif = onOpenNotif,
                balance = income - spent,
                onQuickAdd = onQuickAdd,
                onNewInvoice = onNewInvoice,
                onOpenCalc = onOpenCalc,
                onOpenMore = onOpenMore
            )
        }
        if (recent.isEmpty()) {
            item { EmptyState("لسه مفيش عمليات", "دوس على + وسجّل أول مصروف") }
        } else {
            item {
                TxGroupCard(recent, onEdit, null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("آخر العمليات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .bounceClick(onSeeAll)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Ic.ChevLeft, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                "عرض الكل",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/** كارت فيه مجموعة عمليات بينها خطوط فاصلة. */
@Composable
private fun TxGroupCard(
    list: List<Tx>,
    onClick: (Tx) -> Unit,
    onLong: ((Tx) -> Unit)?,
    header: (@Composable () -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            header?.invoke()
            list.forEachIndexed { i, tx ->
                if (i > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                }
                TxLine(tx, i, onClick = { onClick(tx) }, onLong = if (onLong != null) ({ onLong(tx) }) else null)
            }
        }
    }
}

@Composable
private fun TxLine(tx: Tx, index: Int, onClick: () -> Unit, onLong: (() -> Unit)?) {
    val cat = Cat.of(tx.category)
    val sign = if (tx.isIncome) "+" else "-"
    val tag = when {
        tx.ref.startsWith("inv:") -> " · من فاتورة"
        tx.ref.startsWith("rec:") -> " · متكررة"
        else -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appear(index * 60)
            .bounceCombined(onClick, onLong)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (tx.isIncome) Ic.Cash else cat.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tx.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (tx.photo.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Ic.Attach,
                        contentDescription = "مرفق صورة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(
                "${if (tx.isIncome) "دخل" else cat.label}$tag",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "\u200E$sign ${money(tx.amount)} ج.م",
            color = if (tx.isIncome) incomeColor() else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/* ---------------- العمليات ---------------- */

@Composable
fun TransactionsScreen(
    txs: List<Tx>,
    searching: Boolean,
    monthOffset: Int,
    onMonthChange: (Int) -> Unit,
    onEdit: (Tx) -> Unit,
    onDelete: (Tx) -> Unit,
    modifier: Modifier = Modifier
) {
    val range = remember(monthOffset) { monthRange(monthOffset) }
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("all") }
    var pendingDelete by remember { mutableStateOf<Tx?>(null) }
    val q = query.trim()

    val shown = txs.filter { it.createdAt >= range.first && it.createdAt < range.second }
        .filter { t ->
            when (filter) {
                "exp" -> !t.isIncome
                "income" -> t.isIncome
                "bills" -> !t.isIncome && t.category == Cat.BILLS.key
                else -> true
            }
        }
        .filter { t ->
            q.isEmpty() || t.title.contains(q, ignoreCase = true) ||
                (if (t.isIncome) "دخل" else Cat.of(t.category).label).contains(q, ignoreCase = true)
        }
        .sortedByDescending { it.createdAt }

    val dayFmt = remember { SimpleDateFormat("yyyyMMdd", Locale.US) }
    val labelFmt = remember { SimpleDateFormat("d MMMM yyyy", latinArabic) }
    val todayKey = dayFmt.format(Date())
    val yesterdayKey = dayFmt.format(Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000))
    val groups = shown.groupBy { dayFmt.format(Date(it.createdAt)) }.entries.toList()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (searching) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("ابحث في العمليات") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("all" to "الكل", "exp" to "مصروفات", "income" to "دخل", "bills" to "فواتير")) { f ->
                    val on = filter == f.first
                    Text(
                        f.second,
                        color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .bounceClick { filter = f.first }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }
        item { MonthNav(monthOffset, onMonthChange) }
        if (groups.isEmpty()) {
            item {
                EmptyState(
                    if (txs.isEmpty()) "لسه مفيش عمليات" else "مفيش عمليات هنا",
                    if (txs.isEmpty()) "دوس على + وسجّل أول مصروف" else "جرّب تغيّر الشهر أو الفلتر"
                )
            }
        } else {
            groups.forEach { g ->
                item(key = "h" + g.key) {
                    Text(
                        when (g.key) {
                            todayKey -> "النهاردة"
                            yesterdayKey -> "أمس"
                            else -> labelFmt.format(Date(g.value.first().createdAt))
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                item(key = "g" + g.key) { TxGroupCard(g.value, onEdit, { pendingDelete = it }) }
            }
            item {
                Text(
                    "اضغط مطوّلاً على أي عملية لحذفها",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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

/** يخلّي العنصر يتمدد على عرض الشاشة كله حتى لو الليست ليها padding. */
private fun Modifier.fullBleed(extra: androidx.compose.ui.unit.Dp): Modifier = layout { m, c ->
    val e = extra.roundToPx()
    val p = m.measure(c.copy(minWidth = c.maxWidth + 2 * e, maxWidth = c.maxWidth + 2 * e))
    layout(c.maxWidth, p.height) { p.place(-e, 0) }
}

@Composable
private fun HomeTop(
    userName: String,
    onEditName: () -> Unit,
    onOpenNotif: () -> Unit,
    balance: Double,
    onQuickAdd: (Boolean) -> Unit,
    onNewInvoice: () -> Unit,
    onOpenCalc: () -> Unit,
    onOpenMore: (String) -> Unit
) {
    Column(modifier = Modifier.fullBleed(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(HeaderBrush)
                .drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.07f),
                        radius = size.width * 0.42f,
                        center = Offset(size.width * 0.05f, -size.height * 0.1f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.width * 0.3f,
                        center = Offset(size.width * 0.98f, size.height * 0.55f)
                    )
                }
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 84.dp)
        ) {
            HomeHeader(userName, onEditName, onOpenNotif)
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .layout { m, c ->
                    val p = m.measure(c)
                    val up = 68.dp.roundToPx()
                    layout(p.width, p.height - up) { p.place(0, -up) }
                },
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            BalanceCard(balance, onQuickAdd, onNewInvoice, onOpenCalc)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("خدماتي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    ServiceItem("متكرر", Ic.Repeat, Modifier.weight(1f)) { onOpenMore("rec") }
                    ServiceItem("أهداف", Ic.Target, Modifier.weight(1f)) { onOpenMore("goals") }
                    ServiceItem("إيصال", Ic.Cam, Modifier.weight(1f)) { onQuickAdd(false) }
                    ServiceItem("عملاء", Ic.Users, Modifier.weight(1f)) { onOpenMore("clients") }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(userName: String, onEditName: () -> Unit, onOpenNotif: () -> Unit) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = if (hour < 12) "صباح الخير" else if (hour < 17) "نهارك سعيد" else "مساء الخير"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            onClick = onEditName,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.22f),
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (userName.isBlank()) "م" else userName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                greeting,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                if (userName.isBlank()) "أهلاً بيك" else "أهلاً يا " + userName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            onClick = onOpenNotif,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.18f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Ic.Bell,
                    contentDescription = "التنبيهات",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ServiceItem(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .bounceClick(onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .bounceClick(onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WeekCard(txs: List<Tx>) {
    val letters = remember { arrayOf("ح", "ن", "ث", "ر", "خ", "ج", "س") }
    val days = remember(txs) {
        val base = Calendar.getInstance()
        base.set(Calendar.HOUR_OF_DAY, 0)
        base.set(Calendar.MINUTE, 0)
        base.set(Calendar.SECOND, 0)
        base.set(Calendar.MILLISECOND, 0)
        val list = ArrayList<Pair<String, Double>>()
        for (i in 6 downTo 0) {
            val c = base.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            val start = c.timeInMillis
            val e = c.clone() as Calendar
            e.add(Calendar.DAY_OF_YEAR, 1)
            val end = e.timeInMillis
            val sum = txs.filter { !it.isIncome && it.createdAt >= start && it.createdAt < end }.sumOf { it.amount }
            list.add(Pair(letters[c.get(Calendar.DAY_OF_WEEK) - 1], sum))
        }
        list
    }
    val maxV = days.maxOf { it.second }
    val total = days.sumOf { it.second }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("المصروفات الأسبوعية", fontWeight = FontWeight.Bold)
                Text(
                    "صرفت " + money(total) + " ج.م",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEachIndexed { index, d ->
                    WeekBar(
                        label = d.first,
                        fraction = if (maxV > 0) (d.second / maxV).toFloat() else 0f,
                        today = index == days.size - 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekBar(label: String, fraction: Float, today: Boolean, modifier: Modifier) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0.05f, 1f),
        animationSpec = tween(700),
        label = "bar"
    )
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animated)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (today) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primaryContainer
                    )
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (today) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NameDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("اسمك") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 20) text = it },
                label = { Text("هنناديك بيه في الرئيسية") },
                singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { onSave(text.trim()) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun BalanceCard(
    balance: Double,
    onQuickAdd: (Boolean) -> Unit,
    onNewInvoice: () -> Unit,
    onOpenCalc: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            val shownBalance = remember { Animatable(0f) }
            LaunchedEffect(balance) { shownBalance.animateTo(balance.toFloat(), tween(900)) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Ic.Wallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
                Column {
                    Text(
                        "رصيدك الحالي",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            money(shownBalance.value.toDouble()),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "ج.م",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 7.dp)
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                QuickAction("مصروف", Ic.Up, Modifier.weight(1f)) { onQuickAdd(false) }
                QuickAction("دخل", Ic.Down, Modifier.weight(1f)) { onQuickAdd(true) }
                QuickAction("فاتورة", Ic.Receipt, Modifier.weight(1f)) { onNewInvoice() }
                QuickAction("حاسبة", Ic.Calc, Modifier.weight(1f)) { onOpenCalc() }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: Double, icon: ImageVector, tint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(value), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
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
                        Ic.Edit,
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
    val tint = MaterialTheme.colorScheme.primary
    val amountColor = if (tx.isIncome) incomeColor() else MaterialTheme.colorScheme.onSurface
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
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.isIncome) Ic.Cash else cat.icon,
                    contentDescription = null,
                    tint = tint
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tx.title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (tx.photo.isNotBlank()) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Ic.Attach,
                            contentDescription = "مرفق صورة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
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
                Ic.Wallet,
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
    budget: Double = 0.0,
    onSetBudget: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var editBudget by remember { mutableStateOf(false) }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    val range = remember(mode) {
        if (mode == 2) {
            val c = Calendar.getInstance()
            c.set(Calendar.MONTH, 0)
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val start = c.timeInMillis
            c.add(Calendar.YEAR, 1)
            Pair(start, c.timeInMillis)
        } else monthRange(mode)
    }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp)
            ) {
                listOf("هذا الشهر", "الشهر الماضي", "السنة").forEachIndexed { i, label ->
                    TypeButton(label, mode == i, Modifier.weight(1f)) { mode = i }
                }
            }
        }
        item { DonutCard(total, parts, if (mode == 2) "إجمالي السنة" else "إجمالي المصروفات") }
        item { WeekCard(txs) }
        item { BudgetCard(budget, total) { editBudget = true } }
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
            item {
                Text("أكثر التصنيفات إنفاقاً", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(parts, key = { it.first.key }) { part ->
                CategoryRow(part.first, part.second, total, largest, catBudgets[part.first.key] ?: 0.0)
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
}

@Composable
private fun DonutCard(total: Double, parts: List<Pair<Cat, Double>>, caption: String) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val grow = remember { Animatable(0f) }
    LaunchedEffect(total, parts.size) {
        grow.snapTo(0f)
        grow.animateTo(1f, tween(900))
    }
    val shown = parts.take(4)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                if (shown.isEmpty()) {
                    Text("مفيش مصروفات", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                shown.forEach { p ->
                    val pc = if (total > 0) (p.second / total * 100).roundToInt() else 0
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(p.first.color))
                        Text(p.first.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1)
                        Text("$pc%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    val stroke = 22.dp.toPx()
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
                                sweepAngle = ((sweep - 2f).coerceAtLeast(0.5f)) * grow.value,
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
                    Text(money(total * grow.value), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("ج.م", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(caption, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(cat.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                val pg = remember { Animatable(0f) }
                LaunchedEffect(value, largest) { pg.animateTo((value / largest).toFloat(), tween(800)) }
                LinearProgressIndicator(
                    progress = { pg.value },
                    color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
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
fun AddSheet(
    initial: Tx?,
    onSave: (Tx) -> Unit,
    onBack: () -> Unit,
    prefillAmount: Double? = null,
    initialIncome: Boolean = false
) {
    val context = LocalContext.current
    val newId = remember { System.currentTimeMillis() }
    var photo by remember { mutableStateOf(initial?.photo ?: "") }
    var isIncome by remember { mutableStateOf(initial?.isIncome ?: initialIncome) }
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

    val doSave = {
        if (amount != null && amount > 0) {
            val now = System.currentTimeMillis()
            val finalCat = if (isIncome) Cat.OTHER else cat
            val finalTitle = title.trim().ifBlank { if (isIncome) "دخل" else finalCat.label }
            onSave(
                Tx(
                    id = initial?.id ?: newId,
                    title = finalTitle,
                    amount = amount,
                    category = finalCat.key,
                    isIncome = isIncome,
                    createdAt = initial?.createdAt ?: now,
                    ref = initial?.ref ?: "",
                    photo = photo
                )
            )
        }
    }
    val press = { k: String ->
        amountText = when (k) {
            "⌫" -> amountText.dropLast(1)
            "." -> if (amountText.contains('.')) amountText
            else if (amountText.isEmpty()) "0." else "$amountText."
            else -> if (amountText.length >= 10) amountText
            else if (amountText == "0") k else amountText + k
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        ScreenBar(
            if (initial != null) "تعديل العملية" else if (isIncome) "إضافة دخل" else "إضافة مصروف",
            onBack
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .appear()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp)
            ) {
                TypeButton("مصروف", !isIncome, Modifier.weight(1f)) { isIncome = false }
                TypeButton("دخل", isIncome, Modifier.weight(1f)) { isIncome = true }
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            amountText.ifEmpty { "0" },
                            fontSize = 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (amountText.isEmpty()) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "ج.م",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("ملاحظة (اختياري)") },
                leadingIcon = { Icon(Ic.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (!isIncome) {
                Text("اختر التصنيف", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Cat.entries.chunked(4).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CatTile(item, cat == item) { cat = item }
                                }
                            }
                            repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    val src = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(3f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { r ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    r.forEach { k -> KeypadKey(k, Modifier.weight(1f)) { press(k) } }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KeypadKey("0", Modifier.weight(2f)) { press("0") }
                                KeypadKey(".", Modifier.weight(1f)) { press(".") }
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadKey("⌫", Modifier.fillMaxWidth()) { press("⌫") }
                            Button(
                                onClick = { doSave() },
                                enabled = canSave,
                                interactionSource = src,
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(4.dp),
                                modifier = Modifier
                                    .pressScale(src)
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    Text(
                                        if (initial != null) "حفظ التعديل" else if (isIncome) "تأكيد الدخل" else "تأكيد المصروف",
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PhotoSection(
                photo = photo,
                key = initial?.id ?: newId,
                onChange = { name ->
                    if (photo.isNotBlank() && photo != (initial?.photo ?: "")) Receipts.delete(context, photo)
                    photo = name
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CatTile(cat: Cat, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val pop by animateFloatAsState(
        if (selected) 1.04f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pop"
    )
    val bg by animateColorAsState(if (selected) scheme.primaryContainer else scheme.surface, label = "catbg")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pop
                scaleY = pop
            }
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(2.dp, if (selected) scheme.primary else Color.Transparent, RoundedCornerShape(18.dp))
            .bounceClick(onClick)
            .padding(vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            cat.icon,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Text(
            cat.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (selected) scheme.primary else scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun KeypadKey(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .bounceClick(onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        if (label == "⌫") {
            Icon(Ic.Backspace, contentDescription = "مسح", modifier = Modifier.size(24.dp))
        } else {
            Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
