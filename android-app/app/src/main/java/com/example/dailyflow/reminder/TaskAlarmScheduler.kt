package com.example.dailyflow.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.datastore.preferences.core.edit
import com.example.dailyflow.ALARM_STATE_KEY
import com.example.dailyflow.MainActivity
import com.example.dailyflow.Task
import com.example.dailyflow.dataStore
import com.example.dailyflow.parseTimeValue
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.absoluteValue

object TaskAlarmScheduler {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun rescheduleAll(context: Context, tasks: List<Task>) {
        val prefs = context.dataStore.data.first()
        val raw = prefs[ALARM_STATE_KEY]
        val previousIds = raw?.let { runCatching { json.decodeFromString<List<Long>>(it) }.getOrElse { emptyList() } }
            ?: emptyList()

        previousIds.forEach { taskId ->
            cancel(context, taskId)
        }

        val now = LocalDateTime.now()
        val newIds = mutableListOf<Long>()

        tasks.filter { !it.template && !it.completed }.forEach { task ->
            val eventTime = task.toDateTime() ?: return@forEach
            if (eventTime.isAfter(now)) {
                if (schedule(context, task, eventTime)) {
                    newIds += task.id
                }
            }
        }

        context.dataStore.edit { prefsEdit ->
            prefsEdit[ALARM_STATE_KEY] = json.encodeToString(newIds)
        }
    }

    private fun schedule(context: Context, task: Task, time: LocalDateTime): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
        val triggerAtMillis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_START
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskAlarmReceiver.EXTRA_TITLE, task.title)
            putExtra(TaskAlarmReceiver.EXTRA_TIME, task.time)
            putExtra(TaskAlarmReceiver.EXTRA_DATE, task.date)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(task.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = PendingIntent.getActivity(
            context,
            requestCode(task.id),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent)
        return try {
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
            true
        } catch (e: Exception) {
            scheduleFallback(alarmManager, triggerAtMillis, pendingIntent)
        }
    }

    private fun scheduleFallback(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ): Boolean {
        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms() -> {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
                else -> {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun cancel(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_START
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(taskId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun requestCode(taskId: Long): Int {
        return ((taskId xor (taskId ushr 32)).toInt() * 37).absoluteValue
    }

    private fun Task.toDateTime(): LocalDateTime? {
        if (date.isBlank() || time.isBlank()) return null
        return runCatching {
            val dateValue = LocalDate.parse(date)
            val timeValue = parseTimeValue(time) ?: LocalTime.of(9, 0)
            LocalDateTime.of(dateValue, timeValue)
        }.getOrNull()
    }
}
