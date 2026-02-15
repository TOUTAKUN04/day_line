package com.toutakun04.dayline.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class TaskAlarmDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DISMISS) return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        NotificationManagerCompat.from(context).cancel(AlarmNotificationHelper.notificationId(taskId))
        AlarmRingerService.stopIfMatches(context, taskId)
    }

    companion object {
        const val ACTION_DISMISS = "com.toutakun04.dayline.action.DISMISS_TASK_ALARM"
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}
