package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.ModStatus
import app.fnfmodmanager.ui.theme.FnfColors

/** The four state pills: مثبت / متوافق / غير متوافق / خطأ. */
@Composable
fun StatusChip(
    status: ModStatus,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    count: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val color: Color
    val icon: IconPair
    val labelRes: Int
    when (status) {
        ModStatus.INSTALLED -> { color = FnfColors.Success; icon = Ic.Check; labelRes = R.string.status_installed }
        ModStatus.COMPATIBLE -> { color = FnfColors.Info; icon = Ic.Info; labelRes = R.string.status_compatible }
        ModStatus.INCOMPATIBLE -> { color = FnfColors.Warning; icon = Ic.Alert; labelRes = R.string.status_incompatible }
        ModStatus.ERROR -> { color = FnfColors.Error; icon = Ic.X; labelRes = R.string.status_error }
    }
    val label = stringResource(labelRes) + (count?.let { " $it" } ?: "")
    val shape = RoundedCornerShape(if (compact) 10.dp else 16.dp)
    val badge = if (compact) 16.dp else 22.dp
    val glyph = if (compact) 10.dp else 14.dp
    val base = if (onClick != null) modifier.fnfClickable(onClick = onClick) else modifier
    Row(
        base
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.5f), shape)
            .padding(horizontal = if (compact) 6.dp else 12.dp, vertical = if (compact) 3.dp else 7.dp),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (status == ModStatus.INCOMPATIBLE) {
            FnfIcon(icon, size = badge, tint = color)
        } else {
            Box(Modifier.size(badge).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                FnfIcon(icon, size = glyph, tint = Color.White)
            }
        }
        Text(
            label,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
        )
    }
}
