@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.masrofy.app

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import org.json.JSONArray
import org.json.JSONObject

data class Goal(
    val id: Long,
    val name: String,
    val target: Double,
    val saved: Double,
    val icon: String
)

private val GOAL_ICONS: List<Pair<String, ImageVector>> = listOf(
    "phone" to Ic.Phone,
    "plane" to Ic.Plane,
    "heart" to Ic.Heart,
    "home" to Ic.Home,
    "car" to Ic.Bus,
    "school" to Ic.Book,
    "gift" to Ic.Gift,
    "flag" to Ic.Flag
)

private fun goalIcon(key: String): ImageVector =
    GOAL_ICONS.firstOrNull { it.first == key }?.second ?: Ic.Flag

private fun goalColor(key: String): Color = when (key) {
    "phone" -> Color(0xFF4F6BDB)
    "plane" -> Color(0xFF2A9BD1)
    "heart" -> Color(0xFFD8512D)
    "home" -> Color(0xFF0FA968)
    "car" -> Color(0xFF3B82C4)
    "school" -> Color(0xFF5B8C2A)
    "gift" -> Color(0xFFD94A7E)
    else -> Color(0xFF8A63D2)
}

fun loadGoals(store: Store): List<Goal> {
    val out = mutableListOf<Goal>()
    try {
        val arr = JSONArray(store.prefs.getString("goals", "[]"))
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Goal(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    target = o.getDouble("target"),
                    saved = o.getDouble("saved"),
                    icon = o.optString("icon", "flag")
                )
            )
        }
    } catch (e: Exception) {
        // بيانات تالفة: نبدأ من الأول
    }
    return out
}

fun saveGoals(store: Store, list: List<Goal>) {
    val arr = JSONArray()
    list.forEach { g ->
        arr.put(
            JSONObject()
                .put("id", g.id)
                .put("name", g.name)
                .put("target", g.target)
                .put("saved", g.saved)
                .put("icon", g.icon)
        )
    }
    store.prefs.edit().putString("goals", arr.toString()).apply()
}

private fun progress(g: Goal): Float =
    if (g.target > 0) (g.saved / g.target).toFloat().coerceIn(0f, 1f) else 0f

/* ---------------- كارت الأهداف في الرئيسية ---------------- */

@Composable
fun GoalsHomeCard(goals: List<Goal>, onOpen: () -> Unit) {
    val pending = goals.filter { it.saved < it.target }
    val shown = (if (pending.isNotEmpty()) pending else goals).take(2)
    Surface(
        onClick = onOpen,
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
                Text("أهدافك", fontWeight = FontWeight.SemiBold)
                Text(
                    "عرض الكل",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            shown.forEach { g ->
                val tint = goalColor(g.icon)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(tint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(goalIcon(g.icon), contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(g.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${(progress(g) * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = tint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress(g) },
                            color = tint,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- شاشة الأهداف ---------------- */

@Composable
fun GoalsScreen(goals: List<Goal>, onBack: () -> Unit, onSave: (List<Goal>) -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf<Goal?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Goal?>(null) }
    var amountMode by remember { mutableStateOf("") }
    var amountGoal by remember { mutableStateOf<Goal?>(null) }
    var deleting by remember { mutableStateOf<Goal?>(null) }

    val totalSaved = goals.sumOf { it.saved }
    val totalTarget = goals.sumOf { it.target }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ScreenBar("أهداف التوفير", onBack) }
    ) { pad ->
        if (goals.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState("مفيش أهداف لسه", "حدد هدف (موبايل، سفر، طوارئ) وتابع تقدمك لحد ما توصله")
                Spacer(Modifier.height(16.dp))
                AddGoalButton { editing = null; showForm = true }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(goals, key = { it.id }) { g ->
                    GoalCard(g) { selected = g }
                }
                item { AddGoalButton { editing = null; showForm = true } }
            }
        }
    }

    selected?.let { g ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(g.name) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            amountGoal = g
                            amountMode = "add"
                            selected = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("إضافة مبلغ", modifier = Modifier.fillMaxWidth()) }
                    TextButton(
                        onClick = {
                            amountGoal = g
                            amountMode = "sub"
                            selected = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("سحب مبلغ", modifier = Modifier.fillMaxWidth()) }
                    TextButton(
                        onClick = {
                            editing = g
                            showForm = true
                            selected = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("تعديل الهدف", modifier = Modifier.fillMaxWidth()) }
                    TextButton(
                        onClick = {
                            deleting = g
                            selected = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "حذف الهدف",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { selected = null }) { Text("إغلاق") } }
        )
    }

    amountGoal?.let { g ->
        AmountDialog(
            title = if (amountMode == "add") "إضافة مبلغ لـ " + g.name else "سحب مبلغ من " + g.name,
            onDismiss = { amountGoal = null },
            onConfirm = { v ->
                val newSaved = if (amountMode == "add") g.saved + v else maxOf(0.0, g.saved - v)
                onSave(goals.map { if (it.id == g.id) it.copy(saved = newSaved) else it })
                if (amountMode == "add" && g.saved < g.target && newSaved >= g.target) {
                    Toast.makeText(context, "مبروك! وصلت لهدفك 🎉", Toast.LENGTH_LONG).show()
                }
                amountGoal = null
            }
        )
    }

    if (showForm) {
        GoalDialog(
            initial = editing,
            onDismiss = { showForm = false },
            onSave = { g ->
                val exists = goals.any { it.id == g.id }
                onSave(if (exists) goals.map { if (it.id == g.id) g else it } else goals + g)
                showForm = false
            }
        )
    }

    deleting?.let { g ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("حذف الهدف؟") },
            text = { Text(g.name) },
            confirmButton = {
                TextButton(onClick = {
                    onSave(goals.filter { it.id != g.id })
                    deleting = null
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun GoalCard(g: Goal, onClick: () -> Unit) {
    val done = g.target > 0 && g.saved >= g.target
    val pg = remember { Animatable(0f) }
    LaunchedEffect(g.saved, g.target) { pg.animateTo(progress(g), tween(800)) }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .appear()
            .clip(RoundedCornerShape(22.dp))
            .bounceClick(onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(goalIcon(g.icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(g.name, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "من " + money(g.saved) + " إلى " + money(g.target) + " ج.م",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LinearProgressIndicator(
                progress = { pg.value },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${(progress(g) * 100).toInt()}%",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (done) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Ic.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("تم تحقيق الهدف", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Text(
                        "متبقي " + money(g.target - g.saved) + " ج.م",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountDialog(title: String, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var text by remember { mutableStateOf("") }
    val value = parseNum(text)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { FormField(text, { text = it }, "المبلغ", keyboard = KeyboardType.Decimal) },
        confirmButton = {
            TextButton(
                enabled = value != null && value > 0,
                onClick = { if (value != null && value > 0) onConfirm(value) }
            ) { Text("تأكيد") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun GoalDialog(initial: Goal?, onDismiss: () -> Unit, onSave: (Goal) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var targetText by remember {
        mutableStateOf(if (initial != null) money(initial.target).replace(",", "") else "")
    }
    var savedText by remember {
        mutableStateOf(if (initial != null && initial.saved > 0) money(initial.saved).replace(",", "") else "")
    }
    var icon by remember { mutableStateOf(initial?.icon ?: "flag") }

    val target = parseNum(targetText)
    val saved = if (savedText.isBlank()) 0.0 else parseNum(savedText)
    val valid = name.isNotBlank() && target != null && target > 0 && saved != null && saved >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "هدف جديد" else "تعديل الهدف") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField(name, { name = it }, "اسم الهدف (مثلاً: موبايل جديد)")
                FormField(targetText, { targetText = it }, "المبلغ المطلوب", keyboard = KeyboardType.Decimal)
                FormField(savedText, { savedText = it }, "المدخر حالياً (اختياري)", keyboard = KeyboardType.Decimal)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GOAL_ICONS.forEach { pair ->
                        val tint = goalColor(pair.first)
                        val sel = icon == pair.first
                        Surface(
                            onClick = { icon = pair.first },
                            shape = RoundedCornerShape(14.dp),
                            color = if (sel) tint.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                Icon(pair.second, contentDescription = null, tint = tint)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    if (target != null && saved != null && target > 0 && saved >= 0 && name.isNotBlank()) {
                        onSave(Goal(initial?.id ?: System.currentTimeMillis(), name.trim(), target, saved, icon))
                    }
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}


@Composable
private fun AddGoalButton(onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        interactionSource = src,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .pressScale(src)
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Icon(Ic.Plus, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("إضافة هدف", fontWeight = FontWeight.ExtraBold)
    }
}
