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
    val color: Color      // border and round badge
    val fill: Color       // pill background
    val textColor = FnfColors.Text
    val icon: IconPair
    val labelRes: Int
    when (status) {
        ModStatus.INSTALLED -> { color = FnfColors.Success; fill = Color(0xFF1F1D25); icon = Ic.Check; labelRes = R.string.status_installed }
        ModStatus.COMPATIBLE -> { color = FnfColors.Success; fill = Color(0xFF0F4A25); icon = Ic.Check; labelRes = R.string.status_compatible }
        ModStatus.INCOMPATIBLE -> { color = FnfColors.Accent; fill = Color(0xFF5A0F1E); icon = Ic.X; labelRes = R.string.status_incompatible }
        ModStatus.ERROR -> { color = FnfColors.Accent; fill = Color(0xFF5A0F1E); icon = Ic.Alert; labelRes = R.string.status_error }
    }
    val label = stringResource(labelRes) + (count?.let { " $it" } ?: "")
    val shape = RoundedCornerShape(if (compact) 10.dp else 16.dp)
    val badge = if (compact) 16.dp else 22.dp
    val glyph = if (compact) 10.dp else 14.dp
    val base = if (onClick != null) modifier.fnfClickable(onClick = onClick) else modifier
    Row(
        base
            .clip(shape)
            .background(fill)
            .border(1.5.dp, color, shape)
            .padding(horizontal = if (compact) 6.dp else 12.dp, vertical = if (compact) 3.dp else 7.dp),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(badge).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            FnfIcon(icon, size = glyph, tint = Color.White)
        }
        Text(
            label,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = textColor,
            maxLines = 1,
        )
    }
}
