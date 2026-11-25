package app.fnfmodmanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.fnfmodmanager.R

/** fnf-logo-transparent.svg from the design package, converted to a vector drawable. */
@Composable
fun FnfLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.logo_fnf),
        contentDescription = "FNF Mod Manager",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
