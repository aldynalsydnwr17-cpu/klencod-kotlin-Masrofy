package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.ui.components.AngularButton
import app.fnfmodmanager.ui.components.FnfSheet
import app.fnfmodmanager.ui.components.FnfTextField
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun DeleteConfirm(name: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
    FnfSheet(onDismiss = onCancel) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.delete_title), style = MaterialTheme.typography.titleMedium, color = FnfColors.Text)
            Text(stringResource(R.string.delete_body_fmt, name), style = MaterialTheme.typography.bodyMedium, color = FnfColors.Muted)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AngularButton(stringResource(R.string.detail_delete), onConfirm, Modifier.weight(1f))
                AngularButton(stringResource(R.string.cancel), onCancel, Modifier.weight(1f), primary = false)
            }
        }
    }
}

@Composable
fun VersionEditor(current: String, onSave: (String) -> Unit, onCancel: () -> Unit) {
    var v by remember { mutableStateOf(current) }
    FnfSheet(onDismiss = onCancel) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.settings_version_title), style = MaterialTheme.typography.titleMedium, color = FnfColors.Text)
            Text(stringResource(R.string.version_body), style = MaterialTheme.typography.bodyMedium, color = FnfColors.Muted)
            FnfTextField(v, { v = it }, placeholder = "0.8.8", leading = Ic.Gamepad, keyboardType = KeyboardType.Decimal)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AngularButton(stringResource(R.string.save), { onSave(v) }, Modifier.weight(1f), enabled = v.isNotBlank())
                AngularButton(stringResource(R.string.cancel), onCancel, Modifier.weight(1f), primary = false)
            }
        }
    }
}
