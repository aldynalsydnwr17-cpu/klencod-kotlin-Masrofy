package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

/** Pill that opens a small menu: used for the filter and the sort order. */
@Composable
fun <T> DropdownPill(
    icon: IconPair,
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Box(modifier) {
        Row(
            Modifier.height(44.dp).fnfClickable { open = true }.clip(shape).background(FnfColors.Surface)
                .border(1.dp, FnfColors.GlassStroke.copy(alpha = 0.8f), shape).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FnfIcon(icon, size = 20.dp, red = true)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = FnfColors.Text, maxLines = 1)
        }
        DropdownMenu(open, { open = false }, containerColor = FnfColors.Card) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text, color = if (value == selected) FnfColors.Accent else FnfColors.Text, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onSelect(value); open = false },
                )
            }
        }
    }
}
