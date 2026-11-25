package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.ActivityItem
import app.fnfmodmanager.ui.components.EmptyState
import app.fnfmodmanager.ui.components.FnfIcon
import app.fnfmodmanager.ui.components.FnfSheet
import app.fnfmodmanager.ui.components.GlassCard
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.theme.FnfColors
import java.text.DateFormat
import java.util.Date

@Composable
fun ActivitySheet(items: List<ActivityItem>, onClose: () -> Unit) {
    FnfSheet(onDismiss = onClose) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.activity_title), style = MaterialTheme.typography.titleMedium, color = FnfColors.Text)
            if (items.isEmpty()) {
                EmptyState(Ic.Bell, stringResource(R.string.activity_empty), stringResource(R.string.activity_empty_body))
            } else {
                val fmt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                items.forEach { a ->
                    GlassCard(Modifier.fillMaxWidth(), corner = 16.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FnfIcon(if (a.ok) Ic.Check else Ic.X, size = 20.dp, tint = if (a.ok) FnfColors.Success else FnfColors.Accent)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(a.text, style = MaterialTheme.typography.bodyMedium, color = FnfColors.Text)
                                Text(fmt.format(Date(a.time)), style = MaterialTheme.typography.labelSmall, color = FnfColors.Muted)
                            }
                        }
                    }
                }
            }
        }
    }
}
