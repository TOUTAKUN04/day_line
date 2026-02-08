package com.example.dailyflow.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class TaskAlarmDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DISMISS) return
        NotificationManagerCompat.from(context).cancel(AlarmNotificationHelper.NOTIFICATION_ID)
        context.stopService(Intent(context, AlarmRingerService::class.java))
    }

    companion object {
        const val ACTION_DISMISS = "com.example.dailyflow.action.DISMISS_TASK_ALARM"
    }
}
