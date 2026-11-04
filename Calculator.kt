package com.masrofy.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val OPS = "+−×÷"

/** يحسب التعبير مع أولوية الضرب والقسمة. يرجع null لو التعبير ناقص، و NaN لو قسمة على صفر. */
private fun calcEval(expr: String): Double? {
    val nums = ArrayList<Double>()
    val ops = ArrayList<Char>()
    var neg = false
    var expectNum = true
    var i = 0
    val n = expr.length
    while (i < n) {
        val c = expr[i]
        if (c.isDigit() || c == '.') {
            var j = i
            while (j < n && (expr[j].isDigit() || expr[j] == '.')) j++
            var v = expr.substring(i, j).toDoubleOrNull() ?: return null
            i = j
            while (i < n && expr[i] == '%') {
                v /= 100.0
                i++
            }
            if (neg) {
                v = -v
                neg = false
            }
            nums.add(v)
            expectNum = false
        } else if (c == '−' && expectNum) {
            neg = !neg
            i++
        } else if (OPS.indexOf(c) >= 0) {
            if (expectNum) return null
            ops.add(c)
            expectNum = true
            i++
        } else {
            return null
        }
    }
    if (expectNum || nums.isEmpty()) return null

    val vals = ArrayList<Double>()
    vals.add(nums[0])
    val addOps = ArrayList<Char>()
    for (k in ops.indices) {
        val op = ops[k]
        val v = nums[k + 1]
        if (op == '×') {
            vals[vals.size - 1] = vals[vals.size - 1] * v
        } else if (op == '÷') {
            if (v == 0.0) return Double.NaN
            vals[vals.size - 1] = vals[vals.size - 1] / v
        } else {
            addOps.add(op)
            vals.add(v)
        }
    }
    var r = vals[0]
    for (k in addOps.indices) {
        r = if (addOps[k] == '+') r + vals[k + 1] else r - vals[k + 1]
    }
    return r
}

private fun fmtCalc(v: Double): String {
    if (v.isNaN() || v.isInfinite()) return "0"
    val bd = java.math.BigDecimal(v).setScale(8, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
    return bd.toPlainString().replace('-', '−')
}

private fun pretty(s: String): String =
    Regex("(?<![.\\d])\\d{4,}").replace(s) { m ->
        m.value.reversed().chunked(3).joinToString(",").reversed()
    }

private fun trimOps(s: String): String = s.trimEnd('+', '−', '×', '÷')

@Composable
fun CalculatorScreen(onBack: () -> Unit, onUseAmount: (Double) -> Unit) {
    var expr by rememberSaveable { mutableStateOf("") }
    var done by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }

    fun lastNumber(): String {
        var i = expr.length
        while (i > 0 && (expr[i - 1].isDigit() || expr[i - 1] == '.')) i--
        return expr.substring(i)
    }

    fun currentValue(): Double? {
        val v = calcEval(trimOps(expr))
        return if (v == null || v.isNaN()) null else v
    }

    fun press(k: String) {
        error = false
        when (k) {
            "AC" -> {
                expr = ""
                done = false
            }
            "⌫" -> {
                if (done) {
                    expr = ""
                    done = false
                } else if (expr.isNotEmpty()) {
                    expr = expr.dropLast(1)
                }
            }
            "=" -> {
                val r = calcEval(trimOps(expr))
                if (r != null && r.isNaN()) {
                    error = true
                } else if (r != null) {
                    expr = fmtCalc(r)
                    done = true
                }
            }
            "+", "−", "×", "÷" -> {
                if (expr.isEmpty()) {
                    if (k == "−") expr = "−"
                } else {
                    done = false
                    if (OPS.indexOf(expr.last()) >= 0) {
                        if (expr.length > 1) expr = expr.dropLast(1) + k
                    } else {
                        expr += k
                    }
                }
            }
            "%" -> {
                if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == '%')) {
                    expr += "%"
                    done = false
                }
            }
            "." -> {
                if (done) {
                    expr = "0."
                    done = false
                } else {
                    val ln = lastNumber()
                    if (!ln.contains('.') && !expr.endsWith("%")) {
                        expr += if (ln.isEmpty()) "0." else "."
                    }
                }
            }
            else -> {
                if (done) {
                    expr = k
                    done = false
                } else if (!expr.endsWith("%")) {
                    val ln = lastNumber()
                    if (ln == "0") {
                        expr = expr.dropLast(1) + k
                    } else if (ln.length < 15) {
                        expr += k
                    }
                }
            }
        }
    }

    val value = currentValue()
    val live = if (done || expr.isEmpty() || value == null) null else fmtCalc(value)
    val shown = pretty(expr).ifEmpty { "0" }
    val mainSize = when {
        shown.length > 16 -> 28.sp
        shown.length > 11 -> 38.sp
        else -> 52.sp
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(),
        topBar = { ScreenBar("الحاسبة", onBack) }
    ) { pad ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 22.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        if (error) {
                            Text(
                                "مينفعش القسمة على صفر",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else if (live != null) {
                            Text(
                                "= " + pretty(live),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            shown,
                            fontSize = mainSize,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val v = currentValue()
                            if (v != null) {
                                expr = fmtCalc(v * 1.14)
                                done = true
                            }
                        },
                        enabled = value != null,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("+14% ضريبة") }
                    FilledTonalButton(
                        onClick = {
                            val v = currentValue()
                            if (v != null && v > 0) onUseAmount(v)
                        },
                        enabled = value != null && value > 0,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("سجّله كمصروف") }
                }

                val rows = listOf(
                    listOf("AC", "⌫", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "−"),
                    listOf("1", "2", "3", "+")
                )
                rows.forEach { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        r.forEach { k ->
                            CalcKey(k, Modifier.weight(1f)) { press(k) }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalcKey("0", Modifier.weight(2f)) { press("0") }
                    CalcKey(".", Modifier.weight(1f)) { press(".") }
                    CalcKey("=", Modifier.weight(1f)) { press("=") }
                }
            }
        }
    }
}

@Composable
private fun CalcKey(label: String, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val isOp = label == "+" || label == "−" || label == "×" || label == "÷"
    val isFn = label == "AC" || label == "⌫" || label == "%"
    val bg: Color = when {
        label == "=" -> scheme.primary
        isOp -> scheme.primaryContainer
        isFn -> scheme.surfaceVariant
        else -> scheme.surface
    }
    val fg: Color = when {
        label == "=" -> scheme.onPrimary
        isOp -> scheme.onPrimaryContainer
        label == "AC" -> scheme.error
        else -> scheme.onSurface
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = modifier.height(58.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = fg)
        }
    }
}
