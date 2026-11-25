package app.fnfmodmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.ui.theme.FnfColors

/** The large dashed-border import panel from the concept: icon, title, hint and the file-picker button. */
@Composable
fun ImportPanel(onPick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier.fillMaxWidth().clip(shape)
            .drawBehind {
                drawRoundRect(FnfColors.Surface.copy(alpha = 0.7f), cornerRadius = CornerRadius(24.dp.toPx()))
                drawRoundRect(
                    FnfColors.Accent,
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                )
            }
            .padding(18.dp),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FnfIcon(Ic.ImportFolder, size = 48.dp, red = true)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_add_mod), style = MaterialTheme.typography.titleMedium, color = FnfColors.Text)
                    Text(stringResource(R.string.import_panel_sub), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted)
                    Text(stringResource(R.string.import_panel_hint), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted)
                }
                // faded_zip_mark.png from the replacement pack (already faded in the file), kept beside the text so nothing overlaps
                androidx.compose.foundation.Image(
                    androidx.compose.ui.res.painterResource(R.drawable.faded_zip_mark), null,
                    Modifier.padding(start = 8.dp).height(84.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                )
            }
            Spacer(Modifier.height(6.dp))
            PrimaryButton(stringResource(R.string.import_cta), onPick, Modifier.fillMaxWidth(), icon = Ic.Folder)
        }
    }
}
