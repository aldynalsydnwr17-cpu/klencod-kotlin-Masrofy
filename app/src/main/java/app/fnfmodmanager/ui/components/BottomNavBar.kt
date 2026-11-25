package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.ui.theme.FnfColors

/** Declared in reading order for RTL: first = rightmost. */
enum class Tab(val label: Int, val icon: IconPair) {
    HOME(R.string.tab_home, Ic.Home),
    DISCOVER(R.string.tab_discover, Ic.Compass),
    LIBRARY(R.string.tab_library, Ic.Layers),
    SETTINGS(R.string.tab_settings, Ic.Settings),
}

@Composable
fun BottomNavBar(current: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).background(FnfColors.Surface)
            .border(1.dp, FnfColors.GlassStroke.copy(alpha = 0.5f), shape)
            .navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Tab.entries.forEach { tab ->
            val on = tab == current
            Column(
                Modifier.weight(1f).fnfClickable(role = androidx.compose.ui.semantics.Role.Tab) { onSelect(tab) }.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Box(
                    Modifier.clip(RoundedCornerShape(14.dp)).background(if (on) FnfColors.Accent.copy(alpha = 0.16f) else androidx.compose.ui.graphics.Color.Transparent)
                        .padding(horizontal = 18.dp, vertical = 5.dp),
                ) { FnfIcon(tab.icon, size = 24.dp, red = on, tint = if (on) androidx.compose.ui.graphics.Color.Unspecified else FnfColors.Muted) }
                Text(stringResource(tab.label), style = MaterialTheme.typography.labelSmall, color = if (on) FnfColors.Accent else FnfColors.Muted, maxLines = 1)
            }
        }
    }
}
