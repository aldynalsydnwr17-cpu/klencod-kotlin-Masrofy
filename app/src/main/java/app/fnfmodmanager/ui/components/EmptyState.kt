package app.fnfmodmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun EmptyState(
    icon: IconPair,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    GlassCard(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FnfIcon(icon, size = 44.dp, red = true)
            Text(title, style = MaterialTheme.typography.titleSmall, color = FnfColors.Text, textAlign = TextAlign.Center)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = FnfColors.Muted, textAlign = TextAlign.Center)
            if (actionText != null && onAction != null) {
                Spacer(Modifier.height(6.dp))
                PrimaryButton(actionText, onAction, Modifier.fillMaxWidth(), height = 48.dp)
            }
        }
    }
}
