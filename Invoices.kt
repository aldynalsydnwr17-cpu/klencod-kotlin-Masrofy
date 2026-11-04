@file:OptIn(ExperimentalMaterial3Api::class)

package com.masrofy.app

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

/* ---------------- بيانات الفواتير ---------------- */

data class InvItem(val name: String, val qty: Double, val price: Double) {
    val total: Double get() = qty * price
}

data class Invoice(
    val id: Long,
    val number: String,
    val client: String,
    val phone: String,
    val date: Long,
    val items: List<InvItem>,
    val discount: Double,
    val taxPercent: Double,
    val notes: String,
    val paid: Boolean
) {
    val subtotal: Double get() = items.sumOf { it.total }
    val taxable: Double get() = (subtotal - discount).coerceAtLeast(0.0)
    val tax: Double get() = taxable * taxPercent / 100.0
    val total: Double get() = taxable + tax
}

data class Business(val name: String, val phone: String, val address: String)

data class Client(val id: Long, val name: String, val phone: String)

sealed class InvScreen {
    data class Edit(val id: Long?) : InvScreen()
    data class Show(val id: Long) : InvScreen()
}

class InvoiceStore(context: Context) {
    val prefs = context.getSharedPreferences("masrofy_invoices", Context.MODE_PRIVATE)

    fun loadClients(): List<Client> {
        val out = mutableListOf<Client>()
        try {
            val arr = JSONArray(prefs.getString("clients", "[]"))
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(Client(o.getLong("id"), o.getString("name"), o.optString("phone", "")))
            }
        } catch (e: Exception) {
            // بيانات تالفة: نبدأ من الأول
        }
        return out.sortedBy { it.name }
    }

    fun saveClients(list: List<Client>) {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(JSONObject().put("id", c.id).put("name", c.name).put("phone", c.phone))
        }
        prefs.edit().putString("clients", arr.toString()).apply()
    }

    fun loadInvoices(): List<Invoice> {
        val result = mutableListOf<Invoice>()
        try {
            val arr = JSONArray(prefs.getString("invoices", "[]"))
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val itemsArr = o.getJSONArray("items")
                val items = mutableListOf<InvItem>()
                for (j in 0 until itemsArr.length()) {
                    val io = itemsArr.getJSONObject(j)
                    items.add(InvItem(io.getString("name"), io.getDouble("qty"), io.getDouble("price")))
                }
                result.add(
                    Invoice(
                        id = o.getLong("id"),
                        number = o.getString("number"),
                        client = o.getString("client"),
                        phone = o.optString("phone", ""),
                        date = o.getLong("date"),
                        items = items,
                        discount = o.optDouble("discount", 0.0),
                        taxPercent = o.optDouble("taxPercent", 0.0),
                        notes = o.optString("notes", ""),
                        paid = o.optBoolean("paid", false)
                    )
                )
            }
        } catch (e: Exception) {
            // بيانات تالفة: نبدأ من الأول
        }
        return result.sortedByDescending { it.date }
    }

    fun saveInvoices(list: List<Invoice>) {
        val arr = JSONArray()
        list.forEach { inv ->
            val itemsArr = JSONArray()
            inv.items.forEach { item ->
                itemsArr.put(
                    JSONObject().put("name", item.name).put("qty", item.qty).put("price", item.price)
                )
            }
            arr.put(
                JSONObject()
                    .put("id", inv.id)
                    .put("number", inv.number)
                    .put("client", inv.client)
                    .put("phone", inv.phone)
                    .put("date", inv.date)
                    .put("items", itemsArr)
                    .put("discount", inv.discount)
                    .put("taxPercent", inv.taxPercent)
                    .put("notes", inv.notes)
                    .put("paid", inv.paid)
            )
        }
        prefs.edit().putString("invoices", arr.toString()).apply()
    }

    fun loadBusiness(): Business = Business(
        prefs.getString("biz_name", "") ?: "",
        prefs.getString("biz_phone", "") ?: "",
        prefs.getString("biz_address", "") ?: ""
    )

    fun saveBusiness(b: Business) {
        prefs.edit()
            .putString("biz_name", b.name)
            .putString("biz_phone", b.phone)
            .putString("biz_address", b.address)
            .apply()
    }
}

fun nextInvoiceNumber(list: List<Invoice>): String {
    val max = list.maxOfOrNull { it.number.removePrefix("INV-").toIntOrNull() ?: 0 } ?: 0
    return "INV-" + (max + 1).toString().padStart(4, '0')
}

fun parseNum(s: String): Double? = s.westernDigits().replace(',', '.').trim().toDoubleOrNull()

private fun plainNum(v: Double): String = money(v).replace(",", "")

private fun invDate(ts: Long): String =
    SimpleDateFormat("d MMMM yyyy", latinArabic).format(Date(ts))

private val Amber = Color(0xFFF2A33A)

/* ---------------- قائمة الفواتير ---------------- */

@Composable
fun InvoicesScreen(
    invoices: List<Invoice>,
    biz: Business,
    onOpen: (Invoice) -> Unit,
    onEditBusiness: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unpaid = invoices.filter { !it.paid }
    val unpaidTotal = unpaid.sumOf { it.total }
    val paidTotal = invoices.filter { it.paid }.sumOf { it.total }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "الفواتير",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (biz.name.isBlank()) "ضيف بيانات نشاطك عشان تظهر على الفاتورة" else biz.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onEditBusiness) {
                    Icon(Icons.Rounded.Edit, contentDescription = "بيانات النشاط")
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0E7C66), Color(0xFF0A4F57))))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "مستحق لك",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(money(unpaidTotal), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "ج.م",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryPill("غير مدفوعة", "${unpaid.size} فاتورة", Modifier.weight(1f))
                    SummaryPill("اتحصّل", money(paidTotal) + " ج.م", Modifier.weight(1f))
                }
            }
        }
        if (invoices.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
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
                            Icons.Rounded.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Text("لسه مفيش فواتير", fontWeight = FontWeight.SemiBold)
                    Text(
                        "دوس على «فاتورة جديدة» وجهّز أول فاتورة وابعتها PDF.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(invoices, key = { it.id }) { inv -> InvoiceCard(inv) { onOpen(inv) } }
        }
    }
}

@Composable
private fun SummaryPill(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun StatusChip(paid: Boolean) {
    val color = if (paid) incomeColor() else Amber
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.16f)) {
        Text(
            if (paid) "مدفوعة" else "غير مدفوعة",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun InvoiceCard(inv: Invoice, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(inv.client, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${inv.number} · ${invDate(inv.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("‎" + money(inv.total), fontWeight = FontWeight.Bold)
                StatusChip(inv.paid)
            }
        }
    }
}

/* ---------------- بيانات النشاط ---------------- */

@Composable
fun BusinessDialog(current: Business, onDismiss: () -> Unit, onSave: (Business) -> Unit) {
    var name by remember { mutableStateOf(current.name) }
    var phone by remember { mutableStateOf(current.phone) }
    var address by remember { mutableStateOf(current.address) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("بيانات نشاطك") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "بتظهر في أعلى كل فاتورة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FormField(name, { name = it }, "اسم النشاط أو اسمك")
                FormField(phone, { phone = it }, "رقم الهاتف", keyboard = KeyboardType.Phone)
                FormField(address, { address = it }, "العنوان (اختياري)")
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(Business(name.trim(), phone.trim(), address.trim())) }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun FormField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    )
}

/* ---------------- شريط علوي لشاشات الفاتورة ---------------- */

@Composable
fun ScreenBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowForward, contentDescription = "رجوع")
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            actions()
        }
    }
}

/* ---------------- إنشاء وتعديل فاتورة ---------------- */

private data class ItemDraft(val id: Long, val name: String, val qty: String, val price: String)

@Composable
fun InvoiceEditor(
    existing: Invoice?,
    number: String,
    onBack: () -> Unit,
    onSave: (Invoice) -> Unit,
    clients: List<Client> = emptyList()
) {
    val date = remember { existing?.date ?: System.currentTimeMillis() }
    var client by remember { mutableStateOf(existing?.client ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var discountText by remember { mutableStateOf(if (existing != null && existing.discount > 0) plainNum(existing.discount) else "") }
    var taxText by remember { mutableStateOf(if (existing != null && existing.taxPercent > 0) plainNum(existing.taxPercent) else "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var paid by remember { mutableStateOf(existing?.paid ?: false) }
    var counter by remember { mutableStateOf(1000L) }
    val drafts = remember {
        mutableStateListOf<ItemDraft>().apply {
            if (existing != null && existing.items.isNotEmpty()) {
                existing.items.forEachIndexed { i, item ->
                    add(ItemDraft(i.toLong(), item.name, plainNum(item.qty), plainNum(item.price)))
                }
            } else {
                add(ItemDraft(0L, "", "1", ""))
            }
        }
    }

    val parsed = drafts.map { InvItem(it.name.trim(), parseNum(it.qty) ?: 0.0, parseNum(it.price) ?: 0.0) }
    val valid = parsed.filter { it.name.isNotBlank() && it.qty > 0 }
    val discount = parseNum(discountText) ?: 0.0
    val taxPercent = parseNum(taxText) ?: 0.0
    val preview = Invoice(0L, number, client, phone, date, valid, discount, taxPercent, notes, paid)
    val canSave = client.isNotBlank() && valid.isNotEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(),
        topBar = { ScreenBar(if (existing == null) "فاتورة جديدة" else "تعديل الفاتورة", onBack) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "$number · ${invDate(date)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FormField(client, { client = it }, "اسم العميل")
            val suggestions = clients
                .filter { c ->
                    !c.name.equals(client.trim(), ignoreCase = true) &&
                        (client.isBlank() || c.name.contains(client.trim(), ignoreCase = true))
                }
                .take(8)
            if (suggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { c ->
                        AssistChip(
                            onClick = {
                                client = c.name
                                if (c.phone.isNotBlank()) phone = c.phone
                            },
                            label = { Text(c.name) }
                        )
                    }
                }
            }
            FormField(phone, { phone = it }, "رقم العميل (اختياري)", keyboard = KeyboardType.Phone)

            Text(
                "البنود",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            drafts.forEachIndexed { index, d ->
                val lineTotal = (parseNum(d.qty) ?: 0.0) * (parseNum(d.price) ?: 0.0)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FormField(
                            d.name,
                            { drafts[index] = d.copy(name = it) },
                            "اسم الصنف أو الخدمة",
                            Modifier.weight(1f)
                        )
                        if (drafts.size > 1) {
                            IconButton(onClick = { drafts.removeAt(index) }) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "حذف البند",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormField(
                            d.qty,
                            { drafts[index] = d.copy(qty = it) },
                            "الكمية",
                            Modifier.weight(1f),
                            KeyboardType.Decimal
                        )
                        FormField(
                            d.price,
                            { drafts[index] = d.copy(price = it) },
                            "سعر الوحدة",
                            Modifier.weight(1.4f),
                            KeyboardType.Decimal
                        )
                    }
                    Text(
                        "إجمالي البند: ${money(lineTotal)} ج.م",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    drafts.add(ItemDraft(counter, "", "1", ""))
                    counter += 1
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("إضافة بند")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField(discountText, { discountText = it }, "خصم (ج.م)", Modifier.weight(1f), KeyboardType.Decimal)
                FormField(taxText, { taxText = it }, "ضريبة %", Modifier.weight(1f), KeyboardType.Decimal)
            }
            FormField(notes, { notes = it }, "ملاحظات (اختياري)")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الفاتورة مدفوعة")
                Switch(checked = paid, onCheckedChange = { paid = it })
            }
            Text(
                "لما تكون مدفوعة، هتتسجل تلقائي كدخل في الرئيسية.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SumLine("المجموع", money(preview.subtotal))
                if (discount > 0) SumLine("الخصم", "−" + money(discount))
                if (taxPercent > 0) SumLine("الضريبة (${money(taxPercent)}%)", money(preview.tax))
                SumLine("الإجمالي", money(preview.total) + " ج.م", bold = true)
            }

            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    onSave(
                        Invoice(
                            id = existing?.id ?: now,
                            number = number,
                            client = client.trim(),
                            phone = phone.trim(),
                            date = date,
                            items = valid,
                            discount = discount,
                            taxPercent = taxPercent,
                            notes = notes.trim(),
                            paid = paid
                        )
                    )
                },
                enabled = canSave,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("حفظ الفاتورة", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SumLine(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("‎$value", fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}

/* ---------------- عرض الفاتورة ---------------- */

@Composable
fun InvoiceView(
    inv: Invoice,
    biz: Business,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(),
        topBar = {
            ScreenBar(inv.number, onBack) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = "تعديل")
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InvoicePaper(inv, biz)
            Button(
                onClick = { shareInvoicePdf(context, inv, biz) },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("مشاركة كملف PDF", fontWeight = FontWeight.SemiBold)
            }
            FilledTonalButton(
                onClick = onTogglePaid,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (inv.paid) "تعليم كغير مدفوعة" else "تعليم كمدفوعة")
            }
            TextButton(
                onClick = { confirmDelete = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حذف الفاتورة")
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف الفاتورة؟") },
            text = { Text("${inv.number} · ${inv.client}") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun InvoicePaper(inv: Invoice, biz: Business) {
    val ink = Color(0xFF12211D)
    val muted = Color(0xFF5E6F69)
    val green = Color(0xFF0E7C66)
    val line = Color(0xFFE3EAE6)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        contentColor = ink,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF0E7C66), Color(0xFF0A4F57))))
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (biz.name.isBlank()) "اسم النشاط" else biz.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (biz.phone.isNotBlank()) {
                        Text("‎" + biz.phone, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                    if (biz.address.isNotBlank()) {
                        Text(biz.address, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("فاتورة", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(inv.number, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("العميل", color = muted, fontSize = 11.sp)
                        Text(inv.client, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (inv.phone.isNotBlank()) {
                            Text("‎" + inv.phone, color = muted, fontSize = 12.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("التاريخ", color = muted, fontSize = 11.sp)
                        Text(invDate(inv.date), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(Modifier.height(2.dp))
                        StatusChip(inv.paid)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE6F3EE))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text("الصنف", Modifier.weight(2.4f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = green)
                    Text("الكمية", Modifier.weight(0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = green, textAlign = TextAlign.Center)
                    Text("السعر", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = green, textAlign = TextAlign.Center)
                    Text("الإجمالي", Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = green, textAlign = TextAlign.End)
                }
                Column {
                    inv.items.forEachIndexed { i, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.name, Modifier.weight(2.4f), fontSize = 13.sp)
                            Text(money(item.qty), Modifier.weight(0.9f), fontSize = 13.sp, textAlign = TextAlign.Center)
                            Text(money(item.price), Modifier.weight(1.2f), fontSize = 13.sp, textAlign = TextAlign.Center)
                            Text(money(item.total), Modifier.weight(1.3f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                        }
                        if (i < inv.items.size - 1) HorizontalDivider(color = line)
                    }
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaperLine("المجموع", money(inv.subtotal), muted)
                        if (inv.discount > 0) PaperLine("الخصم", "−" + money(inv.discount), muted)
                        if (inv.taxPercent > 0) PaperLine("الضريبة (${money(inv.taxPercent)}%)", money(inv.tax), muted)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(green)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الإجمالي", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("‎" + money(inv.total) + " ج.م", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (inv.notes.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("ملاحظات", color = muted, fontSize = 11.sp)
                        Text(inv.notes, fontSize = 13.sp)
                    }
                }
                HorizontalDivider(color = line)
                Text(
                    "شكراً لتعاملكم معنا",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PaperLine(label: String, value: String, labelColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = labelColor, fontSize = 13.sp)
        Text("‎$value", fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/* ---------------- ملف PDF ومشاركته ---------------- */

fun shareInvoicePdf(context: Context, inv: Invoice, biz: Business) {
    try {
        val file = InvoicePdf.create(context, inv, biz)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة"))
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر إنشاء ملف الفاتورة", Toast.LENGTH_SHORT).show()
    }
}

object InvoicePdf {
    private const val PW = 595
    private const val PH = 842
    private const val LEFT = 36f
    private const val RIGHT = 559f

    private fun tp(size: Float, color: Int, bold: Boolean = false): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

    private fun fill(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    /** يرسم نص (يتلف لو طويل) داخل عرض معين. الاتجاه من اليمين لليسار. يرجّع الارتفاع. */
    private fun block(c: Canvas, text: String, x: Float, y: Float, w: Float, p: TextPaint, align: Layout.Alignment): Float {
        if (text.isEmpty()) return 0f
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, p, w.toInt())
            .setAlignment(align)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()
        c.save()
        c.translate(x, y)
        layout.draw(c)
        c.restore()
        return layout.height.toFloat()
    }

    fun create(context: Context, inv: Invoice, biz: Business): File {
        val green = 0xFF0E7C66.toInt()
        val ink = 0xFF12211D.toInt()
        val muted = 0xFF5E6F69.toInt()
        val soft = 0xFFE6F3EE.toInt()
        val lineColor = 0xFFE3EAE6.toInt()
        val white = 0xFFFFFFFF.toInt()
        val dateText = SimpleDateFormat("d MMMM yyyy", latinArabic).format(Date(inv.date))

        val doc = PdfDocument()
        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PW, PH, pageNo).create())
        var c = page.canvas

        // الترويسة
        c.drawRect(0f, 0f, PW.toFloat(), 124f, fill(green))
        val bizName = if (biz.name.isBlank()) "اسم النشاط" else biz.name
        var hy = 24f
        hy += block(c, bizName, RIGHT - 300f, hy, 300f, tp(22f, white, true), Layout.Alignment.ALIGN_NORMAL) + 9f
        if (biz.phone.isNotBlank()) {
            hy += block(c, biz.phone, RIGHT - 300f, hy, 300f, tp(11f, white), Layout.Alignment.ALIGN_NORMAL) + 5f
        }
        if (biz.address.isNotBlank()) {
            block(c, biz.address, RIGHT - 300f, hy, 300f, tp(11f, white), Layout.Alignment.ALIGN_NORMAL)
        }
        val invH = block(c, "فاتورة", LEFT, 24f, 190f, tp(26f, white, true), Layout.Alignment.ALIGN_OPPOSITE)
        block(c, inv.number, LEFT, 24f + invH + 9f, 190f, tp(12f, white), Layout.Alignment.ALIGN_OPPOSITE)

        // العميل والتاريخ
        var y = 150f
        block(c, "العميل", RIGHT - 250f, y, 250f, tp(10f, muted), Layout.Alignment.ALIGN_NORMAL)
        val clientH = block(c, inv.client, RIGHT - 250f, y + 14f, 250f, tp(14f, ink, true), Layout.Alignment.ALIGN_NORMAL)
        var leftH = 0f
        if (inv.phone.isNotBlank()) {
            leftH = block(c, inv.phone, RIGHT - 250f, y + 14f + clientH + 2f, 250f, tp(11f, muted), Layout.Alignment.ALIGN_NORMAL) + 2f
        }
        block(c, "التاريخ", LEFT, y, 200f, tp(10f, muted), Layout.Alignment.ALIGN_OPPOSITE)
        block(c, dateText, LEFT, y + 14f, 200f, tp(13f, ink, true), Layout.Alignment.ALIGN_OPPOSITE)
        val statusText = if (inv.paid) "مدفوعة" else "غير مدفوعة"
        val statusColor = if (inv.paid) 0xFF17935F.toInt() else 0xFFC77D0A.toInt()
        block(c, statusText, LEFT, y + 38f, 200f, tp(12f, statusColor, true), Layout.Alignment.ALIGN_OPPOSITE)
        y += 14f + clientH + leftH + 22f
        if (y < 218f) y = 218f

        // أعمدة الجدول من اليمين لليسار
        val nameX = 326f
        val nameW = RIGHT - nameX
        val qtyX = 266f
        val qtyW = nameX - qtyX
        val priceX = 161f
        val priceW = qtyX - priceX
        val totalX = LEFT
        val totalW = priceX - LEFT

        fun tableHeader() {
            c.drawRoundRect(LEFT, y, RIGHT, y + 26f, 6f, 6f, fill(soft))
            val hp = tp(11f, green, true)
            block(c, "الصنف", nameX, y + 7f, nameW - 8f, hp, Layout.Alignment.ALIGN_NORMAL)
            block(c, "الكمية", qtyX, y + 7f, qtyW, hp, Layout.Alignment.ALIGN_CENTER)
            block(c, "السعر", priceX, y + 7f, priceW, hp, Layout.Alignment.ALIGN_CENTER)
            block(c, "الإجمالي", totalX + 6f, y + 7f, totalW - 6f, hp, Layout.Alignment.ALIGN_OPPOSITE)
            y += 26f
        }

        fun newPage() {
            doc.finishPage(page)
            pageNo += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(PW, PH, pageNo).create())
            c = page.canvas
            y = 40f
        }

        tableHeader()
        val rowPaint = tp(12f, ink)
        val boldPaint = tp(12f, ink, true)
        val linePaint = Paint().apply {
            color = lineColor
            strokeWidth = 1f
        }
        inv.items.forEach { item ->
            val nameLayoutH = StaticLayout.Builder
                .obtain(item.name, 0, item.name.length, rowPaint, (nameW - 8f).toInt())
                .setTextDirection(TextDirectionHeuristics.RTL)
                .build().height.toFloat()
            val rowH = maxOf(26f, nameLayoutH + 14f)
            if (y + rowH > 770f) {
                newPage()
                tableHeader()
            }
            block(c, item.name, nameX, y + 7f, nameW - 8f, rowPaint, Layout.Alignment.ALIGN_NORMAL)
            block(c, money(item.qty), qtyX, y + 7f, qtyW, rowPaint, Layout.Alignment.ALIGN_CENTER)
            block(c, money(item.price), priceX, y + 7f, priceW, rowPaint, Layout.Alignment.ALIGN_CENTER)
            block(c, money(item.total), totalX + 6f, y + 7f, totalW - 6f, boldPaint, Layout.Alignment.ALIGN_OPPOSITE)
            y += rowH
            c.drawLine(LEFT, y, RIGHT, y, linePaint)
        }

        // الإجماليات والملاحظات
        if (y + 150f > 800f) newPage()
        y += 18f
        val totalsW = 234f
        var ty = y
        fun totalLine(label: String, value: String, color: Int) {
            val p = tp(12f, color)
            block(c, label, LEFT, ty, totalsW, p, Layout.Alignment.ALIGN_NORMAL)
            block(c, value, LEFT, ty, totalsW, p, Layout.Alignment.ALIGN_OPPOSITE)
            ty += 20f
        }
        totalLine("المجموع", money(inv.subtotal), muted)
        if (inv.discount > 0) totalLine("الخصم", "-" + money(inv.discount), muted)
        if (inv.taxPercent > 0) totalLine("الضريبة (" + money(inv.taxPercent) + "%)", money(inv.tax), muted)
        ty += 4f
        c.drawRoundRect(LEFT, ty, LEFT + totalsW, ty + 32f, 8f, 8f, fill(green))
        val gp = tp(14f, white, true)
        block(c, "الإجمالي", LEFT + 10f, ty + 8f, totalsW - 20f, gp, Layout.Alignment.ALIGN_NORMAL)
        block(c, money(inv.total) + " ج.م", LEFT + 10f, ty + 8f, totalsW - 20f, gp, Layout.Alignment.ALIGN_OPPOSITE)

        if (inv.notes.isNotBlank()) {
            block(c, "ملاحظات", RIGHT - 240f, y, 240f, tp(10f, muted), Layout.Alignment.ALIGN_NORMAL)
            block(c, inv.notes, RIGHT - 240f, y + 14f, 240f, tp(12f, ink), Layout.Alignment.ALIGN_NORMAL)
        }

        // التذييل
        block(c, "شكراً لتعاملكم معنا", LEFT, 808f, RIGHT - LEFT, tp(11f, muted), Layout.Alignment.ALIGN_CENTER)
        doc.finishPage(page)

        val dir = File(context.cacheDir, "invoices")
        dir.mkdirs()
        val file = File(dir, inv.number + ".pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
