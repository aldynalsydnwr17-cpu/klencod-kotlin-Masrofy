package app.fnfmodmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.data.ToastKind
import app.fnfmodmanager.data.ToastMsg
import app.fnfmodmanager.ui.theme.FnfColors
import kotlinx.coroutines.delay

@Composable
fun FnfToastHost(toast: ToastMsg?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(toast?.id) {
        if (toast != null) {
            delay(3200)
            onDismiss()
        }
    }
    Box(modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(toast != null, enter = fadeIn() + slideInVertically { -it }, exit = fadeOut() + slideOutVertically { -it }) {
            val t = toast
            if (t != null) {
                val color = when (t.kind) {
                    ToastKind.SUCCESS -> FnfColors.Success
                    ToastKind.ERROR -> FnfColors.Accent
                    ToastKind.INFO -> FnfColors.Info
                }
                val shape = RoundedCornerShape(18.dp)
                Row(
                    Modifier.fillMaxWidth().fnfClickable(onClick = onDismiss).clip(shape).background(FnfColors.Surface)
                        .border(1.dp, color.copy(alpha = 0.7f), shape).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                        FnfIcon(if (t.kind == ToastKind.SUCCESS) Ic.Check else if (t.kind == ToastKind.ERROR) Ic.X else Ic.Info, size = 16.dp, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(t.text, style = MaterialTheme.typography.labelLarge, color = FnfColors.Text)
                }
            }
        }
    }
}
