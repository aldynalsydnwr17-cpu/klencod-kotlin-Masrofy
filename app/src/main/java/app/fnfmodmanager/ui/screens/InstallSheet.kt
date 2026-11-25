package app.fnfmodmanager.ui.screens

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.InstallPhase
import app.fnfmodmanager.data.InstallUi
import app.fnfmodmanager.ui.components.AngularButton
import app.fnfmodmanager.ui.components.FnfProgressBar
import app.fnfmodmanager.ui.components.FnfSheet
import app.fnfmodmanager.ui.components.FnfTextField
import app.fnfmodmanager.ui.components.GlassCard
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.FnfIcon
import app.fnfmodmanager.ui.components.ModCoverBytes
import app.fnfmodmanager.ui.components.PrimaryButton
import app.fnfmodmanager.ui.components.SecondaryButton
import app.fnfmodmanager.ui.components.SegmentedSpinner
import app.fnfmodmanager.ui.theme.FnfColors
import androidx.compose.ui.text.input.KeyboardType

/** The sheet that opens from "إضافة مود جديد": choose source -> summary -> progress -> result. */
@Composable
fun InstallSheet(
    ui: InstallUi?,
    showChooser: Boolean,
    gameVersion: String,
    onPickZip: () -> Unit,
    onPickFolder: () -> Unit,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    if (ui == null && !showChooser) return
    val running = ui is InstallUi.Running || ui is InstallUi.Preparing
    FnfSheet(onDismiss = onClose, dismissible = !running) {
        when (ui) {
            null -> Chooser(onPickZip, onPickFolder)
            InstallUi.Preparing -> Working(stringResource(R.string.install_preparing), null, 0f)
            is InstallUi.Ready -> Ready(ui, gameVersion, onConfirm, onClose)
            is InstallUi.Running -> {
                val frac = if (ui.bytesTotal > 0) ui.bytesDone.toFloat() / ui.bytesTotal else 0f
                val phase = when (ui.phase) {
                    InstallPhase.PREPARE -> stringResource(R.string.phase_prepare)
                    InstallPhase.COPY -> stringResource(R.string.phase_copy)
                    InstallPhase.PATCH -> stringResource(R.string.phase_patch)
                    InstallPhase.VERIFY -> stringResource(R.string.phase_verify)
                }
                Working(phase, stringResource(R.string.install_files_fmt, ui.filesDone, ui.filesTotal) + "\n" + ui.current, frac)
            }
            is InstallUi.Done -> Done(ui, onClose)
            is InstallUi.Failed -> Failed(ui, onBack, onClose)
        }
    }
}

@Composable
private fun Chooser(onPickZip: () -> Unit, onPickFolder: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.home_add_mod), style = MaterialTheme.typography.titleMedium, color = FnfColors.Text)
        Text(stringResource(R.string.chooser_body), style = MaterialTheme.typography.bodyMedium, color = FnfColors.Muted)
        PrimaryButton(stringResource(R.string.chooser_zip), onPickZip, Modifier.fillMaxWidth(), icon = Ic.Download)
        SecondaryButton(stringResource(R.string.chooser_folder), onPickFolder, Modifier.fillMaxWidth(), icon = Ic.Folder)
    }
}

@Composable
private fun Ready(ui: InstallUi.Ready, gameVersion: String, onConfirm: (String) -> Unit, onClose: () -> Unit) {
    val p = ui.prepared
    val ctx = LocalContext.current
    var version by remember { mutableStateOf(gameVersion) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ModCoverBytes(p.coverBytes, Modifier.size(64.dp), corner = 14.dp)
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.title, style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
                    if (p.author.isNotBlank()) Text(p.author, style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted)
                    Text(
                        stringResource(R.string.install_summary_fmt, p.fileCount, Formatter.formatShortFileSize(ctx, p.totalBytes)),
                        style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted,
                    )
                }
            }
        }
        if (p.exists) Text(stringResource(R.string.install_exists_warning), style = MaterialTheme.typography.bodySmall, color = FnfColors.Warning)
        Text(stringResource(R.string.install_version_label), style = MaterialTheme.typography.labelMedium, color = FnfColors.Muted)
        FnfTextField(version, { version = it }, placeholder = "0.8.8", leading = Ic.Gamepad, keyboardType = KeyboardType.Decimal)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AngularButton(stringResource(R.string.install_confirm), { onConfirm(version) }, Modifier.weight(1f), enabled = version.isNotBlank())
            AngularButton(stringResource(R.string.cancel), onClose, Modifier.weight(1f), primary = false)
        }
    }
}

@Composable
private fun Working(title: String, detail: String?, progress: Float) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SegmentedSpinner(progress = if (detail == null) null else progress)
        Text(title, style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
        if (detail != null) {
            FnfProgressBar(progress)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, textAlign = TextAlign.Center, maxLines = 3)
        }
        Text(stringResource(R.string.install_keep_open), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Done(ui: InstallUi.Done, onClose: () -> Unit) {
    val r = ui.result
    val ctx = LocalContext.current
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FnfIcon(Ic.Check, size = 56.dp, tint = FnfColors.Success)
        Text(stringResource(R.string.install_done_title), style = MaterialTheme.typography.titleMedium, color = FnfColors.Success)
        Text(r.title, style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
        Text(
            stringResource(R.string.install_done_body_fmt, r.fileCount, Formatter.formatShortFileSize(ctx, r.totalBytes), r.oldApi.ifBlank { "—" }, r.newApi),
            style = MaterialTheme.typography.bodyMedium, color = FnfColors.Muted, textAlign = TextAlign.Center,
        )
        r.warnings.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = FnfColors.Warning, textAlign = TextAlign.Center) }
        PrimaryButton(stringResource(R.string.done), onClose, Modifier.fillMaxWidth())
    }
}

@Composable
private fun Failed(ui: InstallUi.Failed, onBack: () -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FnfIcon(Ic.Alert, size = 52.dp, tint = FnfColors.Accent)
        Text(stringResource(R.string.install_failed_title), style = MaterialTheme.typography.titleMedium, color = FnfColors.Accent)
        Text(ui.message, style = MaterialTheme.typography.bodyMedium, color = FnfColors.Text, textAlign = TextAlign.Center)
        ui.details.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, textAlign = TextAlign.Center) }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AngularButton(stringResource(R.string.retry), onBack, Modifier.weight(1f))
            AngularButton(stringResource(R.string.close), onClose, Modifier.weight(1f), primary = false)
        }
    }
}
