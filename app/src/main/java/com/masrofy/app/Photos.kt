package com.masrofy.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** صور الإيصالات: بتتحفظ مصغّرة جوه التطبيق بس. */
object Receipts {
    private fun dir(context: Context): File {
        val d = File(context.filesDir, "receipts")
        d.mkdirs()
        return d
    }

    fun file(context: Context, name: String): File = File(dir(context), name)

    fun copyIn(context: Context, uri: Uri, name: String): String? {
        return try {
            val bounds = BitmapFactory.Options()
            bounds.inJustDecodeBounds = true
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
            val opts = BitmapFactory.Options()
            opts.inSampleSize = sample
            var bmp = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return null

            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use {
                    ExifInterface(it).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }
            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (degrees != 0f) {
                val m = Matrix()
                m.postRotate(degrees)
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
            }
            FileOutputStream(file(context, name)).use { bmp.compress(Bitmap.CompressFormat.JPEG, 80, it) }
            name
        } catch (e: Exception) {
            null
        }
    }

    fun load(context: Context, name: String, maxSide: Int): Bitmap? {
        if (name.isBlank()) return null
        return try {
            val path = file(context, name).path
            val bounds = BitmapFactory.Options()
            bounds.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
            val opts = BitmapFactory.Options()
            opts.inSampleSize = sample
            BitmapFactory.decodeFile(path, opts)
        } catch (e: Exception) {
            null
        }
    }

    fun delete(context: Context, name: String) {
        if (name.isNotBlank()) {
            try {
                file(context, name).delete()
            } catch (e: Exception) {
                // مش مهم
            }
        }
    }
}

@Composable
fun PhotoSection(photo: String, key: Long, onChange: (String) -> Unit) {
    val context = LocalContext.current
    var viewing by remember { mutableStateOf(false) }
    val camFile = remember { File(context.cacheDir, "cam_$key.jpg") }

    fun take(uri: Uri) {
        val name = "r" + key + "_" + System.currentTimeMillis() + ".jpg"
        val saved = Receipts.copyIn(context, uri, name)
        if (saved != null) {
            onChange(saved)
        } else {
            Toast.makeText(context, "تعذر حفظ الصورة", Toast.LENGTH_SHORT).show()
        }
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) take(uri)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) take(Uri.fromFile(camFile))
    }

    fun launchCamera() {
        try {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", camFile)
            camera.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "مفيش تطبيق كاميرا متاح", Toast.LENGTH_SHORT).show()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "صورة الإيصال (اختياري)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (photo.isBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { launchCamera() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Ic.Cam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("تصوير")
                }
                OutlinedButton(
                    onClick = { gallery.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Ic.Photo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("من المعرض")
                }
            }
        } else {
            val bmp = remember(photo) { Receipts.load(context, photo, 300) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .bounceClick { viewing = true }
                ) {
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "صورة الإيصال",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("تم إرفاق الصورة", fontWeight = FontWeight.SemiBold)
                    Text(
                        "دوس عليها للتكبير",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { gallery.launch("image/*") }) { Text("تغيير") }
                IconButton(onClick = { onChange("") }) {
                    Icon(Ic.Close, contentDescription = "إزالة الصورة")
                }
            }
        }
    }

    if (viewing) {
        val big = remember(photo) { Receipts.load(context, photo, 1600) }
        Dialog(onDismissRequest = { viewing = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .bounceClick { viewing = false }
            ) {
                if (big != null) {
                    Image(
                        bitmap = big.asImageBitmap(),
                        contentDescription = "صورة الإيصال",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
