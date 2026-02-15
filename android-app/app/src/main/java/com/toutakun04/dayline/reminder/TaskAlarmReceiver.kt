package com.toutakun04.dayline.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_START) return

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val serviceIntent = Intent(context, AlarmRingerService::class.java).apply {
            action = AlarmRingerService.ACTION_START
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TITLE, intent.getStringExtra(EXTRA_TITLE))
            putExtra(EXTRA_TIME, intent.getStringExtra(EXTRA_TIME))
            putExtra(EXTRA_DATE, intent.getStringExtra(EXTRA_DATE))
            putExtra(EXTRA_LEAD, "Starts now")
        }
        val started = try {
            ContextCompat.startForegroundService(context, serviceIntent)
            true
        } catch (e: Exception) {
            false
        }

        if (!started) {
            val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Task alarm" }
            val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
            val date = intent.getStringExtra(EXTRA_DATE).orEmpty()
            AlarmNotificationHelper.showFallbackNotification(context, taskId, title, time, date, "Starts now")
        }
    }

    companion object {
        const val ACTION_START = "com.toutakun04.dayline.action.START_TASK_ALARM"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_LEAD = "extra_lead"
    }
}
