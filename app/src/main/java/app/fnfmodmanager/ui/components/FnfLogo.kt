package app.fnfmodmanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.fnfmodmanager.R

/** fnf_mod_manager_crown_logo.png from the replacement asset pack (transparent PNG, aspect ratio kept). */
@Composable
fun FnfLogo(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.fnf_mod_manager_crown_logo), "FNF Mod Manager", modifier, contentScale = ContentScale.Fit)
}

/** rhythm_boy_mic_mascot.png: the character shown at the edge of the home header. */
@Composable
fun FnfMascot(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.rhythm_boy_mic_mascot), null, modifier, contentScale = ContentScale.Fit)
}

/** fnf_color_title_logo.png: the coloured game title on the game card. */
@Composable
fun GameTitleLogo(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.fnf_color_title_logo), "Friday Night Funkin'", modifier, contentScale = ContentScale.Fit)
}
