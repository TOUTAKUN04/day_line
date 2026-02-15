package com.toutakun04.dayline.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Task reminder" }
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        val date = intent.getStringExtra(EXTRA_DATE).orEmpty()
        val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)
        val lead = if (minutes > 0) "In ${formatMinutes(minutes)}" else "Reminder"

        val serviceIntent = Intent(context, AlarmRingerService::class.java).apply {
            action = AlarmRingerService.ACTION_START
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TITLE, title)
            putExtra(TaskAlarmReceiver.EXTRA_TIME, time)
            putExtra(TaskAlarmReceiver.EXTRA_DATE, date)
            putExtra(TaskAlarmReceiver.EXTRA_LEAD, lead)
        }

        val started = try {
            ContextCompat.startForegroundService(context, serviceIntent)
            true
        } catch (e: Exception) {
            false
        }

        if (!started) {
            AlarmNotificationHelper.showFallbackNotification(context, taskId, title, time, date, lead)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_MINUTES = "extra_minutes"
    }
}

private fun formatMinutes(minutes: Int): String {
    return when {
        minutes >= 1440 && minutes % 1440 == 0 -> {
            val days = minutes / 1440
            "$days day${if (days == 1) "" else "s"}"
        }
        minutes >= 60 && minutes % 60 == 0 -> {
            val hours = minutes / 60
            "$hours hr${if (hours == 1) "" else "s"}"
        }
        else -> "$minutes min"
    }
}
