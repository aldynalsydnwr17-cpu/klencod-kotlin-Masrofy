package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.SortMode
import app.fnfmodmanager.data.StatusFilter
import app.fnfmodmanager.data.UiState
import app.fnfmodmanager.ui.components.DropdownPill
import app.fnfmodmanager.ui.components.EmptyState
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.IconTile
import app.fnfmodmanager.ui.components.ModActions
import app.fnfmodmanager.ui.components.ModCard
import app.fnfmodmanager.ui.components.SearchField
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun LibraryScreen(
    state: UiState,
    onQuery: (String) -> Unit,
    onFilter: (StatusFilter) -> Unit,
    onSort: (SortMode) -> Unit,
    onRefresh: () -> Unit,
    modActions: ModActions,
    modifier: Modifier = Modifier,
) {
    val mods = state.visibleMods()
    LazyVerticalGrid(
        GridCells.Fixed(2),
        modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.tab_library), style = MaterialTheme.typography.titleLarge, color = FnfColors.Text)
                IconTile(Ic.Refresh, onRefresh, stringResource(R.string.cd_refresh), size = 44.dp)
            }
        }
        item(span = { GridItemSpan(2) }) { SearchField(state.query, onQuery, stringResource(R.string.library_search)) }
        item(span = { GridItemSpan(2) }) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownPill(
                    Ic.Filter, filterLabel(state.filter),
                    StatusFilter.entries.map { it to filterLabel(it) }, state.filter, onFilter,
                )
                DropdownPill(
                    Ic.Sort, sortLabel(state.sort),
                    SortMode.entries.map { it to sortLabel(it) }, state.sort, onSort,
                )
            }
        }
        if (mods.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                EmptyState(Ic.Layers, stringResource(R.string.library_empty_title), stringResource(R.string.library_empty_body))
            }
        } else {
            items(mods, key = { it.folderName }) { m -> ModCard(m, modActions) }
        }
    }
}

@Composable
private fun filterLabel(f: StatusFilter): String = stringResource(
    when (f) {
        StatusFilter.ALL -> R.string.filter_all
        StatusFilter.INSTALLED -> R.string.status_installed
        StatusFilter.COMPATIBLE -> R.string.status_compatible
        StatusFilter.INCOMPATIBLE -> R.string.status_incompatible
        StatusFilter.ERROR -> R.string.status_error
        StatusFilter.FAVORITES -> R.string.filter_favorites
    },
)

@Composable
private fun sortLabel(s: SortMode): String = stringResource(
    when (s) {
        SortMode.NAME -> R.string.sort_name
        SortMode.RECENT -> R.string.sort_recent
        SortMode.FAVORITES -> R.string.sort_favorites
    },
)
