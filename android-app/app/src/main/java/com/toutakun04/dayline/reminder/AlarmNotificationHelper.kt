package com.toutakun04.dayline.reminder

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.toutakun04.dayline.MainActivity
import com.toutakun04.dayline.formatTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

object AlarmNotificationHelper {
    const val SERVICE_CHANNEL_ID = "task_alarms"
    const val FALLBACK_CHANNEL_ID = "task_alarm_fallback"
    const val LEGACY_NOTIFICATION_ID = 2001
    private const val NOTIFICATION_ID_BASE = 2000

    fun notificationId(taskId: Long): Int {
        if (taskId == 0L) return LEGACY_NOTIFICATION_ID
        val hash = (taskId xor (taskId ushr 32)).toInt().absoluteValue
        return NOTIFICATION_ID_BASE + (hash % 1_000_000)
    }

    fun ensureServiceChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Task alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarms for task start times"
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun ensureFallbackChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                FALLBACK_CHANNEL_ID,
                "Task alarm fallback",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Fallback alarm notifications"
                if (alarmUri != null) {
                    setSound(alarmUri, attrs)
                }
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 800, 800)
            }
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showFallbackNotification(context: Context, taskId: Long, title: String, time: String, date: String, lead: String) {
        ensureFallbackChannel(context)
        if (!canPostNotifications(context)) return

        val notification = buildNotification(
            context = context,
            taskId = taskId,
            title = title,
            time = time,
            date = date,
            channelId = FALLBACK_CHANNEL_ID,
            ongoing = true,
            includeSound = true,
            lead = lead
        )
        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId(taskId), notification)
        }
    }

    fun buildNotification(
        context: Context,
        taskId: Long,
        title: String,
        time: String,
        date: String,
        channelId: String,
        ongoing: Boolean,
        includeSound: Boolean,
        lead: String
    ) = run {
        val timeLabel = if (time.isNotBlank()) formatTime(time) else ""
        val dateLabel = runCatching {
            LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM d"))
        }.getOrElse { "" }
        val whenLabel = listOf(dateLabel, timeLabel).filter { it.isNotBlank() }.joinToString(" - ")
        val body = if (whenLabel.isBlank()) lead else "$lead - $whenLabel"

        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissIntent = PendingIntent.getBroadcast(
            context,
            notificationId(taskId),
            Intent(context, TaskAlarmDismissReceiver::class.java)
                .setAction(TaskAlarmDismissReceiver.ACTION_DISMISS)
                .putExtra(TaskAlarmDismissReceiver.EXTRA_TASK_ID, taskId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(ongoing)
            .setAutoCancel(!ongoing)
            .setOnlyAlertOnce(false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissIntent)
            .setDeleteIntent(dismissIntent)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && includeSound) {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (alarmUri != null) {
                builder.setSound(alarmUri)
            }
            builder.setVibrate(longArrayOf(0, 800, 800, 800))
        }

        builder.build()
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
