package app.fnfmodmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun StatCard(value: String, label: String, icon: IconPair, modifier: Modifier = Modifier, tint: Color = FnfColors.Accent, onClick: (() -> Unit)? = null) {
    GlassCard(modifier, corner = 18.dp, onClick = onClick, contentPadding = PaddingValues(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.then(Modifier)) {
            FnfIcon(icon, size = 22.dp, tint = tint)
            Text(value, style = MaterialTheme.typography.titleLarge, color = FnfColors.Text)
            Text(label, style = MaterialTheme.typography.labelSmall, color = FnfColors.Muted, maxLines = 1)
        }
    }
}
