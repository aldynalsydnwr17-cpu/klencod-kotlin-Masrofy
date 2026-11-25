package app.fnfmodmanager.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.InfoCard
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun DiscoverScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    fun open(url: String) = runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    LazyColumn(
        modifier.statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text(stringResource(R.string.tab_discover), style = MaterialTheme.typography.titleLarge, color = FnfColors.Text) }
        item { Text(stringResource(R.string.discover_intro), style = MaterialTheme.typography.bodyMedium, color = FnfColors.Muted) }
        item {
            InfoCard(Ic.Compass, stringResource(R.string.discover_gb_title), stringResource(R.string.discover_gb_body),
                onClick = { open("https://gamebanana.com/games/8694") })
        }
        item {
            InfoCard(Ic.Info, stringResource(R.string.discover_vslice_title), stringResource(R.string.discover_vslice_body),
                onClick = { open("https://funkin.me") })
        }
        item { Text(stringResource(R.string.discover_tip), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted) }
    }
}
