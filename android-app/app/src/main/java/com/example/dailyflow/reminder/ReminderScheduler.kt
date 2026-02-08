package com.example.dailyflow.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.datastore.preferences.core.edit
import com.example.dailyflow.REMINDER_STATE_KEY
import com.example.dailyflow.Task
import com.example.dailyflow.dataStore
import com.example.dailyflow.parseTimeValue
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.absoluteValue

object ReminderScheduler {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val allowedOffsets = listOf(1440, 720, 360, 180, 120, 30)

    suspend fun rescheduleAll(context: Context, tasks: List<Task>) {
        val prefs = context.dataStore.data.first()
        val raw = prefs[REMINDER_STATE_KEY]
        val previousKeys = raw?.let { runCatching { json.decodeFromString<List<String>>(it) }.getOrElse { emptyList() } }
            ?: emptyList()

        previousKeys.forEach { key ->
            val parts = key.split(":")
            val taskId = parts.getOrNull(0)?.toLongOrNull()
            val minutes = parts.getOrNull(1)?.toIntOrNull()
            if (taskId != null && minutes != null) {
                cancel(context, taskId, minutes)
            }
        }

        val now = LocalDateTime.now()
        val newKeys = mutableListOf<String>()

        tasks.filter { !it.template && !it.completed && it.reminders.isNotEmpty() }.forEach { task ->
            val eventTime = task.toDateTime() ?: return@forEach
            task.reminders.filter { it in allowedOffsets }.forEach { minutes ->
                val remindAt = eventTime.minusMinutes(minutes.toLong())
                if (remindAt.isAfter(now)) {
                    schedule(context, task, minutes, remindAt)
                    newKeys += "${task.id}:$minutes"
                }
            }
        }

        context.dataStore.edit { prefsEdit ->
            prefsEdit[REMINDER_STATE_KEY] = json.encodeToString(newKeys)
        }
    }

    private fun schedule(context: Context, task: Task, minutes: Int, time: LocalDateTime) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TITLE, task.title)
            putExtra(TaskReminderReceiver.EXTRA_TIME, task.time)
            putExtra(TaskReminderReceiver.EXTRA_DATE, task.date)
            putExtra(TaskReminderReceiver.EXTRA_MINUTES, minutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(task.id, minutes),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            else -> {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }
    }

    private fun cancel(context: Context, taskId: Long, minutes: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(taskId, minutes),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun requestCode(taskId: Long, minutes: Int): Int {
        return ((taskId xor (taskId ushr 32)).toInt() * 31 + minutes).absoluteValue
    }

    private fun Task.toDateTime(): LocalDateTime? {
        return runCatching {
            val dateValue = LocalDate.parse(date)
            val timeValue = parseTimeValue(time) ?: LocalTime.of(9, 0)
            LocalDateTime.of(dateValue, timeValue)
        }.getOrNull()
    }
}
