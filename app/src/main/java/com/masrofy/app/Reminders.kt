package com.masrofy.app

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Reminders.show(context, false)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Reminders.schedule(context)
        }
    }
}

object Reminders {
    private const val CHANNEL = "daily_reminder"

    private fun prefs(context: Context) =
        context.getSharedPreferences("masrofy", Context.MODE_PRIVATE)

    fun enabled(context: Context): Boolean = prefs(context).getString("rem_on", "0") == "1"
    fun hour(context: Context): Int = prefs(context).getString("rem_h", "21")?.toIntOrNull() ?: 21
    fun minute(context: Context): Int = prefs(context).getString("rem_m", "0")?.toIntOrNull() ?: 0

    fun setConfig(context: Context, on: Boolean, h: Int, m: Int) {
        prefs(context).edit()
            .putString("rem_on", if (on) "1" else "0")
            .putString("rem_h", h.toString())
            .putString("rem_m", m.toString())
            .apply()
        if (on) schedule(context) else cancel(context)
    }

    private fun pending(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            1001,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun schedule(context: Context) {
        if (!enabled(context)) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour(context))
        cal.set(Calendar.MINUTE, minute(context))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending(context)
        )
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(context))
    }

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** force = true للتجربة: يبعت الإشعار حتى لو سجلت عملية النهاردة. */
    fun show(context: Context, force: Boolean) {
        if (!hasPermission(context)) return
        if (!force) {
            val start = Calendar.getInstance()
            start.set(Calendar.HOUR_OF_DAY, 0)
            start.set(Calendar.MINUTE, 0)
            start.set(Calendar.SECOND, 0)
            start.set(Calendar.MILLISECOND, 0)
            val loggedToday = Store(context).load().any { it.createdAt >= start.timeInMillis }
            if (loggedToday) return
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "التذكير اليومي", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("مصروفي")
            .setContentText("سجّلت مصروفات النهاردة؟ خد دقيقة وحدّث حساباتك")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(2001, n)
    }
}

private fun timeLabel(h: Int, m: Int): String {
    val h12 = if (h % 12 == 0) 12 else h % 12
    val ap = if (h < 12) "ص" else "م"
    return h12.toString() + ":" + (if (m < 10) "0" else "") + m + " " + ap
}

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var on by remember { mutableStateOf(Reminders.enabled(context)) }
    var h by remember { mutableIntStateOf(Reminders.hour(context)) }
    var m by remember { mutableIntStateOf(Reminders.minute(context)) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            on = true
            Reminders.setConfig(context, true, h, m)
        } else {
            Toast.makeText(context, "لازم تسمح بالإشعارات من إعدادات الموبايل", Toast.LENGTH_LONG).show()
        }
    }

    fun enable() {
        if (Reminders.hasPermission(context)) {
            on = true
            Reminders.setConfig(context, true, h, m)
        } else {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { ScreenBar("التنبيهات", onBack) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تذكير يومي", fontWeight = FontWeight.Bold)
                            Text(
                                "إشعار بيفكرك تسجّل مصروفاتك",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = on,
                            onCheckedChange = { value ->
                                if (value) {
                                    enable()
                                } else {
                                    on = false
                                    Reminders.setConfig(context, false, h, m)
                                }
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الميعاد", style = MaterialTheme.typography.labelLarge)
                            Text(
                                timeLabel(h, m),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(onClick = {
                            TimePickerDialog(
                                context,
                                { _, hh, mm ->
                                    h = hh
                                    m = mm
                                    Reminders.setConfig(context, on, hh, mm)
                                },
                                h,
                                m,
                                false
                            ).show()
                        }) { Text("تغيير الميعاد") }
                    }
                    Text(
                        "مش هنبعتلك الإشعار لو سجلت عملية النهاردة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FilledTonalButton(
                onClick = {
                    if (Reminders.hasPermission(context)) {
                        Reminders.show(context, true)
                    } else {
                        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("جرّب إشعار الآن", fontWeight = FontWeight.SemiBold) }
        }
    }
}
