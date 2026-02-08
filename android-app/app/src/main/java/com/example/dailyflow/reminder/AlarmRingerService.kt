package com.example.dailyflow.reminder

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlarmRingerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != ACTION_START) return START_NOT_STICKY

        val title = intent.getStringExtra(TaskAlarmReceiver.EXTRA_TITLE).orEmpty().ifBlank { "Task alarm" }
        val time = intent.getStringExtra(TaskAlarmReceiver.EXTRA_TIME).orEmpty()
        val date = intent.getStringExtra(TaskAlarmReceiver.EXTRA_DATE).orEmpty()
        val lead = intent.getStringExtra(TaskAlarmReceiver.EXTRA_LEAD).orEmpty().ifBlank { "Starts now" }

        AlarmNotificationHelper.ensureServiceChannel(this)
        startForeground(
            AlarmNotificationHelper.NOTIFICATION_ID,
            AlarmNotificationHelper.buildNotification(
                context = this,
                title = title,
                time = time,
                date = date,
                channelId = AlarmNotificationHelper.SERVICE_CHANNEL_ID,
                ongoing = true,
                includeSound = false,
                lead = lead
            )
        )
        startAlarmSound()
        startVibration()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopAlarmSound()
        stopVibration()
        super.onDestroy()
    }

    private fun startAlarmSound() {
        stopAlarmSound()
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (uri == null) return

        runCatching {
            val player = MediaPlayer().apply {
                setDataSource(this@AlarmRingerService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            mediaPlayer = player
        }
    }

    private fun stopAlarmSound() {
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
    }

    companion object {
        const val ACTION_START = "com.example.dailyflow.action.ALARM_RING"
    }

    private fun startVibration() {
        stopVibration()
        val vib = getVibrator() ?: return
        if (!vib.hasVibrator()) return
        vibrator = vib
        val pattern = longArrayOf(0, 800, 800, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
    }
}
