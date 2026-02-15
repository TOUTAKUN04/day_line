package com.toutakun04.dayline

import android.app.DatePickerDialog
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.toutakun04.dayline.reminder.AlarmNotificationHelper
import com.toutakun04.dayline.reminder.AlarmRingerService
import com.toutakun04.dayline.ui.theme.DayLineTheme
import com.toutakun04.dayline.reminder.ReminderScheduler
import com.toutakun04.dayline.reminder.TaskAlarmScheduler
import com.toutakun04.dayline.widget.DailyTasksWidgetUpdater
import com.toutakun04.dayline.widget.WalkMeterWidgetUpdater
import com.toutakun04.dayline.weather.WeatherDisplay
import com.toutakun04.dayline.weather.WeatherIconType
import com.toutakun04.dayline.weather.WeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val quickAddSignal = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestEssentialRuntimePermissionsIfNeeded()
        requestExactAlarmPermissionIfNeeded()
        consumeQuickAddIntent(intent)
        setContent {
            DayLineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val quickAdd = quickAddSignal.value
                    val viewModel: TaskViewModel = viewModel(
                        factory = TaskViewModelFactory(TaskStore(this))
                    )
                    DayLinePager(viewModel, quickAddSignal = quickAdd)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeQuickAddIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        stopWalkMeterBackgroundTrackingService()
    }

    override fun onStop() {
        super.onStop()
        ensureWalkMeterBackgroundTracking()
    }

    private fun consumeQuickAddIntent(intent: Intent?) {
        if (intent?.action == ACTION_QUICK_ADD) {
            quickAddSignal.value += 1
            intent.action = null
            setIntent(intent)
        }
    }

    companion object {
        const val ACTION_QUICK_ADD = "com.toutakun04.dayline.action.QUICK_ADD_TASK"
    }
}

class WalkMeterBackgroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var sensorListener: SensorEventListener? = null
    private val cadenceSamples = mutableListOf<StepSample>()
    @Volatile
    private var walkMeterState: WalkMeterState = WalkMeterState()
    @Volatile
    private var stateLoaded: Boolean = false
    private var lastNotifiedSteps: Int = -1

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        ensureNotificationChannel()
        serviceScope.launch {
            val loaded = loadWalkMeterState(this@WalkMeterBackgroundService)
            val installSanitized = if (this@WalkMeterBackgroundService.shouldResetWalkMeterStateFromRestore(loaded)) {
                resetWalkMeterSnapshotForRestore(loaded)
            } else {
                loaded
            }
            val normalized = normalizeWalkMeterStateForToday(installSanitized)
            walkMeterState = normalized
            stateLoaded = true
            if (normalized != loaded) {
                saveWalkMeterState(this@WalkMeterBackgroundService, normalized)
            }
            updateForegroundNotification(normalized.lastMeasuredStepsToday)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, null -> Unit
            else -> return START_NOT_STICKY
        }

        if (!hasActivityRecognitionPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (sensorManager == null || stepCounterSensor == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        startAsForeground()
        registerSensorListenerIfNeeded()
        return START_STICKY
    }

    override fun onDestroy() {
        val snapshot = walkMeterState
        val shouldPersist = stateLoaded
        sensorListener?.let { listener ->
            sensorManager?.unregisterListener(listener)
        }
        sensorListener = null
        cadenceSamples.clear()
        if (shouldPersist) {
            CoroutineScope(Dispatchers.IO).launch {
                saveWalkMeterState(this@WalkMeterBackgroundService, snapshot)
            }
        }
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startAsForeground() {
        val notification = buildNotification(walkMeterState.lastMeasuredStepsToday.coerceAtLeast(0))
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        }.onFailure {
            stopSelf()
        }
    }

    private fun registerSensorListenerIfNeeded() {
        if (sensorListener != null) return
        val manager = sensorManager ?: return
        val sensor = stepCounterSensor ?: return
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (!hasActivityRecognitionPermission()) {
                    sensorManager?.unregisterListener(this)
                    stopSelf()
                    return
                }
                val rawStepCount = event?.values?.firstOrNull() ?: return
                if (!stateLoaded) return

                val nowMs = System.currentTimeMillis()
                val currentState = normalizeWalkMeterStateForToday(walkMeterState)
                if (currentState != walkMeterState) {
                    walkMeterState = currentState
                }

                val todayDate = LocalDate.now()
                val today = todayDate.toString()
                val currentGoal = normalizeDailyWalkGoal(currentState.dailyStepGoal)
                val currentStrideGoal = normalizeDailyStrideGoal(currentState.dailyStrideGoal)
                val updatedState = when {
                    currentState.baselineDate != today -> {
                        currentState.copy(
                            baselineDate = today,
                            baselineStepCount = rawStepCount,
                            dailyStepGoal = currentGoal,
                            dailyStrideGoal = currentStrideGoal
                        )
                    }

                    currentState.baselineDate.isBlank() ||
                        !currentState.baselineStepCount.isFinite() ||
                        currentState.baselineStepCount < 0f -> {
                        currentState.copy(
                            baselineDate = today,
                            baselineStepCount = rawStepCount,
                            dailyStepGoal = currentGoal,
                            dailyStrideGoal = currentStrideGoal
                        )
                    }

                    rawStepCount < currentState.baselineStepCount -> {
                        val carryForwardSteps = currentState.lastMeasuredStepsToday.coerceAtLeast(0)
                        val adjustedBaseline = rawStepCount - carryForwardSteps.toFloat()
                        currentState.copy(
                            baselineDate = today,
                            baselineStepCount = adjustedBaseline,
                            dailyStepGoal = currentGoal,
                            dailyStrideGoal = currentStrideGoal
                        )
                    }

                    else -> currentState.copy(
                        dailyStepGoal = currentGoal,
                        dailyStrideGoal = currentStrideGoal
                    )
                }

                val base = if (
                    updatedState.baselineDate == today &&
                    updatedState.baselineStepCount.isFinite() &&
                    updatedState.baselineStepCount >= 0f
                ) {
                    updatedState.baselineStepCount
                } else {
                    rawStepCount
                }
                val measuredStepsToday = (rawStepCount - base).coerceAtLeast(0f).toInt()
                val stepDeltaNow = (measuredStepsToday - currentState.lastMeasuredStepsToday).coerceAtLeast(0)

                cadenceSamples += StepSample(
                    timestampMs = nowMs,
                    totalSteps = rawStepCount
                )
                val cutoffMs = nowMs - 120_000L
                while (cadenceSamples.size > 2 && cadenceSamples.first().timestampMs < cutoffMs) {
                    cadenceSamples.removeAt(0)
                }
                val measuredCadence = cadenceSamples.toCadenceStepsPerMinute()
                val effectiveCadence = measuredCadence ?: updatedState.lastCadenceSpm.takeIf { it > 0f }
                val detectedIntensity = inferIntensityFromCadenceWithHysteresis(
                    stepsPerMinute = effectiveCadence,
                    activityType = "Walking",
                    previousIntensity = updatedState.lastIntensity
                )
                val computedStrideScore = calculateStridePoints(
                    steps = measuredStepsToday,
                    cadence = effectiveCadence,
                    intensity = detectedIntensity
                )
                val dayRolledOver = updatedState.baselineDate != currentState.baselineDate
                val stableStrideScore = if (dayRolledOver) {
                    computedStrideScore.coerceAtLeast(0)
                } else {
                    maxOf(updatedState.lastStrideScore.coerceAtLeast(0), computedStrideScore)
                }
                val stableMoveMinutes = updateDailyMoveMinutes(
                    previousMinutes = currentState.dailyMoveMinutes,
                    stepDelta = stepDeltaNow,
                    cadenceSpm = effectiveCadence,
                    dayRolledOver = dayRolledOver
                )
                val updatedWeeklyStrideScores = upsertDailyStrideScore(
                    scores = updatedState.weeklyStrideScores,
                    date = todayDate,
                    score = stableStrideScore,
                    referenceDate = todayDate
                )

                val stateWithSnapshot = updatedState.copy(
                    lastMeasuredStepsToday = measuredStepsToday,
                    lastMeasuredRawSteps = rawStepCount,
                    dailyMoveMinutes = stableMoveMinutes,
                    lastCadenceSpm = effectiveCadence ?: 0f,
                    lastIntensity = detectedIntensity,
                    lastStrideScore = stableStrideScore,
                    weeklyStrideScores = updatedWeeklyStrideScores,
                    lastUpdatedAtMs = nowMs
                )
                walkMeterState = stateWithSnapshot

                if (shouldPersistWalkMeterState(currentState, stateWithSnapshot)) {
                    serviceScope.launch {
                        saveWalkMeterState(this@WalkMeterBackgroundService, stateWithSnapshot)
                        updateForegroundNotification(measuredStepsToday)
                    }
                } else if ((measuredStepsToday - lastNotifiedSteps).absoluteValue >= 10) {
                    updateForegroundNotification(measuredStepsToday)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        val registered = manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        if (registered) {
            sensorListener = listener
        }
    }

    private fun updateForegroundNotification(stepsToday: Int) {
        lastNotifiedSteps = stepsToday
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        runCatching {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification(stepsToday))
        }
    }

    private fun buildNotification(stepsToday: Int): android.app.Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_walk_notification)
            .setContentTitle(getString(R.string.walk_meter_tracking_title))
            .setContentText(getString(R.string.walk_meter_tracking_content, stepsToday.coerceAtLeast(0)))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.walk_meter_tracking_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.walk_meter_tracking_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.toutakun04.dayline.action.WALK_METER_TRACK_START"
        const val ACTION_STOP = "com.toutakun04.dayline.action.WALK_METER_TRACK_STOP"
        private const val CHANNEL_ID = "walk_meter_tracking"
        private const val NOTIFICATION_ID = 40961
    }
}

private fun ComponentActivity.requestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
}

private fun ComponentActivity.requestEssentialRuntimePermissionsIfNeeded() {
    val missing = mutableListOf<String>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        missing += Manifest.permission.POST_NOTIFICATIONS
    }

    val fineGranted = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fineGranted && !coarseGranted) {
        missing += Manifest.permission.ACCESS_FINE_LOCATION
        missing += Manifest.permission.ACCESS_COARSE_LOCATION
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
    ) {
        missing += Manifest.permission.ACTIVITY_RECOGNITION
    }

    if (missing.isNotEmpty()) {
        requestPermissions(missing.distinct().toTypedArray(), 1000)
    }
}

private fun ComponentActivity.requestLocationPermissionIfNeeded() {
    val fine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1002
        )
    }
}

private fun ComponentActivity.requestActivityRecognitionPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1003)
        }
    }
}

private fun Context.hasActivityRecognitionPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
}

private fun ComponentActivity.ensureWalkMeterBackgroundTracking() {
    if (!hasActivityRecognitionPermission()) return
    val sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
    startWalkMeterBackgroundTrackingService()
}

private fun Context.startWalkMeterBackgroundTrackingService() {
    val intent = Intent(this, WalkMeterBackgroundService::class.java).apply {
        action = WalkMeterBackgroundService.ACTION_START
    }
    runCatching {
        ContextCompat.startForegroundService(this, intent)
    }
}

private fun Context.stopWalkMeterBackgroundTrackingService() {
    runCatching {
        stopService(Intent(this, WalkMeterBackgroundService::class.java))
    }
}

private fun ComponentActivity.requestExactAlarmPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = getSystemService(android.app.AlarmManager::class.java)
        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            } catch (_: Exception) {
                // If the system blocks the intent, fail silently.
            }
        }
    }
}

@Serializable
enum class RepeatRule {
    NONE,
    DAILY,
    WEEKDAYS,
    WEEKLY,
    MONTHLY
}

@Serializable
data class Task(
    val id: Long,
    val template: Boolean,
    val templateId: Long? = null,
    val title: String,
    val time: String,
    val date: String,
    val completed: Boolean = false,
    val category: String = "",
    val color: String = "#F9C74F",
    val tags: List<String> = emptyList(),
    val repeat: RepeatRule = RepeatRule.NONE,
    val notes: String = "",
    val reminders: List<Int> = emptyList(),
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class ActivityLogEntry(
    val loggedAt: Long = System.currentTimeMillis(),
    val type: String = "",
    val intensity: String = "",
    val durationMinutes: String = "",
    val steps: String = "",
    val distanceKm: String = "",
    val calories: String = "",
    val stridePoints: String = "",
    val notes: String = ""
)

@Serializable
data class ActivityHistory(
    val entries: List<ActivityLogEntry> = emptyList()
)

private const val DEFAULT_DAILY_STEP_GOAL = 6_000
private const val DEFAULT_DAILY_INTENSITY_GOAL = 30

@Serializable
data class WalkMeterState(
    val baselineDate: String = "",
    val baselineStepCount: Float = -1f,
    val dailyStepGoal: Int = DEFAULT_DAILY_STEP_GOAL,
    val dailyStrideGoal: Int = DEFAULT_DAILY_INTENSITY_GOAL,
    val dailyMoveMinutes: Float = 0f,
    val lastMeasuredStepsToday: Int = 0,
    val lastMeasuredRawSteps: Float = -1f,
    val lastCadenceSpm: Float = 0f,
    val lastIntensity: String = "Light",
    val lastStrideScore: Int = 0,
    val weeklyStrideScores: Map<String, Int> = emptyMap(),
    val lastUpdatedAtMs: Long = 0L
)

@Serializable
data class HealthMetrics(
    val weightKg: String = "",
    val heightCm: String = "",
    val age: String = "",
    val sex: String = "",
    val bmi: String = "",
    val bloodSugar: String = "",
    val bloodPressureSys: String = "",
    val bloodPressureDia: String = "",
    val bodyFat: String = ""
)

@Serializable
data class HealthLogEntry(
    val loggedAt: Long = System.currentTimeMillis(),
    val weightKg: String = "",
    val heightCm: String = "",
    val age: String = "",
    val sex: String = "",
    val bmi: String = "",
    val bloodSugar: String = "",
    val bloodPressureSys: String = "",
    val bloodPressureDia: String = "",
    val bodyFat: String = ""
)

@Serializable
data class HealthHistory(
    val entries: List<HealthLogEntry> = emptyList()
)

private val HealthPanelBg = Color(0xFFFFFDF2)
private val HealthCardBg = Color(0xFFFFF6D8)
private val HealthChartBg = Color(0xFFFFFBEA)
private val HealthBorder = Color(0xFFF1E2B7)
private val HealthPillBg = Color(0xFFFFEFB8)
private val HealthPillText = Color(0xFF5A4608)
private val ActivityPanelBg = Color(0xFFF5F9FF)
private val ActivityCardBg = Color(0xFFE9F2FF)
private val ActivityChartBg = Color(0xFFF2F7FF)
private val ActivityBorder = Color(0xFFC6DAF8)
private val ActivityPillBg = Color(0xFFD9E9FF)
private val ActivityPillText = Color(0xFF123B78)

private data class ActivityPalette(
    val panel: Color,
    val card: Color,
    val chart: Color,
    val border: Color,
    val pillBg: Color,
    val pillText: Color,
    val goalPanel: Color,
    val gaugeTrackLight: Color,
    val gaugeTrackMid: Color,
    val gaugeTrackHigh: Color
)

private data class HealthPalette(
    val panel: Color,
    val card: Color,
    val chart: Color,
    val border: Color,
    val pillBg: Color,
    val pillText: Color
)

@Composable
private fun activityPalette(): ActivityPalette {
    val scheme = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) {
        ActivityPalette(
            panel = scheme.surface,
            card = scheme.surfaceVariant,
            chart = scheme.surface,
            border = scheme.outline.copy(alpha = 0.6f),
            pillBg = scheme.primary.copy(alpha = 0.16f),
            pillText = scheme.primary,
            goalPanel = scheme.surfaceVariant,
            gaugeTrackLight = Color(0xFF1B5E20).copy(alpha = 0.42f),
            gaugeTrackMid = Color(0xFFFBC02D).copy(alpha = 0.32f),
            gaugeTrackHigh = Color(0xFFF57C00).copy(alpha = 0.26f)
        )
    } else {
        ActivityPalette(
            panel = ActivityPanelBg,
            card = ActivityCardBg,
            chart = ActivityChartBg,
            border = ActivityBorder,
            pillBg = ActivityPillBg,
            pillText = ActivityPillText,
            goalPanel = Color(0xFFEFF5FF),
            gaugeTrackLight = Color(0xFFE2F1E5),
            gaugeTrackMid = Color(0xFFFFF1D6),
            gaugeTrackHigh = Color(0xFFFFDFDF)
        )
    }
}

@Composable
private fun healthPalette(): HealthPalette {
    val scheme = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) {
        HealthPalette(
            panel = scheme.surface,
            card = scheme.surfaceVariant,
            chart = scheme.surface,
            border = scheme.outline.copy(alpha = 0.6f),
            pillBg = scheme.secondary.copy(alpha = 0.2f),
            pillText = scheme.secondary
        )
    } else {
        HealthPalette(
            panel = HealthPanelBg,
            card = HealthCardBg,
            chart = HealthChartBg,
            border = HealthBorder,
            pillBg = HealthPillBg,
            pillText = HealthPillText
        )
    }
}

class TaskStore(private val context: Context) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun load(): List<Task> {
        val prefs = context.dataStore.data.first()
        val raw = prefs[TASK_STATE_KEY] ?: return emptyList()
        val loaded = runCatching { json.decodeFromString<List<Task>>(raw) }.getOrElse { emptyList() }
        ReminderScheduler.rescheduleAll(context, loaded)
        TaskAlarmScheduler.rescheduleAll(context, loaded)
        return loaded
    }

    suspend fun save(tasks: List<Task>) {
        context.dataStore.edit { prefs ->
            prefs[TASK_STATE_KEY] = json.encodeToString(tasks)
        }
        DailyTasksWidgetUpdater.updateAll(context, tasks)
        ReminderScheduler.rescheduleAll(context, tasks)
        TaskAlarmScheduler.rescheduleAll(context, tasks)
    }
}

class TaskViewModel(private val store: TaskStore) : ViewModel() {
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            tasks = store.load()
        }
    }

    fun saveTasks(updated: List<Task>) {
        if (updated == tasks) return
        tasks = updated
        viewModelScope.launch {
            store.save(updated)
        }
    }

    fun addTask(task: Task) {
        addTasks(listOf(task))
    }

    fun addTasks(newTasks: List<Task>) {
        if (newTasks.isEmpty()) return
        saveTasks(tasks + newTasks)
    }

    fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index < 0) return
        if (tasks[index] == task) return
        val updated = tasks.toMutableList()
        updated[index] = task
        saveTasks(updated)
    }

    fun deleteTask(taskId: Long) {
        if (tasks.none { it.id == taskId }) return
        saveTasks(tasks.filterNot { it.id == taskId })
    }

    fun deleteSeries(templateId: Long) {
        val updated = tasks.filterNot { it.id == templateId || it.templateId == templateId }
        if (updated.size == tasks.size) return
        saveTasks(updated)
    }

    fun ensureInstancesForRange(start: LocalDate, end: LocalDate) {
        val templates = tasks.filter { it.template && it.repeat != RepeatRule.NONE }
        if (templates.isEmpty()) return
        val dateList = generateDateRange(start, end)
        val newItems = mutableListOf<Task>()
        val existingTemplateDatePairs = tasks.asSequence()
            .filter { !it.template && it.templateId != null }
            .map { it.templateId!! to it.date }
            .toHashSet()

        for (template in templates) {
            val seed = LocalDate.parse(template.date)
            for (date in dateList) {
                if (!shouldOccur(template.repeat, seed, date)) continue
                val key = template.id to date.toString()
                if (existingTemplateDatePairs.add(key)) {
                    newItems += template.toInstanceFor(date)
                }
            }
        }

        if (newItems.isNotEmpty()) {
            addTasks(newItems)
        }
    }
}

class TaskViewModelFactory(private val store: TaskStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(store) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayLinePager(viewModel: TaskViewModel, quickAddSignal: Int) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    var showMissingBmiDialog by remember { mutableStateOf(false) }
    var hasConfiguredBmi by remember { mutableStateOf<Boolean?>(null) }
    var skipMissingBmiPromptOnce by remember { mutableStateOf(false) }

    LaunchedEffect(quickAddSignal) {
        if (quickAddSignal > 0) {
            pagerState.animateScrollToPage(0)
        }
    }

    LaunchedEffect(Unit) {
        hasConfiguredBmi = loadHealthHistory(context).hasConfiguredBmi()
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1 || pagerState.currentPage == 2) {
            if (skipMissingBmiPromptOnce) {
                skipMissingBmiPromptOnce = false
                return@LaunchedEffect
            }

            val shouldRefreshFromStorage = pagerState.currentPage == 2 || hasConfiguredBmi != true
            val resolvedHasBmi = if (shouldRefreshFromStorage) {
                loadHealthHistory(context).hasConfiguredBmi().also { hasConfiguredBmi = it }
            } else {
                true
            }
            showMissingBmiDialog = !resolvedHasBmi
        } else {
            showMissingBmiDialog = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 1,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> DayLineApp(viewModel, quickAddSignal = quickAddSignal)
                1 -> ActivityTrackingScreen(isVisible = pagerState.currentPage == 1)
                else -> HealthTrackingScreen()
            }
        }
        PageTabs(
            pagerState = pagerState,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            onTabSelected = { page ->
                if (pagerState.currentPage == page) return@PageTabs
                coroutineScope.launch {
                    pagerState.scrollToPage(page)
                }
            }
        )
    }

    if (showMissingBmiDialog) {
        AlertDialog(
            onDismissRequest = { showMissingBmiDialog = false },
            title = { Text("BMI Setup Needed") },
            text = { Text("Add your health details in Health Tracking to generate BMI.") },
            confirmButton = {
                TextButton(onClick = {
                    showMissingBmiDialog = false
                    skipMissingBmiPromptOnce = true
                    coroutineScope.launch {
                        pagerState.scrollToPage(2)
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayLineApp(viewModel: TaskViewModel, quickAddSignal: Int) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var weatherDisplay by remember { mutableStateOf<WeatherDisplay?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var viewMode by remember { mutableStateOf(ViewMode.Today) }
    var viewDate by remember { mutableStateOf(LocalDate.now()) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(StatusFilter.All) }
    var categoryFilter by remember { mutableStateOf("All") }
    var hideCompleted by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var pendingDelete by remember { mutableStateOf<Task?>(null) }
    var showSeriesDelete by remember { mutableStateOf(false) }
    var pendingDeleteConfirm by remember { mutableStateOf<Task?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingComplete by remember { mutableStateOf<Task?>(null) }
    var showCompleteConfirm by remember { mutableStateOf(false) }
    var completionQuote by remember { mutableStateOf("") }

    LaunchedEffect(quickAddSignal) {
        if (quickAddSignal > 0) {
            editingTask = null
            viewMode = ViewMode.Today
            viewDate = LocalDate.now()
            showDialog = true
        }
    }

    val dateRange by remember(viewMode, viewDate) {
        derivedStateOf { getRange(viewMode, viewDate) }
    }

    LaunchedEffect(dateRange) {
        viewModel.ensureInstancesForRange(dateRange.first, dateRange.second)
    }

    LaunchedEffect(Unit) {
        weatherDisplay = WeatherRepository.loadWeatherDisplay(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    weatherDisplay = WeatherRepository.loadWeatherDisplay(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val categories by remember(viewModel.tasks) {
        derivedStateOf {
            viewModel.tasks.mapNotNull { it.category.takeIf { cat -> cat.isNotBlank() } }
                .distinct()
                .sorted()
        }
    }

    val visibleTasks by remember(
        viewModel.tasks,
        dateRange,
        searchQuery,
        statusFilter,
        categoryFilter,
        hideCompleted
    ) {
        derivedStateOf {
            viewModel.tasks
                .filter { !it.template }
                .filter { isWithinRange(it.date, dateRange) }
                .filter { task ->
                    if (searchQuery.isBlank()) true else {
                        val haystack = listOf(
                            task.title,
                            task.category,
                            task.tags.joinToString(" "),
                            task.notes
                        ).joinToString(" ").lowercase(Locale.getDefault())
                        haystack.contains(searchQuery.lowercase(Locale.getDefault()))
                    }
                }
                .filter { task ->
                    when (statusFilter) {
                        StatusFilter.All -> true
                        StatusFilter.Active -> !task.completed
                        StatusFilter.Completed -> task.completed
                    }
                }
                .filter { task ->
                    if (categoryFilter == "All") true else task.category == categoryFilter
                }
                .filter { task ->
                    if (!hideCompleted) true else !task.completed
                }
                .sortedWith(compareBy<Task> { it.date }.thenBy { it.order }.thenBy { it.time })
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .size(56.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = {
                editingTask = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
            GreetingHeader(viewMode, viewDate, weatherDisplay)

                Spacer(modifier = Modifier.height(16.dp))

                FilterRow(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    statusFilter = statusFilter,
                    onStatusChange = { statusFilter = it },
                    categories = categories,
                    categoryFilter = categoryFilter,
                    onCategoryChange = { categoryFilter = it },
                    hideCompleted = hideCompleted,
                    onHideCompletedChange = { hideCompleted = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ViewToggle(
                    viewMode = viewMode,
                    onViewChange = { viewMode = it },
                    viewDate = viewDate,
                    onDatePicked = { viewDate = it; viewMode = ViewMode.Date }
                )

                Spacer(modifier = Modifier.height(12.dp))

                StatsBar(visibleTasks = visibleTasks, allTasks = viewModel.tasks, dateRange = dateRange)

                Spacer(modifier = Modifier.height(8.dp))

                if (visibleTasks.isEmpty()) {
                    EmptyState()
                } else {
                TaskList(
                    tasks = visibleTasks,
                    onEdit = {
                        editingTask = it
                        showDialog = true
                    },
                    onRequestComplete = { task ->
                        completionQuote = pickCompletionQuote(task)
                        pendingComplete = task
                        showCompleteConfirm = true
                    },
                    onRequestDelete = { task ->
                        pendingDeleteConfirm = task
                        showDeleteConfirm = true
                    }
                )
            }
        }
    }
    }

    if (showDialog) {
        TaskDialog(
            task = editingTask,
            allTasks = viewModel.tasks,
            onClearCategory = { categoryToClear ->
                if (categoryToClear.isBlank()) return@TaskDialog
                val updated = viewModel.tasks.map { existing ->
                    if (existing.category == categoryToClear) {
                        existing.copy(category = "")
                    } else {
                        existing
                    }
                }
                if (updated != viewModel.tasks) {
                    viewModel.saveTasks(updated)
                }
            },
            onDismiss = { showDialog = false },
            onSave = { data ->
                coroutineScope.launch {
                    if (editingTask == null) {
                        val items = buildTasksFromForm(data)
                        viewModel.addTasks(items)
                    } else {
                        val existing = editingTask
                        if (existing != null) {
                            val updated = existing.copy(
                                title = data.title,
                                time = data.time,
                                date = data.date.toString(),
                                category = data.category,
                                color = data.color,
                                tags = data.tags,
                                repeat = data.repeat,
                                notes = data.notes,
                                completed = data.completed,
                                reminders = data.reminders,
                                order = existing.order
                            )
                            updateTaskWithCompletion(context, viewModel, existing, updated)
                        }
                    }
                }
                showDialog = false
            }
        )
    }

    if (showCompleteConfirm && pendingComplete != null) {
        AlertDialog(
            onDismissRequest = {
                showCompleteConfirm = false
                pendingComplete = null
            },
            title = { Text("Mark task completed?") },
            text = {
                Text("\"$completionQuote\"")
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = pendingComplete
                    if (target != null) {
                        completeTask(context, viewModel, target)
                    }
                    showCompleteConfirm = false
                    pendingComplete = null
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteConfirm = false
                    pendingComplete = null
                }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirm && pendingDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                pendingDeleteConfirm = null
            },
            title = { Text("Delete task?") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(onClick = {
                    val target = pendingDeleteConfirm
                    if (target?.templateId != null) {
                        pendingDelete = target
                        showSeriesDelete = true
                    } else if (target != null) {
                        deleteTaskWithAlarms(context, viewModel, target)
                    }
                    showDeleteConfirm = false
                    pendingDeleteConfirm = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    pendingDeleteConfirm = null
                }) { Text("Cancel") }
            }
        )
    }

    if (showSeriesDelete && pendingDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showSeriesDelete = false
                pendingDelete = null
            },
            title = { Text("Delete recurring task?") },
            text = { Text("Do you want to delete the entire series or just this occurrence?") },
            confirmButton = {
                TextButton(onClick = {
                    val target = pendingDelete
                    if (target?.templateId != null) {
                        deleteSeriesWithAlarms(context, viewModel, target.templateId)
                    }
                    showSeriesDelete = false
                    pendingDelete = null
                }) { Text("Entire Series") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val target = pendingDelete
                    if (target != null) {
                        deleteTaskWithAlarms(context, viewModel, target)
                    }
                    showSeriesDelete = false
                    pendingDelete = null
                }) { Text("Just This") }
            }
        )
    }
}

@Composable
fun GreetingHeader(viewMode: ViewMode, viewDate: LocalDate, weather: WeatherDisplay?) {
    val now = LocalTime.now()
    val greeting = when {
        now.hour < 12 -> "Good Morning"
        now.hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    val todayLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    Column {
        Text(text = greeting, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = todayLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        if (weather != null) {
            WeatherRow(weather)
        }
    }
}

@Composable
fun WeatherRow(weather: WeatherDisplay) {
    val icon = weatherIconVector(weather.icon)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${weather.location} \u2022 ${weather.temperature}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun weatherIconVector(type: WeatherIconType): ImageVector {
    return when (type) {
        WeatherIconType.Sun -> Icons.Filled.WbSunny
        WeatherIconType.Moon -> Icons.Filled.NightsStay
        WeatherIconType.Sunrise -> Icons.Filled.WbTwilight
        WeatherIconType.Sunset -> Icons.Filled.WbTwilight
        WeatherIconType.Cloud -> Icons.Filled.Cloud
        WeatherIconType.Rain -> Icons.Filled.Umbrella
        WeatherIconType.Storm -> Icons.Filled.Thunderstorm
        WeatherIconType.Snow -> Icons.Filled.AcUnit
        WeatherIconType.LocationOff -> Icons.Filled.LocationOff
    }
}

@Composable
fun FilterRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: StatusFilter,
    onStatusChange: (StatusFilter) -> Unit,
    categories: List<String>,
    categoryFilter: String,
    onCategoryChange: (String) -> Unit,
    hideCompleted: Boolean,
    onHideCompletedChange: (Boolean) -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("Search tasks, categories, notes") },
            modifier = Modifier.fillMaxWidth(),
            colors = framedTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatusChip("All", statusFilter == StatusFilter.All) { onStatusChange(StatusFilter.All) }
            StatusChip("Active", statusFilter == StatusFilter.Active) { onStatusChange(StatusFilter.Active) }
            StatusChip("Done", statusFilter == StatusFilter.Completed) { onStatusChange(StatusFilter.Completed) }
            StatusChip("Hide Completed", hideCompleted) { onHideCompletedChange(!hideCompleted) }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            item {
                StatusChip("All Categories", categoryFilter == "All") { onCategoryChange("All") }
            }
            items(categories) { cat ->
                StatusChip(cat, categoryFilter == cat) { onCategoryChange(cat) }
            }
        }
    }
}

@Composable
fun StatusChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val background = if (selected) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
    val border = if (selected) accent.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline
    Text(
        text = label,
        modifier = Modifier
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .background(background, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
fun ViewToggle(
    viewMode: ViewMode,
    onViewChange: (ViewMode) -> Unit,
    viewDate: LocalDate,
    onDatePicked: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StatusChip("Today", viewMode == ViewMode.Today) { onViewChange(ViewMode.Today) }
        StatusChip("Tomorrow", viewMode == ViewMode.Tomorrow) { onViewChange(ViewMode.Tomorrow) }
        StatusChip("Week", viewMode == ViewMode.Week) { onViewChange(ViewMode.Week) }
        StatusChip("Pick Date", viewMode == ViewMode.Date) {
            showDatePicker(context, viewDate, onDatePicked)
        }
    }
}

@Composable
fun StatsBar(visibleTasks: List<Task>, allTasks: List<Task>, dateRange: Pair<LocalDate, LocalDate>) {
    val total = allTasks.count { !it.template && isWithinRange(it.date, dateRange) }
    val completed = allTasks.count { !it.template && isWithinRange(it.date, dateRange) && it.completed }
    Text(
        text = "Showing ${visibleTasks.size} of $total tasks \u2022 $completed completed",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "No tasks match this view.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onEdit: (Task) -> Unit,
    onRequestComplete: (Task) -> Unit,
    onRequestDelete: (Task) -> Unit
) {
    val grouped = remember(tasks) { tasks.groupBy { it.date } }
    val sortedKeys = remember(grouped) { grouped.keys.sorted() }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sortedKeys.forEach { dateKey ->
            item(key = "header-$dateKey") {
                Text(
                    text = LocalDate.parse(dateKey).format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            items(
                grouped[dateKey] ?: emptyList(),
                key = { task -> task.id }
            ) { task ->
                SwipeToDeleteTask(task, onEdit, onRequestComplete, onRequestDelete)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDeleteTask(
    task: Task,
    onEdit: (Task) -> Unit,
    onRequestComplete: (Task) -> Unit,
    onRequestDelete: (Task) -> Unit
) {
    val dismissState = rememberDismissState(confirmStateChange = { value ->
        if (value == DismissValue.DismissedToEnd) {
            if (!task.completed) {
                onRequestComplete(task)
            }
        } else if (value == DismissValue.DismissedToStart) {
            onRequestDelete(task)
        }
        false
    })

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            val offset = dismissState.offset.value
            if (offset != 0f) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val maxWidthPx = constraints.maxWidth.toFloat().takeIf { it > 0f }
                        ?: with(LocalDensity.current) { 400.dp.toPx() }
                    val fraction = (offset.absoluteValue / maxWidthPx).coerceIn(0f, 1f)
                    val swipingRight = offset > 0f
                    val bg = if (swipingRight) {
                        Color(0xFF2E7D32).copy(alpha = fraction)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = fraction)
                    }
                    val fg = if (swipingRight) Color.White else MaterialTheme.colorScheme.onError
                    val icon = if (swipingRight) Icons.Filled.Check else Icons.Filled.Delete
                    val label = if (swipingRight) "Complete" else "Delete"
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bg, RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = if (swipingRight) Arrangement.Start else Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, color = fg, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        dismissContent = {
            TaskCard(task, onEdit, onRequestComplete, onRequestDelete)
        }
    )
}

@Composable
fun TaskCard(
    task: Task,
    onEdit: (Task) -> Unit,
    onRequestComplete: (Task) -> Unit,
    onRequestDelete: (Task) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(88.dp)) {
                        Text(
                            text = formatTime(task.time),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Start
                        )
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                if (task.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.tags.joinToString(prefix = "#", separator = " #"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = task.notes, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                if (task.category.isNotBlank() || task.repeat != RepeatRule.NONE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val meta = buildList {
                        if (task.category.isNotBlank()) add("Category: ${task.category}")
                        if (task.repeat != RepeatRule.NONE) add("Recurring")
                    }.joinToString(" \u2022 ")
                    Text(text = meta, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        enabled = !task.completed,
                        onClick = {
                            menuExpanded = false
                            onEdit(task)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Mark completed") },
                        enabled = !task.completed,
                        onClick = {
                            menuExpanded = false
                            onRequestComplete(task)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        enabled = !task.completed,
                        onClick = {
                            menuExpanded = false
                            onRequestDelete(task)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PageTabs(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        Triple("Tasks", Icons.Filled.Search, "Tasks"),
        Triple("Activity", Icons.Filled.FitnessCenter, "Activity"),
        Triple("Health", Icons.Filled.Favorite, "Health")
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = pagerState.currentPage == index
                val tabBg by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
                    label = "page_tab_bg"
                )
                val tabFg by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "page_tab_fg"
                )
                Surface(
                    color = tabBg,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(tab.second, contentDescription = tab.third, tint = tabFg, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.first,
                            color = tabFg,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

private data class ActivityWeeklySummary(
    val sessions: Int,
    val durationMinutes: Float,
    val steps: Float,
    val distanceKm: Float,
    val calories: Float,
    val stridePoints: Float
)

private data class StepSample(
    val timestampMs: Long,
    val totalSteps: Float
)

private data class ActivityLogDraft(
    val type: String = "Walking",
    val intensity: String = "Moderate",
    val durationMinutes: String = "",
    val steps: String = "",
    val distanceKm: String = "",
    val calories: String = "",
    val notes: String = ""
)

private data class DerivedActivityLogValues(
    val durationMinutes: Float,
    val steps: Int,
    val distanceKm: Float,
    val calories: Float,
    val stridePoints: Int
)

private data class ActivityEstimationProfile(
    val distanceKmPerStep: Float,
    val caloriesPerStep: Float,
    val minuteCalorieFloor: Float,
    val baseCadenceSpm: Float
)

private data class WalkGoalDayStatus(
    val date: LocalDate,
    val steps: Int,
    val moveScore: Int,
    val hitGoal: Boolean
)

private enum class WalkGoalField {
    DailySteps,
    Intensity
}

@Composable
fun ActivityTrackingScreen(isVisible: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val sensorManager = remember(context) { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val stepCounterSensor = remember(sensorManager) { sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }
    var activityType by remember { mutableStateOf("Walking") }
    var intensity by remember { mutableStateOf("Moderate") }
    var showManualLogDialog by remember { mutableStateOf(false) }
    var manualLogDraft by remember { mutableStateOf(ActivityLogDraft()) }
    var history by remember { mutableStateOf(ActivityHistory()) }
    var healthHistory by remember { mutableStateOf(HealthHistory()) }
    var walkMeterState by remember { mutableStateOf(WalkMeterState()) }
    var walkMeterStateLoaded by remember { mutableStateOf(false) }
    var walkMeterStepsToday by remember { mutableStateOf<Int?>(null) }
    var walkMeterRawSteps by remember { mutableStateOf<Float?>(null) }
    val cadenceSamples = remember { mutableStateListOf<StepSample>() }
    var liveCadenceStepsPerMinute by remember { mutableStateOf<Float?>(null) }
    var liveDetectedIntensity by remember { mutableStateOf<String?>(null) }
    var liveWalkSessionStartAtMs by remember { mutableStateOf<Long?>(null) }
    var liveWalkSessionStartSteps by remember { mutableStateOf<Int?>(null) }
    var liveWalkLastStepAtMs by remember { mutableStateOf<Long?>(null) }
    var liveWalkPreviewEntry by remember { mutableStateOf<ActivityLogEntry?>(null) }
    var sessionActive by remember { mutableStateOf(false) }
    var sessionStartedAtMs by remember { mutableStateOf<Long?>(null) }
    var sessionStartRawSteps by remember { mutableStateOf<Float?>(null) }
    var sessionStepCount by remember { mutableStateOf(0) }
    var sessionDurationMinutes by remember { mutableStateOf(0f) }
    var walkPermissionGranted by remember { mutableStateOf(context.hasActivityRecognitionPermission()) }
    var saveStatus by remember { mutableStateOf<String?>(null) }
    var goalFieldToEdit by remember { mutableStateOf<WalkGoalField?>(null) }
    var goalFieldInput by remember { mutableStateOf("") }
    var showGoalSetupPrompt by remember { mutableStateOf(false) }
    var goalSetupPromptHandled by remember { mutableStateOf(false) }
    var goalSetupStepInput by remember { mutableStateOf("") }
    var goalSetupIntensityInput by remember { mutableStateOf("") }
    var latestBmi by remember { mutableStateOf<Float?>(null) }
    var idleCaloriesPerMinute by remember { mutableStateOf(defaultIdleCaloriesPerMinute()) }
    val palette = activityPalette()

    LaunchedEffect(Unit) {
        val loadedHistory = loadActivityHistory(context)
        history = loadedHistory
        val loadedWalkState = loadWalkMeterState(context)
        val installSanitizedWalkState = if (context.shouldResetWalkMeterStateFromRestore(loadedWalkState)) {
            resetWalkMeterSnapshotForRestore(loadedWalkState)
        } else {
            loadedWalkState
        }
        val normalizedWalkState = normalizeWalkMeterStateForToday(installSanitizedWalkState)
        val initialWalkState = if (walkPermissionGranted) {
            normalizedWalkState
        } else {
            resetWalkMeterSnapshotForPermissionGap(normalizedWalkState)
        }
        walkMeterState = initialWalkState
        walkMeterStateLoaded = true
        walkMeterStepsToday = initialWalkState.lastMeasuredStepsToday.takeIf { walkPermissionGranted }
        walkMeterRawSteps = initialWalkState.lastMeasuredRawSteps.takeIf { walkPermissionGranted && it >= 0f }
        liveCadenceStepsPerMinute = initialWalkState.lastCadenceSpm.takeIf { walkPermissionGranted && it > 0f }
        liveDetectedIntensity = if (walkPermissionGranted) {
            normalizeIntensityLabel(initialWalkState.lastIntensity)
        } else {
            null
        }
        if (initialWalkState != loadedWalkState) {
            saveWalkMeterState(context, initialWalkState)
        }
        val loadedHealth = loadHealthHistory(context)
        healthHistory = loadedHealth
        val bmiValue = loadedHealth.entries.lastOrNull()?.resolvedBmiValue()
        latestBmi = bmiValue
        idleCaloriesPerMinute = idleCaloriesPerMinuteFromBmi(bmiValue)
        val latest = loadedHistory.entries.lastOrNull()
        if (latest != null) {
            if (latest.type.isNotBlank()) activityType = latest.type
            if (latest.intensity.isNotBlank()) intensity = latest.intensity
        }
    }

    LaunchedEffect(activityType, liveCadenceStepsPerMinute) {
        liveDetectedIntensity = inferIntensityFromCadenceWithHysteresis(
            stepsPerMinute = liveCadenceStepsPerMinute,
            activityType = activityType,
            previousIntensity = liveDetectedIntensity ?: walkMeterState.lastIntensity
        )
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) return@LaunchedEffect
        val loadedHealth = loadHealthHistory(context)
        healthHistory = loadedHealth
        val bmiValue = loadedHealth.entries.lastOrNull()?.resolvedBmiValue()
        latestBmi = bmiValue
        idleCaloriesPerMinute = idleCaloriesPerMinuteFromBmi(bmiValue)
    }

    LaunchedEffect(
        walkMeterStateLoaded,
        latestBmi,
        walkMeterState.dailyStepGoal,
        walkMeterState.dailyStrideGoal
    ) {
        if (!walkMeterStateLoaded || latestBmi == null || goalSetupPromptHandled) return@LaunchedEffect
        if (!usesDefaultWalkGoals(walkMeterState.dailyStepGoal, walkMeterState.dailyStrideGoal)) return@LaunchedEffect

        goalSetupStepInput = normalizeDailyWalkGoal(walkMeterState.dailyStepGoal).toString()
        goalSetupIntensityInput = normalizeDailyStrideGoal(walkMeterState.dailyStrideGoal).toString()
        showGoalSetupPrompt = true
        goalSetupPromptHandled = true
    }

    LaunchedEffect(liveWalkSessionStartAtMs, liveWalkLastStepAtMs) {
        if (liveWalkSessionStartAtMs == null) return@LaunchedEffect
        while (liveWalkSessionStartAtMs != null) {
            val lastStepAt = liveWalkLastStepAtMs ?: break
            val idleMs = System.currentTimeMillis() - lastStepAt
            if (idleMs >= 90_000L) {
                liveWalkSessionStartAtMs = null
                liveWalkSessionStartSteps = null
                liveWalkLastStepAtMs = null
                break
            }
            delay(15_000L)
        }
    }

    DisposableEffect(lifecycleOwner, walkMeterStateLoaded) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                walkPermissionGranted = context.hasActivityRecognitionPermission()
                if (walkMeterStateLoaded) {
                    val normalized = normalizeWalkMeterStateForToday(walkMeterState)
                    val refreshed = if (walkPermissionGranted) {
                        normalized
                    } else {
                        resetWalkMeterSnapshotForPermissionGap(normalized)
                    }
                    if (refreshed != walkMeterState) {
                        walkMeterState = refreshed
                        coroutineScope.launch {
                            saveWalkMeterState(context, refreshed)
                        }
                    }
                    walkMeterStepsToday = refreshed.lastMeasuredStepsToday.takeIf { walkPermissionGranted }
                    walkMeterRawSteps = refreshed.lastMeasuredRawSteps.takeIf { walkPermissionGranted && it >= 0f }
                    liveCadenceStepsPerMinute = refreshed.lastCadenceSpm.takeIf { walkPermissionGranted && it > 0f }
                    liveDetectedIntensity = if (walkPermissionGranted) {
                        normalizeIntensityLabel(refreshed.lastIntensity)
                    } else {
                        null
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(sensorManager, stepCounterSensor, walkPermissionGranted, walkMeterStateLoaded) {
        if (!walkMeterStateLoaded) {
            onDispose { }
        } else if (sensorManager == null || stepCounterSensor == null || !walkPermissionGranted) {
            liveWalkSessionStartAtMs = null
            liveWalkSessionStartSteps = null
            liveWalkLastStepAtMs = null
            liveWalkPreviewEntry = null
            walkMeterStepsToday = null
            walkMeterRawSteps = null
            liveCadenceStepsPerMinute = null
            cadenceSamples.clear()
            liveDetectedIntensity = null
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (!context.hasActivityRecognitionPermission()) {
                        walkPermissionGranted = false
                        return
                    }
                    val rawStepCount = event?.values?.firstOrNull() ?: return
                    val nowMs = System.currentTimeMillis()
                    walkMeterRawSteps = rawStepCount
                    val todayDate = LocalDate.now()
                    val today = todayDate.toString()
                    val currentState = walkMeterState
                    val currentGoal = normalizeDailyWalkGoal(currentState.dailyStepGoal)
                    val currentStrideGoal = normalizeDailyStrideGoal(currentState.dailyStrideGoal)

                    val updatedState = when {
                        currentState.baselineDate != today -> {
                            currentState.copy(
                                baselineDate = today,
                                baselineStepCount = rawStepCount,
                                dailyStepGoal = currentGoal,
                                dailyStrideGoal = currentStrideGoal
                            )
                        }

                        currentState.baselineDate.isBlank() ||
                            !currentState.baselineStepCount.isFinite() ||
                            currentState.baselineStepCount < 0f -> {
                            currentState.copy(
                                baselineDate = today,
                                baselineStepCount = rawStepCount,
                                dailyStepGoal = currentGoal,
                                dailyStrideGoal = currentStrideGoal
                            )
                        }

                        rawStepCount < currentState.baselineStepCount -> {
                            val carryForwardSteps = maxOf(
                                walkMeterStepsToday ?: 0,
                                currentState.lastMeasuredStepsToday
                            ).coerceAtLeast(0)
                            val adjustedBaseline = rawStepCount - carryForwardSteps.toFloat()
                            currentState.copy(
                                baselineDate = today,
                                baselineStepCount = adjustedBaseline,
                                dailyStepGoal = currentGoal,
                                dailyStrideGoal = currentStrideGoal
                            )
                        }

                        else -> currentState.copy(
                            dailyStepGoal = currentGoal,
                            dailyStrideGoal = currentStrideGoal
                        )
                    }

                    val base = if (
                        updatedState.baselineDate == today &&
                        updatedState.baselineStepCount.isFinite() &&
                        updatedState.baselineStepCount >= 0f
                    ) {
                        updatedState.baselineStepCount
                    } else {
                        rawStepCount
                    }
                    val measuredStepsToday = (rawStepCount - base).coerceAtLeast(0f).toInt()
                    val stepDeltaNow = (measuredStepsToday - currentState.lastMeasuredStepsToday).coerceAtLeast(0)
                    walkMeterStepsToday = measuredStepsToday
                    if (stepDeltaNow > 0) {
                        liveWalkLastStepAtMs = nowMs
                    }

                    cadenceSamples += StepSample(
                        timestampMs = nowMs,
                        totalSteps = rawStepCount
                    )
                    val cutoffMs = nowMs - 120_000L
                    while (cadenceSamples.size > 2 && cadenceSamples.first().timestampMs < cutoffMs) {
                        cadenceSamples.removeAt(0)
                    }
                    val measuredCadence = cadenceSamples.toCadenceStepsPerMinute()
                    val effectiveCadence = measuredCadence ?: updatedState.lastCadenceSpm.takeIf { it > 0f }
                    val detectedIntensity = inferIntensityFromCadenceWithHysteresis(
                        stepsPerMinute = effectiveCadence,
                        activityType = activityType,
                        previousIntensity = updatedState.lastIntensity
                    )
                    val computedStrideScore = calculateStridePoints(
                        steps = measuredStepsToday,
                        cadence = effectiveCadence,
                        intensity = detectedIntensity
                    )
                    val dayRolledOver = updatedState.baselineDate != currentState.baselineDate
                    val stableStrideScore = if (dayRolledOver) {
                        computedStrideScore.coerceAtLeast(0)
                    } else {
                        maxOf(updatedState.lastStrideScore.coerceAtLeast(0), computedStrideScore)
                    }
                    val stableMoveMinutes = updateDailyMoveMinutes(
                        previousMinutes = currentState.dailyMoveMinutes,
                        stepDelta = stepDeltaNow,
                        cadenceSpm = effectiveCadence,
                        dayRolledOver = dayRolledOver
                    )
                    if (dayRolledOver) {
                        liveWalkSessionStartAtMs = null
                        liveWalkSessionStartSteps = null
                        liveWalkLastStepAtMs = null
                    }
                    if (stepDeltaNow > 0 && (effectiveCadence ?: 0f) >= 55f) {
                        if (liveWalkSessionStartAtMs == null || liveWalkSessionStartSteps == null) {
                            liveWalkSessionStartAtMs = nowMs
                            liveWalkSessionStartSteps = measuredStepsToday
                        }
                    }
                    val sessionStartAt = liveWalkSessionStartAtMs
                    val sessionStartSteps = liveWalkSessionStartSteps
                    if (sessionStartAt != null && sessionStartSteps != null) {
                        val sessionMinutes = ((nowMs - sessionStartAt).coerceAtLeast(0L) / 60_000f)
                        val sessionSteps = (measuredStepsToday - sessionStartSteps).coerceAtLeast(0)
                        if (sessionMinutes >= 2f && sessionSteps > 0) {
                            val liveSessionType = if (activityType == "Running") "Running" else "Walking"
                            val sessionDistanceKm = estimateDistanceKmFromSteps(sessionSteps, liveSessionType)
                            val sessionCalories = estimateWalkometerCalories(
                                steps = sessionSteps,
                                cadence = effectiveCadence,
                                intensity = detectedIntensity
                            )
                            val sessionScore = calculateStridePoints(
                                steps = sessionSteps,
                                cadence = effectiveCadence,
                                intensity = detectedIntensity
                            )
                            liveWalkPreviewEntry = ActivityLogEntry(
                                loggedAt = nowMs,
                                type = liveSessionType,
                                intensity = detectedIntensity,
                                durationMinutes = formatMetricValue(sessionMinutes),
                                steps = sessionSteps.toString(),
                                distanceKm = if (sessionDistanceKm > 0f) {
                                    String.format(Locale.getDefault(), "%.2f", sessionDistanceKm)
                                } else {
                                    ""
                                },
                                calories = if (sessionCalories > 0f) formatMetricValue(sessionCalories) else "",
                                stridePoints = sessionScore.toString(),
                                notes = "Live walk detected (2+ min)."
                            )
                        }
                    }
                    val updatedWeeklyStrideScores = upsertDailyStrideScore(
                        scores = updatedState.weeklyStrideScores,
                        date = todayDate,
                        score = stableStrideScore,
                        referenceDate = todayDate
                    )
                    liveCadenceStepsPerMinute = effectiveCadence
                    liveDetectedIntensity = detectedIntensity

                    val stateWithSnapshot = updatedState.copy(
                        lastMeasuredStepsToday = measuredStepsToday,
                        lastMeasuredRawSteps = rawStepCount,
                        dailyMoveMinutes = stableMoveMinutes,
                        lastCadenceSpm = effectiveCadence ?: 0f,
                        lastIntensity = detectedIntensity,
                        lastStrideScore = stableStrideScore,
                        weeklyStrideScores = updatedWeeklyStrideScores,
                        lastUpdatedAtMs = nowMs
                    )
                    walkMeterState = stateWithSnapshot
                    if (shouldPersistWalkMeterState(currentState, stateWithSnapshot)) {
                        coroutineScope.launch {
                            saveWalkMeterState(context, stateWithSnapshot)
                        }
                    }

                    if (sessionActive) {
                        val startRaw = sessionStartRawSteps ?: rawStepCount.also { sessionStartRawSteps = it }
                        val startMs = sessionStartedAtMs ?: nowMs.also { sessionStartedAtMs = it }
                        sessionStepCount = (rawStepCount - startRaw).coerceAtLeast(0f).toInt()
                        sessionDurationMinutes = ((nowMs - startMs).coerceAtLeast(0L) / 60_000f)
                        intensity = inferIntensityFromCadenceWithHysteresis(
                            stepsPerMinute = liveCadenceStepsPerMinute,
                            activityType = activityType,
                            previousIntensity = intensity
                        )
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(listener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose {
                sensorManager.unregisterListener(listener)
                if (walkMeterStateLoaded) {
                    coroutineScope.launch {
                        saveWalkMeterState(context, walkMeterState)
                    }
                }
            }
        }
    }

    val latestSavedEntry = history.entries.lastOrNull()
    val latestEntry = listOfNotNull(latestSavedEntry, liveWalkPreviewEntry)
        .maxByOrNull { it.loggedAt }
    val weeklySummary = remember(history.entries) { history.entries.toWeeklySummary() }
    val walkGoalSteps = normalizeDailyWalkGoal(walkMeterState.dailyStepGoal)
    val walkGoalStridePoints = normalizeDailyStrideGoal(walkMeterState.dailyStrideGoal)
    val canUseWalkMeterSignals = walkPermissionGranted && stepCounterSensor != null
    val todayWalkSteps = if (canUseWalkMeterSignals) {
        (walkMeterStepsToday ?: walkMeterState.lastMeasuredStepsToday).coerceAtLeast(0)
    } else {
        0
    }
    val walkMeterCardStepsToday = if (canUseWalkMeterSignals) todayWalkSteps else null
    val todayWalkKm = estimateDistanceKmFromSteps(todayWalkSteps, "Walking")
    val effectiveCadenceForWalk = if (canUseWalkMeterSignals) {
        liveCadenceStepsPerMinute ?: walkMeterState.lastCadenceSpm.takeIf { it > 0f }
    } else {
        null
    }
    val liveIntensityForWalk = if (canUseWalkMeterSignals) {
        liveDetectedIntensity ?: normalizeIntensityLabel(walkMeterState.lastIntensity)
    } else {
        "Light"
    }
    val computedMoveScore = calculateStridePoints(
        steps = todayWalkSteps,
        cadence = effectiveCadenceForWalk,
        intensity = liveIntensityForWalk
    )
    val todayIdleCalories = todayElapsedMinutes() * idleCaloriesPerMinute
    val todayWalkCalories = estimateWalkometerCalories(
        steps = todayWalkSteps,
        cadence = effectiveCadenceForWalk,
        intensity = liveIntensityForWalk
    )
    val todayTotalCalories = todayIdleCalories + todayWalkCalories
    val todayPersistedScore = if (canUseWalkMeterSignals) {
        walkMeterState.weeklyStrideScores[LocalDate.now().toString()]
            ?.coerceAtLeast(0)
            ?: 0
    } else {
        0
    }
    val todayMoveScore = if (canUseWalkMeterSignals) {
        maxOf(
            todayPersistedScore,
            walkMeterState.lastStrideScore.coerceAtLeast(0),
            computedMoveScore
        )
    } else {
        0
    }
    val persistedWalkScoresByDate = remember(walkMeterState.weeklyStrideScores, canUseWalkMeterSignals) {
        if (canUseWalkMeterSignals) {
            walkMeterState.weeklyStrideScores.toLocalDateScoreMap()
        } else {
            emptyMap()
        }
    }
    val walkStreakDays = remember(history.entries, walkGoalStridePoints, todayMoveScore, persistedWalkScoresByDate) {
        history.entries.walkGoalStreak(
            targetScore = walkGoalStridePoints,
            todayMoveScoreLive = todayMoveScore,
            persistedWalkScoresByDate = persistedWalkScoresByDate
        )
    }
    val walkWeeklyGoalStatus = remember(
        history.entries,
        walkGoalStridePoints,
        todayWalkSteps,
        todayMoveScore,
        persistedWalkScoresByDate
    ) {
        history.entries.walkGoalWeek(
            targetScore = walkGoalStridePoints,
            todayStepsLive = todayWalkSteps,
            todayMoveScoreLive = todayMoveScore,
            persistedWalkScoresByDate = persistedWalkScoresByDate
        )
    }
    val todayDate = LocalDate.now()
    val todayMoveMinutesFromLogs = remember(history.entries, todayDate) {
        history.entries.walkDurationMinutesForDate(referenceDate = todayDate)
    }
    val todayKey = todayDate.toString()
    val todayMoveMinutesFromWalkMeter = remember(
        walkMeterState.dailyMoveMinutes,
        walkMeterState.baselineDate,
        canUseWalkMeterSignals,
        todayKey
    ) {
        if (canUseWalkMeterSignals && walkMeterState.baselineDate == todayKey) {
            walkMeterState.dailyMoveMinutes.coerceAtLeast(0f)
        } else {
            0f
        }
    }
    val todayMoveMinutes = maxOf(todayMoveMinutesFromLogs, todayMoveMinutesFromWalkMeter).coerceAtLeast(0f)
    val weeklyIntensityLineGoal = 150
    val weeklyIntensityLineScore = remember(walkWeeklyGoalStatus) {
        walkWeeklyGoalStatus.sumOf { it.moveScore.coerceAtLeast(0) }
    }
    val appendActivityEntry: (ActivityLogEntry) -> Unit = { entry ->
        val normalizedPoints = entry.stridePoints.toIntOrNull()
            ?: calculateStridePoints(
                steps = entry.steps.toIntOrNull() ?: 0,
                cadence = null,
                intensity = entry.intensity.ifBlank { "Moderate" }
            )
        val finalizedEntry = entry.copy(stridePoints = normalizedPoints.toString())
        val updatedHistory = ActivityHistory((history.entries + finalizedEntry).takeLast(240))
        history = updatedHistory
        saveStatus = "Saved ${formatLoggedAt(finalizedEntry.loggedAt)}"
        coroutineScope.launch {
            saveActivityHistory(context, updatedHistory)
        }
    }
    val walkRunMode = activityType == "Walking" || activityType == "Running"
    val startWalkRunSession: () -> Unit = {
        val currentRawSteps = walkMeterRawSteps ?: walkMeterState.lastMeasuredRawSteps.takeIf { it >= 0f }
        sessionActive = true
        sessionStartedAtMs = currentRawSteps?.let { System.currentTimeMillis() }
        sessionStartRawSteps = currentRawSteps
        sessionStepCount = 0
        sessionDurationMinutes = 0f
        intensity = inferIntensityFromCadenceWithHysteresis(
            stepsPerMinute = liveCadenceStepsPerMinute,
            activityType = activityType,
            previousIntensity = walkMeterState.lastIntensity
        )
        saveStatus = "Session started for $activityType."
    }
    val stopWalkRunSession: () -> Unit = {
        if (sessionActive) {
            val finalDuration = sessionDurationMinutes.takeIf { it > 0f } ?: 1f
            val finalSteps = sessionStepCount.coerceAtLeast(0)
            val detectedIntensity = inferIntensityFromCadenceWithHysteresis(
                stepsPerMinute = liveCadenceStepsPerMinute,
                activityType = activityType,
                previousIntensity = intensity
            )

            val estimatedDistanceKm = estimateDistanceKmFromSteps(finalSteps, activityType)
            val estimatedCalories = estimateCaloriesFromSteps(
                steps = finalSteps,
                activityType = activityType,
                durationMinutes = finalDuration
            )
            manualLogDraft = ActivityLogDraft(
                type = activityType,
                intensity = detectedIntensity,
                durationMinutes = formatMetricValue(finalDuration),
                steps = finalSteps.toString(),
                distanceKm = if (estimatedDistanceKm > 0f) {
                    String.format(Locale.getDefault(), "%.2f", estimatedDistanceKm)
                } else "",
                calories = if (estimatedCalories > 0f) formatMetricValue(estimatedCalories) else "",
                notes = "Walk-o-meter session ($activityType)."
            )
            intensity = detectedIntensity
            saveStatus = "Session captured. Confirm from + log dialog."
            showManualLogDialog = true

            sessionActive = false
            sessionStartedAtMs = null
            sessionStartRawSteps = null
            sessionStepCount = 0
            sessionDurationMinutes = 0f
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    manualLogDraft = ActivityLogDraft(
                        type = activityType,
                        intensity = intensity
                    )
                    showManualLogDialog = true
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add activity log")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.panel, RoundedCornerShape(24.dp))
                    .border(1.dp, palette.border, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Activity Tracking",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Log sessions and monitor weekly movement trends.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        WalkMeterCard(
                            stepsToday = walkMeterCardStepsToday,
                            walkPermissionGranted = walkPermissionGranted,
                            sensorAvailable = stepCounterSensor != null,
                            activityType = activityType,
                            liveCadenceStepsPerMinute = liveCadenceStepsPerMinute,
                            liveDetectedIntensity = liveDetectedIntensity,
                            sessionActive = sessionActive,
                            sessionStepCount = sessionStepCount,
                            sessionDurationMinutes = sessionDurationMinutes,
                            onStartSession = startWalkRunSession,
                            onStopSession = stopWalkRunSession,
                            onEditDailyStepGoal = {
                                goalFieldToEdit = WalkGoalField.DailySteps
                                goalFieldInput = walkGoalSteps.toString()
                            },
                            onEditIntensityGoal = {
                                goalFieldToEdit = WalkGoalField.Intensity
                                goalFieldInput = walkGoalStridePoints.toString()
                            },
                            walkRunMode = walkRunMode,
                            dailyStepGoal = walkGoalSteps,
                            dailyStrideGoal = walkGoalStridePoints,
                            strideScoreToday = todayMoveScore,
                            weeklyIntensityGoal = weeklyIntensityLineGoal,
                            weeklyIntensityScore = weeklyIntensityLineScore,
                            streakDays = walkStreakDays,
                            moveMinutesToday = todayMoveMinutes,
                            walkedKmToday = todayWalkKm,
                            caloriesToday = todayTotalCalories,
                            walkCaloriesToday = todayWalkCalories,
                            idleCaloriesToday = todayIdleCalories,
                            bmiForCalories = latestBmi,
                            idleCaloriesPerMinute = idleCaloriesPerMinute
                        )
                    }
                    item {
                        WalkWeeklyStreakSection(
                            week = walkWeeklyGoalStatus,
                            targetScore = walkGoalStridePoints,
                            streakDays = walkStreakDays
                        )
                    }
                    item {
                        val helperText = if (walkRunMode) {
                            "Walk/run intensity is estimated from recent step speed (steps/min)."
                        } else {
                            "Switch activity to Walking or Running for live step-based intensity."
                        }
                        Text(
                            text = helperText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (latestEntry != null) {
                        item {
                            ActivitySummaryCard(
                                entry = latestEntry,
                                weeklySummary = weeklySummary
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Manual Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap the + button to add manual activities and save intensity score.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (healthHistory.entries.isNotEmpty()) {
                        item {
                            ActivityWeightTrendSection(healthHistory.entries)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                saveStatus?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showManualLogDialog) {
        ActivityLogDialog(
            initial = manualLogDraft,
            onDismiss = { showManualLogDialog = false },
            onSave = { entry ->
                appendActivityEntry(entry)
                activityType = entry.type.ifBlank { activityType }
                intensity = entry.intensity.ifBlank { intensity }
                showManualLogDialog = false
            }
        )
    }

    val editingGoalField = goalFieldToEdit
    if (editingGoalField != null) {
        val isStepsGoal = editingGoalField == WalkGoalField.DailySteps
        val title = if (isStepsGoal) "Edit Daily Step Goal" else "Edit Intensity Goal"
        AlertDialog(
            onDismissRequest = { goalFieldToEdit = null },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = goalFieldInput,
                    onValueChange = { input ->
                        goalFieldInput = input.filter { it.isDigit() }
                    },
                    singleLine = true,
                    label = { Text(if (isStepsGoal) "Steps" else "Intensity Score") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = framedTextFieldColors()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val entered = goalFieldInput.toIntOrNull()
                        if (entered == null) {
                            saveStatus = "Enter a valid number."
                            return@Button
                        }
                        val updatedState = when (editingGoalField) {
                            WalkGoalField.DailySteps -> {
                                walkMeterState.copy(dailyStepGoal = normalizeDailyWalkGoal(entered))
                            }

                            WalkGoalField.Intensity -> {
                                walkMeterState.copy(dailyStrideGoal = normalizeDailyStrideGoal(entered))
                            }
                        }
                        walkMeterState = updatedState
                        goalFieldToEdit = null
                        saveStatus = null
                        coroutineScope.launch {
                            saveWalkMeterState(context, updatedState)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalFieldToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGoalSetupPrompt) {
        AlertDialog(
            onDismissRequest = { showGoalSetupPrompt = false },
            title = { Text("Set Your Daily Goals") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = goalSetupStepInput,
                        onValueChange = { input ->
                            goalSetupStepInput = input.filter { it.isDigit() }
                        },
                        singleLine = true,
                        label = { Text("Daily Step Goal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = framedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = goalSetupIntensityInput,
                        onValueChange = { input ->
                            goalSetupIntensityInput = input.filter { it.isDigit() }
                        },
                        singleLine = true,
                        label = { Text("Daily Intensity Goal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = framedTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stepGoal = goalSetupStepInput.toIntOrNull()
                        val intensityGoal = goalSetupIntensityInput.toIntOrNull()
                        if (stepGoal == null || intensityGoal == null) {
                            saveStatus = "Enter valid goal values."
                            return@Button
                        }
                        val updatedState = walkMeterState.copy(
                            dailyStepGoal = normalizeDailyWalkGoal(stepGoal),
                            dailyStrideGoal = normalizeDailyStrideGoal(intensityGoal)
                        )
                        walkMeterState = updatedState
                        showGoalSetupPrompt = false
                        saveStatus = null
                        coroutineScope.launch {
                            saveWalkMeterState(context, updatedState)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalSetupPrompt = false }) {
                    Text("Later")
                }
            }
        )
    }

}

@Composable
private fun WalkMeterCard(
    stepsToday: Int?,
    walkPermissionGranted: Boolean,
    sensorAvailable: Boolean,
    activityType: String,
    liveCadenceStepsPerMinute: Float?,
    liveDetectedIntensity: String?,
    sessionActive: Boolean,
    sessionStepCount: Int,
    sessionDurationMinutes: Float,
    onStartSession: () -> Unit,
    onStopSession: () -> Unit,
    onEditDailyStepGoal: () -> Unit,
    onEditIntensityGoal: () -> Unit,
    walkRunMode: Boolean,
    dailyStepGoal: Int,
    dailyStrideGoal: Int,
    strideScoreToday: Int,
    weeklyIntensityGoal: Int,
    weeklyIntensityScore: Int,
    streakDays: Int,
    moveMinutesToday: Float,
    walkedKmToday: Float,
    caloriesToday: Float,
    walkCaloriesToday: Float,
    idleCaloriesToday: Float,
    bmiForCalories: Float?,
    idleCaloriesPerMinute: Float
) {
    val palette = activityPalette()
    val hasStepData = stepsToday != null
    val rawStepsValue = stepsToday ?: 0
    val goalSteps = normalizeDailyWalkGoal(dailyStepGoal)
    val staticProgress = rawStepsValue / goalSteps.toFloat()
    val targetProgress = if (hasStepData) staticProgress else 0f
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 650),
        label = "walk_meter_progress"
    )

    val displaySteps = if (hasStepData) rawStepsValue else 0
    val effectiveCadence = liveCadenceStepsPerMinute
    val cadenceValue = effectiveCadence?.let { "${formatMetricValue(it)} spm" } ?: "0 spm"
    val intensityValue = if (hasStepData) {
        liveDetectedIntensity ?: inferIntensityFromCadenceWithHysteresis(
            stepsPerMinute = effectiveCadence,
            activityType = activityType,
            previousIntensity = "Light"
        )
    } else {
        "No Data"
    }
    val pointsGoal = normalizeDailyStrideGoal(dailyStrideGoal)
    val pointsValue = strideScoreToday.coerceAtLeast(0)
    val weeklyLineGoal = weeklyIntensityGoal.coerceAtLeast(1)
    val weeklyLineValue = weeklyIntensityScore.coerceAtLeast(0)
    val weeklyLineProgress = (weeklyLineValue / weeklyLineGoal.toFloat()).coerceIn(0f, 1f)
    val intensityColor = when (intensityValue) {
        "Light" -> Color(0xFF1B5E20)
        "Moderate" -> Color(0xFFFBC02D)
        "Hard" -> Color(0xFF7B1FA2)
        "No Data" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Walk-o-meter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Live step tracking + manual walk/run logging",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            WalkMeterGauge(
                stepsToday = displaySteps,
                goalSteps = goalSteps,
                progress = progress,
                progressColor = intensityColor,
                moveScore = pointsValue,
                moveScoreGoal = pointsGoal,
                moveScoreColor = Color(0xFF1976D2)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                WalkMeterStatTile(
                    label = "Pace",
                    value = cadenceValue,
                    accent = Color(0xFF1976D2),
                    modifier = Modifier.weight(1.06f)
                )
                WalkMeterStatTile(
                    label = "Today",
                    value = "$displaySteps",
                    accent = Color(0xFF00897B),
                    modifier = Modifier.weight(0.88f)
                )
                WalkMeterStatTile(
                    label = "Distance",
                    value = "${formatMetricValue(walkedKmToday)} km",
                    accent = Color(0xFF5D4037),
                    modifier = Modifier.weight(0.96f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                WalkMeterStatTile(
                    label = "Calories",
                    value = "${formatMetricValue(caloriesToday)} kcal",
                    accent = Color(0xFFEF6C00),
                    modifier = Modifier.weight(1.06f)
                )
                WalkMeterStatTile(
                    label = "Streak",
                    value = "$streakDays days",
                    accent = Color(0xFF7CB342),
                    modifier = Modifier.weight(0.88f)
                )
                WalkMeterStatTile(
                    label = "Move Min",
                    value = "${formatMetricValue(moveMinutesToday)} min",
                    accent = Color(0xFF6A1B9A),
                    modifier = Modifier.weight(0.96f)
                )
            }
            WalkMeterPointsLine(
                name = "Weekly Intensity Score",
                points = weeklyLineValue,
                goal = weeklyLineGoal,
                progress = weeklyLineProgress,
                color = Color(0xFF1976D2),
                summary = "Calculated from this week's walk/run intensity totals."
            )
            Text(
                text = "Calories today = Idle ${formatMetricValue(idleCaloriesToday)} + Walk ${formatMetricValue(walkCaloriesToday)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = bmiForCalories?.let {
                    "Idle rate from BMI ${formatMetricValue(it)}: ${formatMetricValue(idleCaloriesPerMinute)} kcal/min"
                } ?: "Idle rate: ${formatMetricValue(idleCaloriesPerMinute)} kcal/min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (sessionActive) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Session active: $sessionStepCount steps | ${formatMetricValue(sessionDurationMinutes)} min",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                color = palette.goalPanel,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WalkMeterStatTile(
                        label = "Daily Step Goal",
                        value = goalSteps.toString(),
                        accent = Color(0xFF6A1B9A),
                        modifier = Modifier.weight(1f),
                        onClick = onEditDailyStepGoal
                    )
                    WalkMeterStatTile(
                        label = "Intensity Goal",
                        value = pointsGoal.toString(),
                        accent = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f),
                        onClick = onEditIntensityGoal
                    )
                }
            }
        }
    }
}

@Composable
private fun WalkMeterGauge(
    stepsToday: Int,
    goalSteps: Int,
    progress: Float,
    progressColor: Color,
    moveScore: Int,
    moveScoreGoal: Int,
    moveScoreColor: Color
) {
    val palette = activityPalette()
    val progressForArc = progress.coerceIn(0f, 1f)
    val progressPercent = (progress * 100f).toInt()
    val bonusSteps = (stepsToday - goalSteps).coerceAtLeast(0)
    val moveScoreProgress = if (moveScoreGoal > 0) {
        (moveScore / moveScoreGoal.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(214.dp)) {
            val arcStart = 140f
            val arcSweep = 260f
            val zoneSweep = arcSweep / 3f
            val zoneGap = 3f
            val trackWidth = 22f
            val progressWidth = 14f
            val innerInset = 30f
            val innerSize = Size(
                width = (size.width - (innerInset * 2f)).coerceAtLeast(1f),
                height = (size.height - (innerInset * 2f)).coerceAtLeast(1f)
            )
            val innerTopLeft = Offset(innerInset, innerInset)
            val innerLineWidth = progressWidth * 0.9f

            drawArc(
                color = palette.gaugeTrackLight,
                startAngle = arcStart,
                sweepAngle = zoneSweep - zoneGap,
                useCenter = false,
                style = Stroke(width = trackWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = palette.gaugeTrackMid,
                startAngle = arcStart + zoneSweep,
                sweepAngle = zoneSweep - zoneGap,
                useCenter = false,
                style = Stroke(width = trackWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = palette.gaugeTrackHigh,
                startAngle = arcStart + (zoneSweep * 2f),
                sweepAngle = zoneSweep - zoneGap,
                useCenter = false,
                style = Stroke(width = trackWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = arcStart,
                sweepAngle = arcSweep * progressForArc,
                useCenter = false,
                style = Stroke(width = progressWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = moveScoreColor.copy(alpha = 0.22f),
                startAngle = arcStart,
                sweepAngle = arcSweep,
                useCenter = false,
                topLeft = innerTopLeft,
                size = innerSize,
                style = Stroke(width = innerLineWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = moveScoreColor,
                startAngle = arcStart,
                sweepAngle = arcSweep * moveScoreProgress,
                useCenter = false,
                topLeft = innerTopLeft,
                size = innerSize,
                style = Stroke(width = innerLineWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stepsToday.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "steps today",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$progressPercent% of $goalSteps goal",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Intensity Score $moveScore/$moveScoreGoal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = moveScoreColor
            )
            if (bonusSteps > 0) {
                Text(
                    text = "+$bonusSteps bonus steps",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun WalkMeterPointsLine(
    name: String,
    points: Int,
    goal: Int,
    progress: Float,
    color: Color,
    summary: String = "Calculated from steps + current pace intensity."
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$points / $goal",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(999.dp))
            )
        }
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (points > goal) {
            Text(
                text = "+${points - goal} bonus intensity score after goal",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun WalkMeterStatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val useAccent = if (isSystemInDarkTheme()) {
        lerp(accent, Color.White, 0.35f)
    } else {
        accent
    }
    val background = if (isSystemInDarkTheme()) {
        useAccent.copy(alpha = 0.18f)
    } else {
        useAccent.copy(alpha = 0.12f)
    }
    val clickableModifier = if (onClick != null) {
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        modifier
    }
    Surface(
        modifier = clickableModifier,
        color = background,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = useAccent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WalkWeeklyStreakSection(
    week: List<WalkGoalDayStatus>,
    targetScore: Int,
    streakDays: Int
) {
    val palette = activityPalette()
    val orderedWeek = if (week.isEmpty()) {
        val today = LocalDate.now()
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            WalkGoalDayStatus(date = date, steps = 0, moveScore = 0, hitGoal = false)
        }
    } else {
        week
    }
    val hitDays = orderedWeek.count { it.hitGoal }
    val progressTarget = if (orderedWeek.isNotEmpty()) hitDays / orderedWeek.size.toFloat() else 0f
    val hitProgress by animateFloatAsState(
        targetValue = progressTarget.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450),
        label = "weekly_streak_progress"
    )
    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEE", Locale.getDefault()) }
    val averageScore = remember(orderedWeek) {
        if (orderedWeek.isNotEmpty()) {
            orderedWeek.sumOf { it.moveScore }.toFloat() / orderedWeek.size
        } else {
            0f
        }
    }
    val longestStreak = remember(orderedWeek) { longestGoalHitStreak(orderedWeek) }

    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Weekly Goal Streak",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Target: $targetScore intensity score/day",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                orderedWeek.forEach { day ->
                    val ratio = if (targetScore > 0) {
                        (day.moveScore / targetScore.toFloat()).coerceIn(0f, 1.2f)
                    } else {
                        0f
                    }
                    val heatAlpha = if (day.moveScore <= 0) 0.08f else 0.2f + (ratio.coerceIn(0f, 1f) * 0.65f)
                    val cardColor = Color(0xFF2E7D32).copy(alpha = heatAlpha)
                    val valueColor = if (ratio >= 0.72f) Color.White else MaterialTheme.colorScheme.onSurface
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = cardColor,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = day.date.format(dayFormatter),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ratio >= 0.72f) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = day.moveScore.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = valueColor
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hitProgress)
                        .height(7.dp)
                        .background(Color(0xFF2E7D32), RoundedCornerShape(999.dp))
                )
            }
            Text(
                text = "$hitDays/7 goal-hit days | Avg ${formatMetricValue(averageScore)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Longest streak: $longestStreak day(s) | Current streak: $streakDays day(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivitySummaryCard(entry: ActivityLogEntry, weeklySummary: ActivityWeeklySummary) {
    val palette = activityPalette()
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Latest Session",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatLoggedAt(entry.loggedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityStatPill(entry.type.ifBlank { "Activity" })
                if (entry.intensity.isNotBlank()) ActivityStatPill(entry.intensity)
                entry.durationMinutes.toPositiveFloatOrNull()?.let {
                    ActivityStatPill("${formatMetricValue(it)} min")
                }
                entry.steps.toPositiveFloatOrNull()?.let {
                    ActivityStatPill("${formatMetricValue(it)} steps")
                }
                entry.distanceKm.toPositiveFloatOrNull()?.let {
                    ActivityStatPill("${formatMetricValue(it)} km")
                }
                entry.calories.toPositiveFloatOrNull()?.let {
                    ActivityStatPill("${formatMetricValue(it)} kcal")
                }
                val points = entry.stridePoints.toFloatOrNull()
                    ?: calculateStridePoints(
                        steps = entry.steps.toIntOrNull() ?: 0,
                        cadence = null,
                        intensity = entry.intensity.ifBlank { "Moderate" }
                    ).toFloat()
                if (points > 0f) {
                    ActivityStatPill("${formatMetricValue(points)} score")
                }
            }
            val weeklyParts = buildList {
                add("${weeklySummary.sessions} sessions")
                if (weeklySummary.durationMinutes > 0f) add("${formatMetricValue(weeklySummary.durationMinutes)} min")
                if (weeklySummary.steps > 0f) add("${formatMetricValue(weeklySummary.steps)} steps")
                if (weeklySummary.distanceKm > 0f) add("${formatMetricValue(weeklySummary.distanceKm)} km")
                if (weeklySummary.calories > 0f) add("${formatMetricValue(weeklySummary.calories)} kcal")
                if (weeklySummary.stridePoints > 0f) add("${formatMetricValue(weeklySummary.stridePoints)} score")
            }
            Text(
                text = "Last 7 days: ${weeklyParts.joinToString(" \u2022 ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (entry.notes.isNotBlank()) {
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ActivityStatPill(text: String) {
    val palette = activityPalette()
    Surface(
        color = palette.pillBg,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = palette.pillText
        )
    }
}

@Composable
private fun ActivityWeightTrendSection(entries: List<HealthLogEntry>) {
    val ordered = remember(entries) { entries.sortedBy { it.loggedAt } }
    val weightPoints = remember(ordered) { ordered.toMetricPoints { it.weightKg } }
    if (weightPoints.isEmpty()) return

    val palette = activityPalette()
    val weightRows = remember(ordered) {
        val values = ordered.mapNotNull { entry ->
            entry.weightKg.toPositiveFloatOrNull()?.let { weight ->
                entry.loggedAt to weight
            }
        }
        values.mapIndexedNotNull { index, (loggedAt, weight) ->
            if (index == 0) {
                Triple(loggedAt, weight, null)
            } else {
                val delta = weight - values[index - 1].second
                if (delta.absoluteValue < 0.0001f) {
                    null
                } else {
                    Triple(loggedAt, weight, delta)
                }
            }
        }.takeLast(14).asReversed()
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Weight Trend",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = "Weight history from Health Tracking.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(6.dp))
    SingleMetricTrendCard(
        title = "Weight Trend",
        unit = "kg",
        points = weightPoints,
        color = Color(0xFF00897B),
        cardColor = palette.card,
        chartColor = palette.chart
    )
    if (weightRows.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = palette.card),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Weight Log",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Includes first set date and later change days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                weightRows.forEachIndexed { index, (loggedAt, weight, delta) ->
                    val dateLabel = Instant.ofEpochMilli(loggedAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
                    val changeLabel = when {
                        delta == null -> "Set"
                        delta > 0f -> "+${formatMetricValue(delta)} kg"
                        else -> "${formatMetricValue(delta)} kg"
                    }
                    val changeColor = when {
                        delta == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        delta < 0f -> Color(0xFF2E7D32)
                        else -> Color(0xFFC62828)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${formatMetricValue(weight)} kg",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = changeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = changeColor,
                            textAlign = TextAlign.End
                        )
                    }
                    if (index < weightRows.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    val palette = activityPalette()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.chart, RoundedCornerShape(12.dp)),
        colors = framedTextFieldColors()
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityLogDialog(
    initial: ActivityLogDraft,
    onDismiss: () -> Unit,
    onSave: (ActivityLogEntry) -> Unit
) {
    var type by remember(initial.type) { mutableStateOf(initial.type.ifBlank { "Walking" }) }
    var intensity by remember(initial.intensity) { mutableStateOf(initial.intensity.ifBlank { "Moderate" }) }
    var durationMinutes by remember(initial.durationMinutes) { mutableStateOf(initial.durationMinutes) }
    var steps by remember(initial.steps) { mutableStateOf(initial.steps) }
    var distanceKm by remember(initial.distanceKm) { mutableStateOf(initial.distanceKm) }
    var calories by remember(initial.calories) { mutableStateOf(initial.calories) }
    var notes by remember(initial.notes) { mutableStateOf(initial.notes) }
    val palette = activityPalette()

    val derivedValues = deriveActivityLogValues(
        activityType = type,
        intensity = intensity,
        durationInput = durationMinutes,
        stepsInput = steps,
        distanceInput = distanceKm,
        caloriesInput = calories
    )
    val canSave = derivedValues.durationMinutes > 0f && derivedValues.steps > 0
    val autoFillPreview = buildList {
        if (durationMinutes.isBlank() && derivedValues.durationMinutes > 0f) {
            add("Duration ${formatMetricValue(derivedValues.durationMinutes)} min")
        }
        if (steps.isBlank() && derivedValues.steps > 0) {
            add("Steps ${derivedValues.steps}")
        }
        if (distanceKm.isBlank() && derivedValues.distanceKm > 0f) {
            add("Distance ${String.format(Locale.getDefault(), "%.2f", derivedValues.distanceKm)} km")
        }
        if (calories.isBlank() && derivedValues.calories > 0f) {
            add("Calories ${formatMetricValue(derivedValues.calories)} kcal")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = {
                    val resolvedDuration = durationMinutes.ifBlank {
                        formatMetricValue(derivedValues.durationMinutes)
                    }
                    val resolvedSteps = steps.ifBlank {
                        derivedValues.steps.takeIf { it > 0 }?.toString().orEmpty()
                    }
                    val resolvedDistanceKm = distanceKm.ifBlank {
                        if (derivedValues.distanceKm > 0f) {
                            String.format(Locale.getDefault(), "%.2f", derivedValues.distanceKm)
                        } else {
                            ""
                        }
                    }
                    val resolvedCalories = calories.ifBlank {
                        if (derivedValues.calories > 0f) {
                            formatMetricValue(derivedValues.calories)
                        } else {
                            ""
                        }
                    }
                    onSave(
                        ActivityLogEntry(
                            loggedAt = System.currentTimeMillis(),
                            type = type.trim(),
                            intensity = intensity.trim(),
                            durationMinutes = resolvedDuration.trim(),
                            steps = resolvedSteps.trim(),
                            distanceKm = resolvedDistanceKm.trim(),
                            calories = resolvedCalories.trim(),
                            stridePoints = derivedValues.stridePoints.toString(),
                            notes = notes.trim()
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add Activity Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Activity Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Walking", "Running", "Cycling", "Gym", "Sports", "Yoga").forEach { option ->
                        StatusChip(option, type == option) { type = option }
                    }
                }
                Text(
                    text = "Intensity",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Light", "Moderate", "Hard").forEach { option ->
                        StatusChip(option, intensity == option) { intensity = option }
                    }
                }
                ActivityField(
                    label = "Duration (minutes)",
                    value = durationMinutes,
                    placeholder = "e.g. 45",
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = { durationMinutes = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ActivityField(
                            label = "Steps",
                            value = steps,
                            placeholder = "e.g. 5200",
                            keyboardType = KeyboardType.Number,
                            onValueChange = { steps = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ActivityField(
                            label = "Distance (km)",
                            value = distanceKm,
                            placeholder = "e.g. 4.2",
                            keyboardType = KeyboardType.Decimal,
                            onValueChange = { distanceKm = it }
                        )
                    }
                }
                ActivityField(
                    label = "Calories (kcal)",
                    value = calories,
                    placeholder = "e.g. 320",
                    keyboardType = KeyboardType.Number,
                    onValueChange = { calories = it }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    placeholder = { Text("Optional details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.chart, RoundedCornerShape(12.dp)),
                    colors = framedTextFieldColors()
                )
                if (autoFillPreview.isNotEmpty()) {
                    Text(
                        text = "Auto-estimate on save: ${autoFillPreview.joinToString(" | ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Intensity Score: ${derivedValues.stridePoints}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!canSave) {
                    Text(
                        text = "Add at least one value (duration, steps, distance, or calories).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Composable
fun HealthTrackingScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var metrics by remember { mutableStateOf(HealthMetrics()) }
    var history by remember { mutableStateOf(HealthHistory()) }
    var saveStatus by remember { mutableStateOf<String?>(null) }
    val palette = healthPalette()

    LaunchedEffect(Unit) {
        val loaded = loadHealthHistory(context)
        history = loaded
        metrics = loaded.entries.lastOrNull()?.toMetrics() ?: HealthMetrics()
    }

    val autoBmi = calculateBmi(metrics.weightKg, metrics.heightCm)
    val validAge = metrics.age.toIntOrNull()?.let { it in 1..120 } == true
    val hasSex = metrics.sex.isNotBlank()
    val canSave = autoBmi != null && validAge && hasSex
    val latestEntry = history.entries.lastOrNull()
    val baselineMetrics = latestEntry?.toMetrics() ?: HealthMetrics()
    val hasChanges = !metrics.hasSameEditableValues(baselineMetrics)

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.panel, RoundedCornerShape(24.dp))
                    .border(1.dp, palette.border, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Health Tracking",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Weight, height, age and sex are required. BMI is calculated automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (latestEntry != null) {
                        item {
                            HealthLatestSummaryCard(entry = latestEntry)
                        }
                    }
                    if (history.entries.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Trend Charts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            HealthTrendSection(history.entries)
                        }
                    }
                    item {
                        Text(
                            text = "Log Metrics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    item {
                        Text(
                            text = "Required for BMI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    item {
                        HealthField(
                            label = "Weight (kg)",
                            value = metrics.weightKg,
                            placeholder = "e.g. 72.5",
                            onValueChange = { metrics = metrics.copy(weightKg = it) }
                        )
                    }
                    item {
                        HealthField(
                            label = "Height (cm)",
                            value = metrics.heightCm,
                            placeholder = "e.g. 175",
                            onValueChange = { metrics = metrics.copy(heightCm = it) }
                        )
                    }
                    item {
                        HealthField(
                            label = "Age",
                            value = metrics.age,
                            placeholder = "e.g. 27",
                            onValueChange = { metrics = metrics.copy(age = it) }
                        )
                    }
                    item {
                        Text(
                            text = "Sex",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Male", "Female", "Other").forEach { option ->
                                StatusChip(option, metrics.sex == option) {
                                    metrics = metrics.copy(sex = option)
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = autoBmi ?: "--",
                            onValueChange = {},
                            label = { Text("BMI (Auto)") },
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(palette.chart, RoundedCornerShape(12.dp)),
                            colors = framedTextFieldColors()
                        )
                        Text(
                            text = "BMI updates automatically from weight and height.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        Text(
                            text = "Optional",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                HealthField(
                                    label = "Blood Pressure (Sys)",
                                    value = metrics.bloodPressureSys,
                                    placeholder = "e.g. 120",
                                    onValueChange = { metrics = metrics.copy(bloodPressureSys = it) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                HealthField(
                                    label = "Blood Pressure (Dia)",
                                    value = metrics.bloodPressureDia,
                                    placeholder = "e.g. 80",
                                    onValueChange = { metrics = metrics.copy(bloodPressureDia = it) }
                                )
                            }
                        }
                    }
                    item {
                        HealthField(
                            label = "Blood Sugar",
                            value = metrics.bloodSugar,
                            placeholder = "e.g. 95",
                            onValueChange = { metrics = metrics.copy(bloodSugar = it) }
                        )
                    }
                    item {
                        HealthField(
                            label = "Body Fat (%)",
                            value = metrics.bodyFat,
                            placeholder = "e.g. 18",
                            onValueChange = { metrics = metrics.copy(bodyFat = it) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                saveStatus?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (hasChanges) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSave,
                        onClick = {
                            val bmiValue = autoBmi ?: return@Button
                            val entry = HealthLogEntry(
                                loggedAt = System.currentTimeMillis(),
                                weightKg = metrics.weightKg.trim(),
                                heightCm = metrics.heightCm.trim(),
                                age = metrics.age.trim(),
                                sex = metrics.sex.trim(),
                                bmi = bmiValue,
                                bloodSugar = metrics.bloodSugar.trim(),
                                bloodPressureSys = metrics.bloodPressureSys.trim(),
                                bloodPressureDia = metrics.bloodPressureDia.trim(),
                                bodyFat = metrics.bodyFat.trim()
                            )
                            val updatedHistory = HealthHistory((history.entries + entry).takeLast(180))
                            history = updatedHistory
                            metrics = entry.toMetrics()
                            saveStatus = "Saved ${formatLoggedAt(entry.loggedAt)}"
                            coroutineScope.launch {
                                saveHealthHistory(context, updatedHistory)
                            }
                        }
                    ) {
                        Text("Save")
                    }
                    if (!canSave) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter valid weight, height, age and sex to calculate BMI and save.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private data class MetricPoint(
    val loggedAt: Long,
    val value: Float
)

private data class BloodPressurePoint(
    val loggedAt: Long,
    val systolic: Float,
    val diastolic: Float
)

@Composable
private fun HealthLatestSummaryCard(entry: HealthLogEntry) {
    val palette = healthPalette()
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Latest Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatLoggedAt(entry.loggedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HealthStatPill("BMI ${entry.bmi}")
                HealthStatPill("Weight ${entry.weightKg} kg")
                if (entry.age.isNotBlank()) HealthStatPill("Age ${entry.age}")
                if (entry.sex.isNotBlank()) HealthStatPill(entry.sex)
                if (entry.bloodPressureSys.isNotBlank() && entry.bloodPressureDia.isNotBlank()) {
                    HealthStatPill("BP ${entry.bloodPressureSys}/${entry.bloodPressureDia}")
                }
                if (entry.bloodSugar.isNotBlank()) HealthStatPill("Sugar ${entry.bloodSugar}")
                if (entry.bodyFat.isNotBlank()) HealthStatPill("Fat ${entry.bodyFat}%")
            }
        }
    }
}

@Composable
private fun HealthStatPill(text: String) {
    val palette = healthPalette()
    Surface(
        color = palette.pillBg,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = palette.pillText
        )
    }
}

@Composable
private fun HealthTrendSection(entries: List<HealthLogEntry>) {
    val ordered = remember(entries) { entries.sortedBy { it.loggedAt } }
    val bmiPoints = remember(ordered) { ordered.toMetricPoints { it.bmi } }
    val weightPoints = remember(ordered) { ordered.toMetricPoints { it.weightKg } }
    val sugarPoints = remember(ordered) { ordered.toMetricPoints { it.bloodSugar } }
    val bodyFatPoints = remember(ordered) { ordered.toMetricPoints { it.bodyFat } }
    val bpPoints = remember(ordered) { ordered.toBloodPressurePoints() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Health Trends",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Graphs update each time you save a new reading.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (weightPoints.isNotEmpty()) {
            SingleMetricTrendCard(
                title = "Weight Trend",
                unit = "kg",
                points = weightPoints,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        if (bmiPoints.isNotEmpty()) {
            SingleMetricTrendCard(
                title = "BMI Trend",
                unit = "",
                points = bmiPoints,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (bpPoints.isNotEmpty()) {
            BloodPressureTrendCard(bpPoints)
        }

        if (sugarPoints.isNotEmpty()) {
            SingleMetricTrendCard(
                title = "Blood Sugar Trend",
                unit = "mg/dL",
                points = sugarPoints,
                color = Color(0xFF7E57C2)
            )
        }

        if (bodyFatPoints.isNotEmpty()) {
            SingleMetricTrendCard(
                title = "Body Fat Trend",
                unit = "%",
                points = bodyFatPoints,
                color = Color(0xFF00897B)
            )
        }
    }
}

@Composable
private fun SingleMetricTrendCard(
    title: String,
    unit: String,
    points: List<MetricPoint>,
    color: Color,
    cardColor: Color? = null,
    chartColor: Color? = null
) {
    if (points.isEmpty()) return
    val palette = healthPalette()
    val resolvedCardColor = cardColor ?: palette.card
    val resolvedChartColor = chartColor ?: palette.chart
    val values = points.map { it.value }
    val latest = values.last()

    Card(
        colors = CardDefaults.cardColors(containerColor = resolvedCardColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (unit.isBlank()) {
                    "Latest: ${formatMetricValue(latest)}"
                } else {
                    "Latest: ${formatMetricValue(latest)} $unit"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TrendSparkline(
                primaryValues = values,
                primaryColor = color,
                containerColor = resolvedChartColor
            )
            if (points.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatChartDate(points.first().loggedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatChartDate(points.last().loggedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BloodPressureTrendCard(points: List<BloodPressurePoint>) {
    if (points.isEmpty()) return
    val palette = healthPalette()
    val latest = points.last()
    val systolic = points.map { it.systolic }
    val diastolic = points.map { it.diastolic }

    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Blood Pressure Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Latest: ${formatMetricValue(latest.systolic)}/${formatMetricValue(latest.diastolic)} mmHg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TrendSparkline(
                primaryValues = systolic,
                primaryColor = Color(0xFFD32F2F),
                secondaryValues = diastolic,
                secondaryColor = Color(0xFFF57C00)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Systolic", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD32F2F))
                Text("Diastolic", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF57C00))
            }
            if (points.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatChartDate(points.first().loggedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatChartDate(points.last().loggedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendSparkline(
    primaryValues: List<Float>,
    primaryColor: Color,
    secondaryValues: List<Float> = emptyList(),
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    containerColor: Color? = null
) {
    if (primaryValues.isEmpty() && secondaryValues.isEmpty()) return
    val palette = healthPalette()
    val resolvedContainerColor = containerColor ?: palette.chart
    val allValues = primaryValues + secondaryValues
    val minValue = allValues.minOrNull() ?: return
    val maxValue = allValues.maxOrNull() ?: return
    val hasVariation = maxValue > minValue
    val range = if (hasVariation) (maxValue - minValue) else 1f

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = resolvedContainerColor)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            val verticalPadding = size.height * 0.16f
            val chartHeight = size.height - (verticalPadding * 2f)

            fun yFor(value: Float): Float {
                if (!hasVariation) return size.height / 2f
                val normalized = ((value - minValue) / range).coerceIn(0f, 1f)
                return verticalPadding + (1f - normalized) * chartHeight
            }

            fun drawSeries(values: List<Float>, color: Color) {
                if (values.isEmpty()) return
                if (values.size == 1) {
                    val y = yFor(values.first())
                    val startX = 0f
                    val endX = size.width
                    drawLine(
                        color = color.copy(alpha = 0.65f),
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 4.5f,
                        cap = StrokeCap.Round
                    )
                    return
                }
                val stepX = size.width / (values.size - 1).toFloat()
                val path = Path()
                values.forEachIndexed { index, value ->
                    val point = Offset(index * stepX, yFor(value))
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                )
                values.forEachIndexed { index, value ->
                    drawCircle(
                        color = color,
                        radius = 2.8f,
                        center = Offset(index * stepX, yFor(value))
                    )
                }
            }

            drawSeries(primaryValues, primaryColor)
            if (secondaryValues.isNotEmpty()) {
                drawSeries(secondaryValues, secondaryColor)
            }
        }
    }
}

private fun List<ActivityLogEntry>.toWeeklySummary(referenceDate: LocalDate = LocalDate.now()): ActivityWeeklySummary {
    val zone = ZoneId.systemDefault()
    val startDate = referenceDate.minusDays(6)
    val weeklyEntries = filter { entry ->
        val loggedDate = Instant.ofEpochMilli(entry.loggedAt).atZone(zone).toLocalDate()
        !loggedDate.isBefore(startDate) && !loggedDate.isAfter(referenceDate)
    }

    fun total(selector: (ActivityLogEntry) -> String): Float {
        return weeklyEntries.sumOf { selector(it).toPositiveFloatOrNull()?.toDouble() ?: 0.0 }.toFloat()
    }

    return ActivityWeeklySummary(
        sessions = weeklyEntries.size,
        durationMinutes = total { it.durationMinutes },
        steps = total { it.steps },
        distanceKm = total { it.distanceKm },
        calories = total { it.calories },
        stridePoints = weeklyEntries.sumOf { entry ->
            val parsed = entry.stridePoints.toFloatOrNull()
            val fallback = calculateStridePoints(
                steps = entry.steps.toIntOrNull() ?: 0,
                cadence = null,
                intensity = entry.intensity.ifBlank { "Moderate" }
            ).toFloat()
            (parsed ?: fallback).toDouble()
        }.toFloat()
    )
}

private fun List<ActivityLogEntry>.walkGoalWeek(
    targetScore: Int,
    todayStepsLive: Int,
    todayMoveScoreLive: Int,
    persistedWalkScoresByDate: Map<LocalDate, Int> = emptyMap(),
    referenceDate: LocalDate = LocalDate.now()
): List<WalkGoalDayStatus> {
    if (targetScore <= 0) return emptyList()
    val stepTotals = walkStepTotalsByDate(todayStepsLive = todayStepsLive, referenceDate = referenceDate)
    val scoreTotals = walkScoreTotalsByDate(
        todayMoveScoreLive = todayMoveScoreLive,
        persistedWalkScoresByDate = persistedWalkScoresByDate,
        referenceDate = referenceDate
    )
    return (6 downTo 0).map { dayOffset ->
        val date = referenceDate.minusDays(dayOffset.toLong())
        val steps = stepTotals[date] ?: 0
        val score = scoreTotals[date] ?: 0
        WalkGoalDayStatus(
            date = date,
            steps = steps,
            moveScore = score,
            hitGoal = score >= targetScore
        )
    }
}

private fun List<ActivityLogEntry>.walkGoalStreak(
    targetScore: Int,
    todayMoveScoreLive: Int,
    persistedWalkScoresByDate: Map<LocalDate, Int> = emptyMap(),
    referenceDate: LocalDate = LocalDate.now()
): Int {
    if (targetScore <= 0) return 0
    val totals = walkScoreTotalsByDate(
        todayMoveScoreLive = todayMoveScoreLive,
        persistedWalkScoresByDate = persistedWalkScoresByDate,
        referenceDate = referenceDate
    )

    // Keep yesterday's streak visible until today's target is reached/missed.
    val todayHitGoal = (totals[referenceDate] ?: 0) >= targetScore
    var streak = if (todayHitGoal) 1 else 0
    var cursor = referenceDate.minusDays(1)
    while ((totals[cursor] ?: 0) >= targetScore) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
}

private fun List<ActivityLogEntry>.walkDurationMinutesForDate(
    referenceDate: LocalDate = LocalDate.now()
): Float {
    val zone = ZoneId.systemDefault()
    return sumOf { entry ->
        if (!entry.type.equals("Walking", ignoreCase = true) &&
            !entry.type.equals("Running", ignoreCase = true)
        ) {
            return@sumOf 0.0
        }
        val date = Instant.ofEpochMilli(entry.loggedAt).atZone(zone).toLocalDate()
        if (date != referenceDate) return@sumOf 0.0
        entry.durationMinutes.toPositiveFloatOrNull()?.toDouble() ?: 0.0
    }.toFloat()
}

private fun List<ActivityLogEntry>.walkStepTotalsByDate(
    todayStepsLive: Int,
    referenceDate: LocalDate = LocalDate.now()
): Map<LocalDate, Int> {
    val zone = ZoneId.systemDefault()
    val totals = mutableMapOf<LocalDate, Int>()

    for (entry in this) {
        if (!entry.type.equals("Walking", ignoreCase = true) &&
            !entry.type.equals("Running", ignoreCase = true)
        ) {
            continue
        }
        val date = Instant.ofEpochMilli(entry.loggedAt).atZone(zone).toLocalDate()
        val steps = entry.steps.toIntOrNull() ?: 0
        totals[date] = (totals[date] ?: 0) + steps.coerceAtLeast(0)
    }
    val todayFromLogs = totals[referenceDate] ?: 0
    totals[referenceDate] = maxOf(todayFromLogs, todayStepsLive.coerceAtLeast(0))
    return totals
}

private fun List<ActivityLogEntry>.walkScoreTotalsByDate(
    todayMoveScoreLive: Int,
    persistedWalkScoresByDate: Map<LocalDate, Int> = emptyMap(),
    referenceDate: LocalDate = LocalDate.now()
): Map<LocalDate, Int> {
    val zone = ZoneId.systemDefault()
    val totals = mutableMapOf<LocalDate, Int>()

    for (entry in this) {
        if (!entry.type.equals("Walking", ignoreCase = true) &&
            !entry.type.equals("Running", ignoreCase = true)
        ) {
            continue
        }
        val date = Instant.ofEpochMilli(entry.loggedAt).atZone(zone).toLocalDate()
        val score = entry.stridePoints.toIntOrNull()
            ?: calculateStridePoints(
                steps = entry.steps.toIntOrNull() ?: 0,
                cadence = null,
                intensity = entry.intensity.ifBlank { "Moderate" }
            )
        totals[date] = (totals[date] ?: 0) + score.coerceAtLeast(0)
    }
    for ((date, score) in persistedWalkScoresByDate) {
        val safeScore = score.coerceAtLeast(0)
        totals[date] = maxOf(totals[date] ?: 0, safeScore)
    }
    val todayFromLogs = totals[referenceDate] ?: 0
    val todayPersisted = persistedWalkScoresByDate[referenceDate]?.coerceAtLeast(0) ?: 0
    totals[referenceDate] = maxOf(todayFromLogs, todayMoveScoreLive.coerceAtLeast(0), todayPersisted)
    return totals
}

private fun longestGoalHitStreak(days: List<WalkGoalDayStatus>): Int {
    var best = 0
    var current = 0
    for (day in days) {
        if (day.hitGoal) {
            current += 1
            if (current > best) best = current
        } else {
            current = 0
        }
    }
    return best
}

private fun String.toPositiveFloatOrNull(): Float? {
    val value = trim().toFloatOrNull() ?: return null
    return value.takeIf { it > 0f }
}

private fun HealthLogEntry.resolvedBmiValue(): Float? {
    val direct = bmi.toPositiveFloatOrNull()
    if (direct != null) return direct
    return calculateBmi(weightKg, heightCm)?.toFloatOrNull()?.takeIf { it > 0f }
}

private fun HealthHistory.hasConfiguredBmi(): Boolean {
    return entries.any { it.resolvedBmiValue() != null }
}

@Suppress("DEPRECATION")
private fun Context.firstInstallTimeMs(): Long {
    return runCatching {
        packageManager.getPackageInfo(packageName, 0).firstInstallTime
    }.getOrDefault(0L)
}

private fun Context.shouldResetWalkMeterStateFromRestore(state: WalkMeterState): Boolean {
    val installTime = firstInstallTimeMs()
    val lastUpdated = state.lastUpdatedAtMs
    if (installTime <= 0L || lastUpdated <= 0L) return false
    // If snapshot predates this app install, it likely came from backup restore.
    return lastUpdated + 60_000L < installTime
}

private fun resetWalkMeterSnapshotForRestore(state: WalkMeterState): WalkMeterState {
    return state.copy(
        baselineDate = "",
        baselineStepCount = -1f,
        dailyStepGoal = normalizeDailyWalkGoal(state.dailyStepGoal),
        dailyStrideGoal = normalizeDailyStrideGoal(state.dailyStrideGoal),
        dailyMoveMinutes = 0f,
        lastMeasuredStepsToday = 0,
        lastMeasuredRawSteps = -1f,
        lastCadenceSpm = 0f,
        lastIntensity = "Light",
        lastStrideScore = 0,
        weeklyStrideScores = emptyMap(),
        lastUpdatedAtMs = 0L
    )
}

private fun resetWalkMeterSnapshotForPermissionGap(
    state: WalkMeterState,
    referenceDate: LocalDate = LocalDate.now()
): WalkMeterState {
    val todayKey = referenceDate.toString()
    val sanitizedScores = normalizeWeeklyStrideScores(state.weeklyStrideScores, referenceDate) - todayKey
    return state.copy(
        baselineDate = "",
        baselineStepCount = -1f,
        dailyMoveMinutes = state.dailyMoveMinutes.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f,
        lastMeasuredStepsToday = 0,
        lastMeasuredRawSteps = -1f,
        lastCadenceSpm = 0f,
        lastIntensity = "Light",
        lastStrideScore = 0,
        weeklyStrideScores = sanitizedScores,
        lastUpdatedAtMs = 0L
    )
}

private fun defaultIdleCaloriesPerMinute(): Float = 1.05f

private fun idleCaloriesPerMinuteFromBmi(bmi: Float?): Float {
    if (bmi == null || bmi <= 0f) return defaultIdleCaloriesPerMinute()
    val assumedHeightMeters = 1.70f
    val estimatedWeightKg = bmi * assumedHeightMeters * assumedHeightMeters
    val restingCaloriesPerMinute = (3.5f * estimatedWeightKg) / 200f
    return restingCaloriesPerMinute.coerceIn(0.7f, 2.2f)
}

private fun todayElapsedMinutes(): Float {
    return (LocalTime.now().toSecondOfDay() / 60f).coerceAtLeast(0f)
}

private fun normalizeWalkMeterStateForToday(
    state: WalkMeterState,
    referenceDate: LocalDate = LocalDate.now()
): WalkMeterState {
    val today = referenceDate.toString()
    var weeklyStrideScores = normalizeWeeklyStrideScores(state.weeklyStrideScores, referenceDate)
    val baselineStepCount = state.baselineStepCount.takeIf { it.isFinite() && it >= 0f } ?: -1f
    val normalized = state.copy(
        baselineStepCount = baselineStepCount,
        dailyStepGoal = normalizeDailyWalkGoal(state.dailyStepGoal),
        dailyStrideGoal = normalizeDailyStrideGoal(state.dailyStrideGoal),
        dailyMoveMinutes = state.dailyMoveMinutes.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f,
        lastMeasuredStepsToday = state.lastMeasuredStepsToday.coerceAtLeast(0),
        lastMeasuredRawSteps = state.lastMeasuredRawSteps.takeIf { it.isFinite() && it >= 0f } ?: -1f,
        lastCadenceSpm = state.lastCadenceSpm.coerceAtLeast(0f),
        lastIntensity = normalizeIntensityLabel(state.lastIntensity),
        lastStrideScore = state.lastStrideScore.coerceAtLeast(0),
        weeklyStrideScores = weeklyStrideScores,
        lastUpdatedAtMs = state.lastUpdatedAtMs.coerceAtLeast(0L)
    )

    val effectiveBaselineDate = normalized.baselineDate.ifBlank {
        normalized.lastUpdatedAtMs.takeIf { it > 0L }?.let { timestamp ->
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        }.orEmpty()
    }
    val scoreDate = runCatching {
        LocalDate.parse(effectiveBaselineDate.ifBlank { today })
    }.getOrElse { referenceDate }
    weeklyStrideScores = upsertDailyStrideScore(
        scores = weeklyStrideScores,
        date = scoreDate,
        score = normalized.lastStrideScore,
        referenceDate = referenceDate
    )

    if (effectiveBaselineDate == today) {
        val synced = normalized.copy(weeklyStrideScores = weeklyStrideScores)
        return if (synced.baselineDate == today) synced else synced.copy(baselineDate = today)
    }

    val rolloverBaseline = normalized.lastMeasuredRawSteps.takeIf { it >= 0f }
        ?: normalized.baselineStepCount.takeIf { it >= 0f }
        ?: -1f

    return normalized.copy(
        baselineDate = today,
        baselineStepCount = rolloverBaseline,
        dailyMoveMinutes = 0f,
        lastMeasuredStepsToday = 0,
        lastCadenceSpm = 0f,
        lastIntensity = "Light",
        lastStrideScore = 0,
        weeklyStrideScores = weeklyStrideScores
    )
}

private fun shouldPersistWalkMeterState(previous: WalkMeterState, current: WalkMeterState): Boolean {
    val baselineChanged = previous.baselineDate != current.baselineDate ||
        previous.baselineStepCount != current.baselineStepCount
    if (baselineChanged) return true

    val goalChanged = previous.dailyStepGoal != current.dailyStepGoal ||
        previous.dailyStrideGoal != current.dailyStrideGoal
    if (goalChanged) return true

    val performanceChanged = previous.lastIntensity != current.lastIntensity ||
        previous.lastStrideScore != current.lastStrideScore ||
        (previous.dailyMoveMinutes - current.dailyMoveMinutes).absoluteValue >= 0.05f
    if (performanceChanged) return true

    if (previous.weeklyStrideScores != current.weeklyStrideScores) return true

    val elapsedMs = (current.lastUpdatedAtMs - previous.lastUpdatedAtMs).coerceAtLeast(0L)
    val stepsDelta = (current.lastMeasuredStepsToday - previous.lastMeasuredStepsToday).absoluteValue
    val rawReset = previous.lastMeasuredRawSteps >= 0f &&
        current.lastMeasuredRawSteps >= 0f &&
        current.lastMeasuredRawSteps < previous.lastMeasuredRawSteps
    return rawReset || stepsDelta >= 5 || elapsedMs >= 15_000L
}

private fun List<StepSample>.toCadenceStepsPerMinute(): Float? {
    if (size < 2) return null
    val first = first()
    val last = last()
    val elapsedMs = (last.timestampMs - first.timestampMs).coerceAtLeast(1L)
    val stepDelta = (last.totalSteps - first.totalSteps).coerceAtLeast(0f)
    if (stepDelta <= 0f) return null
    return stepDelta / (elapsedMs / 60_000f)
}

private const val DEFAULT_MOVE_MINUTES_CADENCE_SPM = 100f

private fun updateDailyMoveMinutes(
    previousMinutes: Float,
    stepDelta: Int,
    cadenceSpm: Float?,
    dayRolledOver: Boolean
): Float {
    val baseMinutes = if (dayRolledOver) {
        0f
    } else {
        previousMinutes.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    }
    if (stepDelta <= 0) return baseMinutes
    val cadence = (cadenceSpm?.takeIf { it.isFinite() && it > 0f } ?: DEFAULT_MOVE_MINUTES_CADENCE_SPM)
        .coerceIn(60f, 180f)
    val gainedMinutes = stepDelta / cadence
    return (baseMinutes + gainedMinutes).coerceAtLeast(0f)
}

private const val WALK_METER_WEEKLY_SCORE_HISTORY_DAYS = 21

private fun Map<String, Int>.toLocalDateScoreMap(): Map<LocalDate, Int> {
    return entries.mapNotNull { (dateKey, score) ->
        runCatching { LocalDate.parse(dateKey) }.getOrNull()?.let { parsed ->
            parsed to score.coerceAtLeast(0)
        }
    }.toMap()
}

private fun normalizeWeeklyStrideScores(
    scores: Map<String, Int>,
    referenceDate: LocalDate = LocalDate.now()
): Map<String, Int> {
    if (scores.isEmpty()) return emptyMap()
    val earliestDate = referenceDate.minusDays((WALK_METER_WEEKLY_SCORE_HISTORY_DAYS - 1).toLong())
    return scores.entries.mapNotNull { (dateKey, score) ->
        val parsedDate = runCatching { LocalDate.parse(dateKey) }.getOrNull() ?: return@mapNotNull null
        if (parsedDate.isBefore(earliestDate)) return@mapNotNull null
        parsedDate.toString() to score.coerceAtLeast(0)
    }.toMap()
}

private fun upsertDailyStrideScore(
    scores: Map<String, Int>,
    date: LocalDate,
    score: Int,
    referenceDate: LocalDate = LocalDate.now()
): Map<String, Int> {
    val normalized = normalizeWeeklyStrideScores(scores, referenceDate)
    val key = date.toString()
    val safeScore = score.coerceAtLeast(0)
    val existing = normalized[key] ?: 0
    return if (safeScore > existing) {
        normalized + (key to safeScore)
    } else {
        normalized
    }
}

private fun normalizeIntensityLabel(value: String?): String {
    return when (value) {
        "Light", "Moderate", "Hard" -> value
        else -> "Light"
    }
}

private fun inferIntensityFromCadenceWithHysteresis(
    stepsPerMinute: Float?,
    activityType: String,
    previousIntensity: String?
): String {
    val previous = normalizeIntensityLabel(previousIntensity)
    val effectiveCadence = sanitizeCadenceSpm(stepsPerMinute) ?: return previous

    val running = activityType == "Running"
    val moderateThreshold = if (running) 145f else 88f
    val hardThreshold = if (running) 178f else 130f
    val hysteresisMargin = 8f

    return when (previous) {
        "Light" -> {
            if (effectiveCadence >= moderateThreshold + hysteresisMargin) "Moderate" else "Light"
        }

        "Moderate" -> when {
            effectiveCadence < moderateThreshold - hysteresisMargin -> "Light"
            effectiveCadence >= hardThreshold + hysteresisMargin -> "Hard"
            else -> "Moderate"
        }

        "Hard" -> {
            if (effectiveCadence < hardThreshold - hysteresisMargin) "Moderate" else "Hard"
        }

        else -> inferIntensityFromCadence(effectiveCadence, activityType)
    }
}

private fun inferIntensityFromCadence(stepsPerMinute: Float?, activityType: String): String {
    val effectiveCadence = sanitizeCadenceSpm(stepsPerMinute)
    if (effectiveCadence == null) {
        return if (activityType == "Running") "Moderate" else "Light"
    }
    return when (activityType) {
        "Running" -> when {
            effectiveCadence < 145f -> "Light"
            effectiveCadence < 178f -> "Moderate"
            else -> "Hard"
        }

        else -> when {
            effectiveCadence < 88f -> "Light"
            effectiveCadence < 130f -> "Moderate"
            else -> "Hard"
        }
    }
}

private fun estimateDistanceKmFromSteps(steps: Int, activityType: String): Float {
    if (steps <= 0) return 0f
    val kmPerStep = distancePerStepKm(activityType)
    return steps * kmPerStep
}

private fun estimateCaloriesFromSteps(steps: Int, activityType: String, durationMinutes: Float): Float {
    if (steps <= 0 || durationMinutes <= 0f) return 0f
    val kcalPerStep = caloriesPerStep(activityType)
    val stepBasedCalories = steps * kcalPerStep
    val minuteFloor = minuteCalorieFloor(activityType)
    val timeBasedCalories = durationMinutes * minuteFloor
    return maxOf(stepBasedCalories, timeBasedCalories)
}

private fun estimateWalkometerCalories(steps: Int, cadence: Float?, intensity: String): Float {
    if (steps <= 0) return 0f
    val estimatedDuration = when {
        cadence != null && cadence > 0f -> steps / cadence
        else -> steps / 92f
    }.coerceAtLeast(1f)
    val multiplier = intensityCalorieMultiplier(intensity)
    val base = estimateCaloriesFromSteps(steps, "Walking", estimatedDuration)
    return base * multiplier
}

private fun deriveActivityLogValues(
    activityType: String,
    intensity: String,
    durationInput: String,
    stepsInput: String,
    distanceInput: String,
    caloriesInput: String
): DerivedActivityLogValues {
    val normalizedType = activityType.ifBlank { "Walking" }
    val normalizedIntensity = intensity.ifBlank { "Moderate" }

    val durationProvided = durationInput.toPositiveFloatOrNull()
    val stepsProvided = stepsInput.trim().toIntOrNull()?.coerceAtLeast(0)
    val distanceProvided = distanceInput.toPositiveFloatOrNull()
    val caloriesProvided = caloriesInput.toPositiveFloatOrNull()

    var resolvedSteps = stepsProvided ?: 0
    if (resolvedSteps <= 0 && distanceProvided != null) {
        resolvedSteps = estimateStepsFromDistanceKm(distanceProvided, normalizedType)
    }

    val fallbackCadence = defaultCadenceForType(normalizedType, normalizedIntensity)
    var resolvedDuration = durationProvided ?: 0f
    if (resolvedDuration <= 0f && resolvedSteps > 0) {
        resolvedDuration = (resolvedSteps / fallbackCadence).coerceAtLeast(1f)
    }

    if (resolvedSteps <= 0 && resolvedDuration > 0f) {
        resolvedSteps = (resolvedDuration * fallbackCadence).toInt().coerceAtLeast(0)
    }

    if (resolvedSteps <= 0 && caloriesProvided != null) {
        resolvedSteps = estimateStepsFromCalories(caloriesProvided, normalizedType, normalizedIntensity)
        if (resolvedDuration <= 0f && resolvedSteps > 0) {
            resolvedDuration = (resolvedSteps / fallbackCadence).coerceAtLeast(1f)
        }
    }

    val resolvedDistance = distanceProvided
        ?: if (resolvedSteps > 0) estimateDistanceKmFromSteps(resolvedSteps, normalizedType) else 0f

    val estimatedCalories = if (resolvedSteps > 0 && resolvedDuration > 0f) {
        estimateCaloriesFromSteps(
            steps = resolvedSteps,
            activityType = normalizedType,
            durationMinutes = resolvedDuration
        ) * intensityCalorieMultiplier(normalizedIntensity)
    } else {
        0f
    }
    val resolvedCalories = caloriesProvided ?: estimatedCalories

    val derivedCadence = if (resolvedSteps > 0 && resolvedDuration > 0f) {
        resolvedSteps / resolvedDuration
    } else {
        null
    }
    val resolvedStridePoints = calculateStridePoints(
        steps = resolvedSteps,
        cadence = derivedCadence,
        intensity = normalizedIntensity
    )

    return DerivedActivityLogValues(
        durationMinutes = resolvedDuration.coerceAtLeast(0f),
        steps = resolvedSteps.coerceAtLeast(0),
        distanceKm = resolvedDistance.coerceAtLeast(0f),
        calories = resolvedCalories.coerceAtLeast(0f),
        stridePoints = resolvedStridePoints
    )
}

private fun distancePerStepKm(activityType: String): Float {
    return activityEstimationProfile(activityType).distanceKmPerStep
}

private fun caloriesPerStep(activityType: String): Float {
    return activityEstimationProfile(activityType).caloriesPerStep
}

private fun minuteCalorieFloor(activityType: String): Float {
    return activityEstimationProfile(activityType).minuteCalorieFloor
}

private fun defaultCadenceForType(activityType: String, intensity: String): Float {
    val profile = activityEstimationProfile(activityType)
    return profile.baseCadenceSpm * cadenceIntensityMultiplier(intensity)
}

private fun estimateStepsFromDistanceKm(distanceKm: Float, activityType: String): Int {
    if (distanceKm <= 0f) return 0
    val steps = distanceKm / distancePerStepKm(activityType)
    return steps.toInt().coerceAtLeast(0)
}

private fun estimateStepsFromCalories(calories: Float, activityType: String, intensity: String): Int {
    if (calories <= 0f) return 0
    val effectiveCaloriesPerStep = caloriesPerStep(activityType) * intensityCalorieMultiplier(intensity)
    if (effectiveCaloriesPerStep <= 0f) return 0
    return (calories / effectiveCaloriesPerStep).toInt().coerceAtLeast(0)
}

private fun intensityCalorieMultiplier(intensity: String): Float {
    return when (intensity) {
        "Light" -> 0.9f
        "Moderate" -> 1.1f
        "Hard" -> 1.35f
        else -> 1f
    }
}

private fun cadenceIntensityMultiplier(intensity: String): Float {
    return when (intensity) {
        "Light" -> 0.82f
        "Moderate" -> 1.0f
        "Hard" -> 1.22f
        else -> 1.0f
    }
}

private fun activityEstimationProfile(activityType: String): ActivityEstimationProfile {
    return when (activityType.trim().lowercase(Locale.getDefault())) {
        "running" -> ActivityEstimationProfile(
            distanceKmPerStep = 0.00102f,
            caloriesPerStep = 0.075f,
            minuteCalorieFloor = 8.5f,
            baseCadenceSpm = 156f
        )

        "cycling" -> ActivityEstimationProfile(
            distanceKmPerStep = 0.00255f,
            caloriesPerStep = 0.068f,
            minuteCalorieFloor = 7.0f,
            baseCadenceSpm = 96f
        )

        "gym" -> ActivityEstimationProfile(
            distanceKmPerStep = 0.00067f,
            caloriesPerStep = 0.058f,
            minuteCalorieFloor = 6.0f,
            baseCadenceSpm = 92f
        )

        "sports" -> ActivityEstimationProfile(
            distanceKmPerStep = 0.00094f,
            caloriesPerStep = 0.071f,
            minuteCalorieFloor = 7.4f,
            baseCadenceSpm = 118f
        )

        "yoga" -> ActivityEstimationProfile(
            distanceKmPerStep = 0.00055f,
            caloriesPerStep = 0.031f,
            minuteCalorieFloor = 3.2f,
            baseCadenceSpm = 66f
        )

        else -> ActivityEstimationProfile(
            distanceKmPerStep = 0.00078f,
            caloriesPerStep = 0.045f,
            minuteCalorieFloor = 3.8f,
            baseCadenceSpm = 102f
        )
    }
}

private fun normalizeDailyWalkGoal(goal: Int): Int {
    return goal.coerceIn(1_000, 100_000)
}

private fun normalizeDailyStrideGoal(goal: Int): Int {
    return goal.coerceIn(10, 1_000)
}

private fun usesDefaultWalkGoals(stepGoal: Int, intensityGoal: Int): Boolean {
    val normalizedSteps = normalizeDailyWalkGoal(stepGoal)
    val normalizedIntensity = normalizeDailyStrideGoal(intensityGoal)
    return normalizedSteps == DEFAULT_DAILY_STEP_GOAL &&
        normalizedIntensity == DEFAULT_DAILY_INTENSITY_GOAL
}

private fun calculateStridePoints(steps: Int, cadence: Float?, intensity: String): Int {
    if (steps <= 0) return 0
    val normalizedIntensity = normalizeIntensityLabel(intensity)
    val effectiveCadence = sanitizeCadenceSpm(cadence)
    val cadenceForDuration = (effectiveCadence ?: when (normalizedIntensity) {
        "Hard" -> 130f
        "Moderate" -> 100f
        else -> 88f
    }).coerceIn(35f, 210f)
    val estimatedActiveMinutes = (steps / cadenceForDuration).coerceAtLeast(0f)

    val pointsPerMinute = if (effectiveCadence != null) {
        when {
            effectiveCadence >= 130f -> 2f
            effectiveCadence >= 100f -> 1f
            else -> 0f
        }
    } else {
        when (normalizedIntensity) {
            "Hard" -> 2f
            "Moderate" -> 1f
            else -> 0f
        }
    }

    return (estimatedActiveMinutes * pointsPerMinute)
        .roundToInt()
        .coerceAtLeast(0)
}

private fun sanitizeCadenceSpm(cadence: Float?): Float? {
    val raw = cadence?.takeIf { it.isFinite() } ?: return null
    if (raw <= 0f) return null
    return raw.coerceIn(35f, 210f)
}

private fun List<HealthLogEntry>.toMetricPoints(selector: (HealthLogEntry) -> String): List<MetricPoint> {
    return mapNotNull { entry ->
        selector(entry).toFloatOrNull()?.let { value ->
            MetricPoint(entry.loggedAt, value)
        }
    }
}

private fun List<HealthLogEntry>.toBloodPressurePoints(): List<BloodPressurePoint> {
    return mapNotNull { entry ->
        val sys = entry.bloodPressureSys.toFloatOrNull()
        val dia = entry.bloodPressureDia.toFloatOrNull()
        if (sys != null && dia != null) {
            BloodPressurePoint(
                loggedAt = entry.loggedAt,
                systolic = sys,
                diastolic = dia
            )
        } else {
            null
        }
    }
}

private fun formatMetricValue(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}

private fun formatChartDate(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("MMM d"))
}

private fun formatLoggedAt(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
}

@Composable
private fun HealthField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    val palette = healthPalette()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.chart, RoundedCornerShape(12.dp)),
        colors = framedTextFieldColors()
    )
}

@Composable
fun TaskDialog(
    task: Task?,
    allTasks: List<Task>,
    onClearCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (TaskFormData) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(task?.title ?: "") }
    var time by remember { mutableStateOf(task?.time ?: nextHourTime()) }
    var date by remember { mutableStateOf(task?.date?.let { LocalDate.parse(it) } ?: LocalDate.now()) }
    var category by remember { mutableStateOf(task?.category ?: "") }
    var color by remember { mutableStateOf(task?.color ?: "#F9C74F") }
    val tags by remember { mutableStateOf(task?.tags?.joinToString(", ") ?: "") }
    var repeat by remember { mutableStateOf(task?.repeat ?: RepeatRule.NONE) }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    val completed by remember { mutableStateOf(task?.completed ?: false) }
    var reminders by remember { mutableStateOf(task?.reminders ?: emptyList()) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var clearCategoryMenuExpanded by remember { mutableStateOf(false) }
    var categoryToRemove by remember { mutableStateOf<String?>(null) }
    var showTimeDialog by remember { mutableStateOf(false) }
    val existingCategories = remember(allTasks) {
        allTasks.mapNotNull { it.category.takeIf { cat -> cat.isNotBlank() } }
            .distinct()
            .sorted()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) {
                    Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                onSave(
                    TaskFormData(
                        title = title,
                        time = time,
                        date = date,
                        category = category,
                        color = color,
                        tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        repeat = repeat,
                        notes = notes,
                        completed = completed,
                        reminders = reminders
                    )
                )
            }) {
                Text(if (task == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(if (task == null) "New Task" else "Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task") },
                    colors = framedTextFieldColors()
                )
                Text(
                    text = "Set Time & Date",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showTimeDialog = true },
                        modifier = Modifier.heightIn(min = 38.dp),
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 5.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = "Time: ${formatTime(time)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    OutlinedButton(
                        onClick = { showDatePicker(context, date) { date = it } },
                        modifier = Modifier.heightIn(min = 38.dp),
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 5.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = "Date: ${date.format(DateTimeFormatter.ofPattern("MMM d"))}",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("Add a new category or choose an existing one") },
                    colors = framedTextFieldColors(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    Box {
                        OutlinedButton(
                            onClick = {
                            if (existingCategories.isEmpty()) {
                                Toast.makeText(context, "No categories yet. Type one above.", Toast.LENGTH_SHORT).show()
                            } else {
                                categoryMenuExpanded = true
                            }
                        },
                            modifier = Modifier.heightIn(min = 40.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Select Category",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            existingCategories.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        category = item
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            if (existingCategories.isEmpty()) {
                                Toast.makeText(context, "No categories yet.", Toast.LENGTH_SHORT).show()
                            } else {
                                clearCategoryMenuExpanded = true
                            }
                        },
                        modifier = Modifier.heightIn(min = 40.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = "Remove Category",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    DropdownMenu(
                        expanded = clearCategoryMenuExpanded,
                        onDismissRequest = { clearCategoryMenuExpanded = false }
                    ) {
                        existingCategories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    categoryToRemove = item
                                    clearCategoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                RepeatPicker(selected = repeat, onSelected = { repeat = it })
                ReminderPicker(selected = reminders, onSelected = { reminders = it })
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    colors = framedTextFieldColors()
                )
            }
        }
    )

    categoryToRemove?.let { item ->
        AlertDialog(
            onDismissRequest = { categoryToRemove = null },
            confirmButton = {
                Button(onClick = {
                    onClearCategory(item)
                    if (category == item) {
                        category = ""
                    }
                    categoryToRemove = null
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToRemove = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Remove category?") },
            text = { Text("This will delete this category are you sure?") }
        )
    }

    if (showTimeDialog) {
        TimeSelectionDialog(
            initial = time,
            onDismiss = { showTimeDialog = false },
            onConfirm = { picked ->
                time = picked
                showTimeDialog = false
            }
        )
    }

}

@Composable
fun RepeatPicker(selected: RepeatRule, onSelected: (RepeatRule) -> Unit) {
    Column {
        Text(text = "Repeat", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip("None", selected == RepeatRule.NONE) { onSelected(RepeatRule.NONE) }
            StatusChip("Daily", selected == RepeatRule.DAILY) { onSelected(RepeatRule.DAILY) }
            StatusChip("Weekly", selected == RepeatRule.WEEKLY) { onSelected(RepeatRule.WEEKLY) }
            StatusChip("Monthly", selected == RepeatRule.MONTHLY) { onSelected(RepeatRule.MONTHLY) }
        }
    }
}

@Composable
fun ReminderPicker(selected: List<Int>, onSelected: (List<Int>) -> Unit) {
    val options = listOf(
        1440 to "1 day",
        720 to "12 hr",
        360 to "6 hr",
        180 to "3 hr",
        120 to "2 hr",
        30 to "30 min"
    )
    Column {
        Text(text = "Countdown Alerts", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(options.take(4)) { (minutes, label) ->
                val active = minutes in selected
                StatusChip(label, active) {
                    onSelected(toggleReminder(selected, minutes))
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(options.drop(4)) { (minutes, label) ->
                val active = minutes in selected
                StatusChip(label, active) {
                    onSelected(toggleReminder(selected, minutes))
                }
            }
        }
    }
}

fun showDatePicker(context: Context, initial: LocalDate, onPicked: (LocalDate) -> Unit) {
    DatePickerDialog(
        context,
        { _, year, month, day ->
            onPicked(LocalDate.of(year, month + 1, day))
        },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth
    ).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionDialog(
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parsedInitial = remember(initial) { parseTimeValue(initial) ?: LocalTime.now().withSecond(0).withNano(0) }
    val selectionColor = MaterialTheme.colorScheme.primary
    val onSelectionColor = MaterialTheme.colorScheme.onPrimary
    val pickerState = rememberTimePickerState(
        initialHour = parsedInitial.hour,
        initialMinute = parsedInitial.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        pickerState.hour,
                        pickerState.minute
                    )
                )
            }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Set Time") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = pickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        clockDialSelectedContentColor = onSelectionColor,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectorColor = selectionColor,
                        containerColor = MaterialTheme.colorScheme.surface,
                        periodSelectorBorderColor = selectionColor.copy(alpha = 0.45f),
                        periodSelectorSelectedContainerColor = selectionColor,
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        periodSelectorSelectedContentColor = onSelectionColor,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        timeSelectorSelectedContainerColor = selectionColor,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                        timeSelectorSelectedContentColor = onSelectionColor,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    )
}

enum class ViewMode { Today, Tomorrow, Week, Date }

enum class StatusFilter { All, Active, Completed }

data class TaskFormData(
    val title: String,
    val time: String,
    val date: LocalDate,
    val category: String,
    val color: String,
    val tags: List<String>,
    val repeat: RepeatRule,
    val notes: String,
    val completed: Boolean,
    val reminders: List<Int>
)

fun getRange(mode: ViewMode, date: LocalDate): Pair<LocalDate, LocalDate> {
    val today = LocalDate.now()
    return when (mode) {
        ViewMode.Today -> today to today
        ViewMode.Tomorrow -> today.plusDays(1) to today.plusDays(1)
        ViewMode.Week -> today to today.plusDays(6)
        ViewMode.Date -> date to date
    }
}

fun isWithinRange(dateString: String, range: Pair<LocalDate, LocalDate>): Boolean {
    val date = LocalDate.parse(dateString)
    return !date.isBefore(range.first) && !date.isAfter(range.second)
}

fun generateDateRange(start: LocalDate, end: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var current = start
    while (!current.isAfter(end)) {
        dates += current
        current = current.plusDays(1)
    }
    return dates
}

fun shouldOccur(rule: RepeatRule, seed: LocalDate, target: LocalDate): Boolean {
    if (target.isBefore(seed)) return false
    return when (rule) {
        RepeatRule.DAILY -> true
        RepeatRule.WEEKDAYS -> target.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY
        RepeatRule.WEEKLY -> seed.dayOfWeek == target.dayOfWeek
        RepeatRule.MONTHLY -> seed.dayOfMonth == target.dayOfMonth
        RepeatRule.NONE -> false
    }
}

fun Task.toInstanceFor(date: LocalDate): Task {
    return copy(
        id = System.currentTimeMillis() + Random.nextInt(1000, 9999),
        template = false,
        templateId = id,
        date = date.toString(),
        completed = false,
        order = date.dayOfYear
    )
}

fun buildTasksFromForm(data: TaskFormData): List<Task> {
    val baseId = System.currentTimeMillis()
    return if (data.repeat == RepeatRule.NONE) {
        listOf(
            Task(
                id = baseId,
                template = false,
                title = data.title,
                time = data.time,
                date = data.date.toString(),
                completed = data.completed,
                category = data.category,
                color = data.color,
                tags = data.tags,
                repeat = data.repeat,
                notes = data.notes,
                reminders = data.reminders,
                order = data.date.dayOfYear
            )
        )
    } else {
        val template = Task(
            id = baseId,
            template = true,
            title = data.title,
            time = data.time,
            date = data.date.toString(),
            completed = false,
            category = data.category,
            color = data.color,
            tags = data.tags,
            repeat = data.repeat,
            notes = data.notes,
            reminders = data.reminders,
            order = data.date.dayOfYear
        )
        val instance = template.toInstanceFor(data.date)
        listOf(template, instance)
    }
}

fun formatTime(timeValue: String): String {
    return runCatching {
        val time = LocalTime.parse(timeValue)
        time.format(DateTimeFormatter.ofPattern("h:mm a"))
    }.getOrElse { timeValue }
}

fun nextHourTime(): String {
    val next = LocalTime.now().plusHours(1).withMinute(0)
    return next.format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun toggleReminder(selected: List<Int>, minutes: Int): List<Int> {
    return if (selected.contains(minutes)) {
        selected.filterNot { it == minutes }
    } else {
        (selected + minutes).sortedDescending()
    }
}

private val completionQuotes = listOf(
    "Small steps add up.",
    "Progress, not perfection.",
    "Done is better than perfect.",
    "One task closer to your goal.",
    "Consistency beats intensity.",
    "Momentum comes from finishing."
)

fun pickCompletionQuote(task: Task): String {
    val index = (task.id % completionQuotes.size).toInt().absoluteValue
    return completionQuotes[index]
}

private val activityJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

private val walkMeterJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

private suspend fun loadActivityHistory(context: Context): ActivityHistory {
    val prefs = context.dataStore.data.first()
    val raw = prefs[ACTIVITY_STATE_KEY] ?: return ActivityHistory()
    return runCatching { activityJson.decodeFromString<ActivityHistory>(raw) }.getOrElse {
        val legacy = runCatching { activityJson.decodeFromString<List<ActivityLogEntry>>(raw) }.getOrNull()
        if (legacy != null) {
            ActivityHistory(entries = legacy)
        } else {
            ActivityHistory()
        }
    }
}

private suspend fun saveActivityHistory(context: Context, history: ActivityHistory) {
    context.dataStore.edit { prefs ->
        prefs[ACTIVITY_STATE_KEY] = activityJson.encodeToString(history)
    }
}

private suspend fun loadWalkMeterState(context: Context): WalkMeterState {
    val prefs = context.dataStore.data.first()
    val raw = prefs[WALK_METER_STATE_KEY] ?: return WalkMeterState()
    return runCatching { walkMeterJson.decodeFromString<WalkMeterState>(raw) }.getOrElse {
        WalkMeterState()
    }
}

private suspend fun saveWalkMeterState(context: Context, state: WalkMeterState) {
    context.dataStore.edit { prefs ->
        prefs[WALK_METER_STATE_KEY] = walkMeterJson.encodeToString(state)
    }
    WalkMeterWidgetUpdater.updateAll(context)
}

private val healthJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

private suspend fun loadHealthHistory(context: Context): HealthHistory {
    val prefs = context.dataStore.data.first()
    val raw = prefs[HEALTH_STATE_KEY] ?: return HealthHistory()
    return runCatching { healthJson.decodeFromString<HealthHistory>(raw) }.getOrElse {
        val legacy = runCatching { healthJson.decodeFromString<HealthMetrics>(raw) }.getOrNull()
        if (legacy != null && legacy.hasMeaningfulData()) {
            val legacyBmi = legacy.bmi.ifBlank { calculateBmi(legacy.weightKg, legacy.heightCm).orEmpty() }
            HealthHistory(entries = listOf(legacy.toLogEntry(legacyBmi)))
        } else {
            HealthHistory()
        }
    }
}

private suspend fun saveHealthHistory(context: Context, history: HealthHistory) {
    context.dataStore.edit { prefs ->
        prefs[HEALTH_STATE_KEY] = healthJson.encodeToString(history)
    }
}

private fun HealthLogEntry.toMetrics(): HealthMetrics {
    return HealthMetrics(
        weightKg = weightKg,
        heightCm = heightCm,
        age = age,
        sex = sex,
        bmi = bmi,
        bloodSugar = bloodSugar,
        bloodPressureSys = bloodPressureSys,
        bloodPressureDia = bloodPressureDia,
        bodyFat = bodyFat
    )
}

private fun HealthMetrics.toLogEntry(bmi: String): HealthLogEntry {
    return HealthLogEntry(
        loggedAt = System.currentTimeMillis(),
        weightKg = weightKg.trim(),
        heightCm = heightCm.trim(),
        age = age.trim(),
        sex = sex.trim(),
        bmi = bmi.trim(),
        bloodSugar = bloodSugar.trim(),
        bloodPressureSys = bloodPressureSys.trim(),
        bloodPressureDia = bloodPressureDia.trim(),
        bodyFat = bodyFat.trim()
    )
}

private fun HealthMetrics.hasMeaningfulData(): Boolean {
    return listOf(weightKg, heightCm, age, sex, bmi, bloodSugar, bloodPressureSys, bloodPressureDia, bodyFat)
        .any { it.isNotBlank() }
}

private fun HealthMetrics.hasSameEditableValues(other: HealthMetrics): Boolean {
    return weightKg.trim() == other.weightKg.trim() &&
        heightCm.trim() == other.heightCm.trim() &&
        age.trim() == other.age.trim() &&
        sex.trim().lowercase(Locale.getDefault()) == other.sex.trim().lowercase(Locale.getDefault()) &&
        bloodSugar.trim() == other.bloodSugar.trim() &&
        bloodPressureSys.trim() == other.bloodPressureSys.trim() &&
        bloodPressureDia.trim() == other.bloodPressureDia.trim() &&
        bodyFat.trim() == other.bodyFat.trim()
}

private fun calculateBmi(weightKg: String, heightCm: String): String? {
    val weight = weightKg.toDoubleOrNull()
    val height = heightCm.toDoubleOrNull()
    if (weight == null || height == null) return null
    if (weight <= 0.0 || height <= 0.0) return null
    val meters = height / 100.0
    val bmi = weight / (meters * meters)
    return String.format(Locale.getDefault(), "%.1f", bmi)
}

private fun completeTask(context: Context, viewModel: TaskViewModel, task: Task) {
    if (task.completed) return
    updateTaskWithCompletion(context, viewModel, task, task.copy(completed = true))
}

private fun deleteTaskWithAlarms(context: Context, viewModel: TaskViewModel, task: Task) {
    TaskAlarmScheduler.cancelTask(context, task.id)
    ReminderScheduler.cancelTask(context, task)
    dismissActiveAlarm(context, task.id)
    viewModel.deleteTask(task.id)
}

private fun deleteSeriesWithAlarms(context: Context, viewModel: TaskViewModel, templateId: Long) {
    val seriesTasks = viewModel.tasks.filter { it.id == templateId || it.templateId == templateId }
    seriesTasks.forEach { task ->
        TaskAlarmScheduler.cancelTask(context, task.id)
        ReminderScheduler.cancelTask(context, task)
        dismissActiveAlarm(context, task.id)
    }
    viewModel.deleteSeries(templateId)
}

private fun updateTaskWithCompletion(
    context: Context,
    viewModel: TaskViewModel,
    previous: Task?,
    updated: Task
) {
    val prior = previous ?: updated
    if (!prior.completed && updated.completed) {
        TaskAlarmScheduler.cancelTask(context, prior.id)
        ReminderScheduler.cancelTask(context, prior)
        dismissActiveAlarm(context, prior.id)
    }
    viewModel.updateTask(updated)
}

private fun dismissActiveAlarm(context: Context, taskId: Long) {
    NotificationManagerCompat.from(context).cancel(AlarmNotificationHelper.notificationId(taskId))
    AlarmRingerService.stopIfMatches(context, taskId)
}

fun parseTimeValue(value: String): LocalTime? {
    return runCatching { LocalTime.parse(value) }
        .getOrElse {
            runCatching {
                LocalTime.parse(value, DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
            }.getOrNull()
        }
}

@Composable
fun framedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary
)

