package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.ui.theme.FnfColors

/**
 * Game status card as in the reference: round red play button with its label on one side, then the game name,
 * version, the path the user chose, and the detection status.
 */
@Composable
fun GameCard(
    gameVersion: String,
    installed: Boolean,
    installedVersion: String?,
    hasFolder: Boolean,
    path: String,
    onEditVersion: () -> Unit,
    onChoosePath: () -> Unit,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.fnfClickable(onClick = onLaunch).padding(end = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).border(2.dp, FnfColors.Accent, CircleShape).background(FnfColors.Accent.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) { FnfIcon(Ic.Play, size = 32.dp, red = true, contentDescription = stringResource(R.string.home_launch)) }
                Text(stringResource(R.string.home_launch), style = MaterialTheme.typography.labelMedium, color = FnfColors.Text)
            }
            Box(Modifier.fillMaxHeight().width(1.dp).background(FnfColors.GlassStroke.copy(alpha = 0.6f)))
            Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.home_game_title), style = MaterialTheme.typography.titleSmall, color = FnfColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                InfoLine(Ic.Gamepad, stringResource(R.string.home_version_fmt, gameVersion), onEditVersion)
                InfoLine(Ic.Folder, stringResource(R.string.home_path_fmt, path.ifBlank { stringResource(R.string.home_path_none) }), onChoosePath)
                val ok = installed && hasFolder
                val msg = when {
                    !installed -> stringResource(R.string.home_not_detected)
                    !hasFolder -> stringResource(R.string.home_perm_missing)
                    else -> stringResource(R.string.home_detected_ok)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FnfIcon(if (ok) Ic.Check else Ic.Alert, size = 18.dp, tint = if (ok) FnfColors.Success else FnfColors.Warning)
                    Text(msg, style = MaterialTheme.typography.labelMedium, color = if (ok) FnfColors.Success else FnfColors.Warning)
                }
            }
            GameTitleLogo(Modifier.padding(start = 8.dp).width(76.dp))
        }
    }
}

@Composable
private fun InfoLine(icon: IconPair, text: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().fnfClickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FnfIcon(icon, size = 20.dp)
        Text(text, style = MaterialTheme.typography.bodySmall, color = FnfColors.Text, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
