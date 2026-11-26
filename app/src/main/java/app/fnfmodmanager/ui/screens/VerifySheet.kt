package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.VerifyReport
import app.fnfmodmanager.ui.components.AngularButton
import app.fnfmodmanager.ui.components.FnfIcon
import app.fnfmodmanager.ui.components.FnfSheet
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.SegmentedSpinner
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun VerifySheet(verifying: Boolean, report: VerifyReport?, onClose: () -> Unit) {
    if (!verifying && report == null) return
    FnfSheet(onDismiss = onClose, dismissible = !verifying) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (report == null) {
                SegmentedSpinner()
                Text(stringResource(R.string.verify_running), style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
            } else {
                val ok = report.issues.isEmpty()
                FnfIcon(if (ok) Ic.Check else Ic.Alert, size = 52.dp, tint = if (ok) FnfColors.Success else FnfColors.Warning)
                Text(stringResource(if (ok) R.string.verify_ok else R.string.verify_issues), style = MaterialTheme.typography.titleMedium, color = if (ok) FnfColors.Success else FnfColors.Warning)
                Text(report.title, style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
                Text(stringResource(R.string.verify_checked_fmt, report.checked), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted)
                report.issues.take(20).forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = FnfColors.Error, textAlign = TextAlign.Center) }
                if (report.issues.size > 20) Text("… +${report.issues.size - 20}", style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted)
                report.notes.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, textAlign = TextAlign.Center) }
                AngularButton(stringResource(R.string.close), onClose, Modifier.fillMaxWidth())
            }
        }
    }
}
