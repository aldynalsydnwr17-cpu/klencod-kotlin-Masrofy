package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, icon: IconPair? = null, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) FnfIcon(icon, size = 26.dp, red = true)
            Text(title, style = MaterialTheme.typography.titleMedium, color = FnfColors.Text)
        }
        if (action != null && onAction != null) {
            val pill = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            Row(
                Modifier.fnfClickable(onClick = onAction).clip(pill).border(1.dp, FnfColors.GlassStroke, pill).background(FnfColors.Surface).padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FnfIcon(Ic.ChevronLeft, size = 18.dp)
                Text(action, style = MaterialTheme.typography.labelMedium, color = FnfColors.Text)
            }
        }
    }
}
