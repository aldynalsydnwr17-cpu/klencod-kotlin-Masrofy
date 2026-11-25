package app.fnfmodmanager.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R

/** Every icon exists in a red and a white version (the pair shipped in the design ZIP). */
data class IconPair(@DrawableRes val red: Int, @DrawableRes val white: Int)

object Ic {
    val Home = IconPair(R.drawable.ic_home_red, R.drawable.ic_home_white)
    val Settings = IconPair(R.drawable.ic_settings_red, R.drawable.ic_settings_white)
    val Search = IconPair(R.drawable.ic_search_red, R.drawable.ic_search_white)
    val Bell = IconPair(R.drawable.ic_bell_red, R.drawable.ic_bell_white)
    val Folder = IconPair(R.drawable.ic_folder_red, R.drawable.ic_folder_white)
    val FolderPlus = IconPair(R.drawable.ic_folder_plus_red, R.drawable.ic_folder_plus_white)
    val Gamepad = IconPair(R.drawable.ic_gamepad_red, R.drawable.ic_gamepad_white)
    val Download = IconPair(R.drawable.ic_download_red, R.drawable.ic_download_white)
    val Star = IconPair(R.drawable.ic_star_red, R.drawable.ic_star_white)
    val Info = IconPair(R.drawable.ic_info_red, R.drawable.ic_info_white)
    val Trash = IconPair(R.drawable.ic_trash_red, R.drawable.ic_trash_white)
    val Check = IconPair(R.drawable.ic_check_red, R.drawable.ic_check_white)
    val Play = IconPair(R.drawable.ic_play_red, R.drawable.ic_play_white)
    val Plus = IconPair(R.drawable.ic_plus_red, R.drawable.ic_plus_white)
    val Music = IconPair(R.drawable.ic_music_red, R.drawable.ic_music_white)
    val More = IconPair(R.drawable.ic_more_red, R.drawable.ic_more_white)
    val ChevronLeft = IconPair(R.drawable.ic_chevron_left_red, R.drawable.ic_chevron_left_white)

    // not in the ZIP: drawn in the same 24px / 1.8 stroke style (design/extra-icons)
    val Layers = IconPair(R.drawable.ic_layers_red, R.drawable.ic_layers_white)
    val Volume = IconPair(R.drawable.ic_volume_red, R.drawable.ic_volume_white)
    val VolumeOff = IconPair(R.drawable.ic_volume_off_red, R.drawable.ic_volume_off_white)
    val Filter = IconPair(R.drawable.ic_filter_red, R.drawable.ic_filter_white)
    val Sort = IconPair(R.drawable.ic_sort_red, R.drawable.ic_sort_white)
    val X = IconPair(R.drawable.ic_x_red, R.drawable.ic_x_white)
    val Alert = IconPair(R.drawable.ic_alert_red, R.drawable.ic_alert_white)
    val Refresh = IconPair(R.drawable.ic_refresh_red, R.drawable.ic_refresh_white)
    val Compass = IconPair(R.drawable.ic_compass_red, R.drawable.ic_compass_white)
    val Crown = IconPair(R.drawable.ic_crown_red, R.drawable.ic_crown_white)
    val Pause = IconPair(R.drawable.ic_pause_red, R.drawable.ic_pause_white)
    val StarFilled = IconPair(R.drawable.ic_star_filled_red, R.drawable.ic_star_filled_white)
}

@Composable
fun FnfIcon(
    icon: IconPair,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    red: Boolean = false,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null,
) {
    Icon(
        painter = painterResource(if (red) icon.red else icon.white),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}
