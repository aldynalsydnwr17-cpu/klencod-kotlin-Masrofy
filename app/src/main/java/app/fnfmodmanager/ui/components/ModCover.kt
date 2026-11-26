package app.fnfmodmanager.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import app.fnfmodmanager.R
import app.fnfmodmanager.ui.theme.FnfColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private fun decode(file: File, maxSide: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxSide && bounds.outHeight / (sample * 2) >= maxSide) sample *= 2
    return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
}

/**
 * The cover is the mod's own _polymod_icon.png (kept as-is on disk, only down-sampled for display when it is far
 * larger than the view). Mods without one get the placeholder icon from the design package.
 */
@Composable
fun ModCover(cover: File?, modifier: Modifier = Modifier, corner: Dp = 16.dp, maxSide: Int = 512) {
    val shape = RoundedCornerShape(corner)
    val bmp = produceState<ImageBitmap?>(null, cover?.path, cover?.lastModified()) {
        value = if (cover != null && cover.exists()) withContext(Dispatchers.IO) { runCatching { decode(cover, maxSide) }.getOrNull() } else null
    }.value
    val base = modifier.clip(shape)
    if (bmp != null) {
        Image(bmp, null, base, contentScale = ContentScale.Crop, filterQuality = androidx.compose.ui.graphics.FilterQuality.High)
    } else {
        Image(painterResource(R.drawable.mod_placeholder), null, base.background(FnfColors.PlaceholderFill), contentScale = ContentScale.Crop)
    }
}

/** Same as [ModCover] for a cover that is still in memory (the install summary, before anything is copied). */
@Composable
fun ModCoverBytes(bytes: ByteArray?, modifier: Modifier = Modifier, corner: Dp = 16.dp) {
    val shape = RoundedCornerShape(corner)
    val bmp = remember(bytes) { bytes?.let { b -> runCatching { BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap() }.getOrNull() } }
    val base = modifier.clip(shape)
    if (bmp != null) Image(bmp, null, base, contentScale = ContentScale.Crop, filterQuality = androidx.compose.ui.graphics.FilterQuality.High)
    else Image(painterResource(R.drawable.mod_placeholder), null, base.background(FnfColors.PlaceholderFill), contentScale = ContentScale.Crop)
}
