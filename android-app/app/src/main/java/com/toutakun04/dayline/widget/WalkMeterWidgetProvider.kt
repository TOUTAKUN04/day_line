package com.toutakun04.dayline.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import com.toutakun04.dayline.MainActivity
import com.toutakun04.dayline.R
import com.toutakun04.dayline.WALK_METER_STATE_KEY
import com.toutakun04.dayline.WalkMeterState
import com.toutakun04.dayline.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class WalkMeterWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            updateWidgets(context, appWidgetManager, appWidgetIds)
            pendingResult.finish()
        }
    }

    companion object {
        private val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        suspend fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val state = loadWalkState(context)
            appWidgetIds.forEach { appWidgetId ->
                val views = buildRemoteViews(context, appWidgetManager, appWidgetId, state)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private suspend fun loadWalkState(context: Context): WalkMeterState {
            val prefs = context.dataStore.data.first()
            val raw = prefs[WALK_METER_STATE_KEY] ?: return WalkMeterState()
            return runCatching { json.decodeFromString<WalkMeterState>(raw) }.getOrElse { WalkMeterState() }
        }

        private fun buildRemoteViews(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            state: WalkMeterState
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_walk_meter)

            val stepGoal = normalizeStepGoal(state.dailyStepGoal)
            val scoreGoal = normalizeScoreGoal(state.dailyStrideGoal)
            val steps = state.lastMeasuredStepsToday.coerceAtLeast(0)
            val cadence = state.lastCadenceSpm.takeIf { it > 0f }
            val intensity = normalizeIntensityLabel(state.lastIntensity, cadence)
            val computedScore = calculateStrideScore(steps, cadence, intensity)
            val score = maxOf(state.lastStrideScore.coerceAtLeast(0), computedScore)
            val stepProgress = (steps / stepGoal.toFloat()).coerceIn(0f, 1f)
            val scoreProgress = (score / scoreGoal.toFloat()).coerceIn(0f, 1f)
            val stepPercent = (stepProgress * 100f).roundToInt().coerceIn(0, 100)
            val darkMode = isDarkMode(context)
            val stepColor = intensityColor(intensity, darkMode)
            val scoreColor = scoreProgressColor(darkMode)

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val ringSizeDp = if (minWidthDp >= 230) 124 else 108
            val ringBitmap = createProgressRing(
                context = context,
                sizeDp = ringSizeDp,
                stepProgress = stepProgress,
                scoreProgress = scoreProgress,
                stepColor = stepColor,
                scoreColor = scoreColor
            )

            views.setImageViewBitmap(R.id.walk_widget_ring, ringBitmap)
            views.setTextViewText(R.id.walk_widget_percent, "$stepPercent%")
            views.setTextViewText(R.id.walk_widget_steps, "$steps / $stepGoal steps")
            views.setTextViewText(R.id.walk_widget_score, "Score $score / $scoreGoal")

            val paceLabel = cadence?.let { "${formatOneDecimal(it)} spm - $intensity" } ?: "No live pace yet"
            views.setTextViewText(R.id.walk_widget_subtitle, paceLabel)

            val updatedLabel = if (state.lastUpdatedAtMs > 0L) {
                val localTime = Instant.ofEpochMilli(state.lastUpdatedAtMs)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
                "Updated $localTime"
            } else {
                "Updated --"
            }
            views.setTextViewText(R.id.walk_widget_updated, updatedLabel)

            val launchIntent = Intent(context, MainActivity::class.java)
            val launchPendingIntent = PendingIntent.getActivity(
                context,
                2,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.walk_widget_root, launchPendingIntent)

            return views
        }

        private fun createProgressRing(
            context: Context,
            sizeDp: Int,
            stepProgress: Float,
            scoreProgress: Float,
            stepColor: Int,
            scoreColor: Int
        ): Bitmap {
            val sizePx = dpToPx(context, sizeDp).coerceAtLeast(64)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val darkMode = isDarkMode(context)

            val center = sizePx / 2f
            val outerStroke = (sizePx * 0.11f).coerceAtLeast(8f)
            val innerStroke = (sizePx * 0.07f).coerceAtLeast(5f)
            val outerRadius = center - (outerStroke / 2f) - 1f
            val innerRadius = outerRadius - outerStroke - 6f

            val outerRect = RectF(
                center - outerRadius,
                center - outerRadius,
                center + outerRadius,
                center + outerRadius
            )
            val innerRect = RectF(
                center - innerRadius,
                center - innerRadius,
                center + innerRadius,
                center + innerRadius
            )

            val outerTrack = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = outerStroke
                strokeCap = Paint.Cap.ROUND
                color = if (darkMode) Color.argb(78, 255, 255, 255) else Color.argb(78, 32, 32, 32)
            }
            val outerProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = outerStroke
                strokeCap = Paint.Cap.ROUND
                color = stepColor
            }
            val innerTrack = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = innerStroke
                strokeCap = Paint.Cap.ROUND
                color = if (darkMode) Color.argb(62, 255, 255, 255) else Color.argb(62, 32, 32, 32)
            }
            val innerProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = innerStroke
                strokeCap = Paint.Cap.ROUND
                color = scoreColor
            }

            canvas.drawArc(outerRect, -90f, 360f, false, outerTrack)
            canvas.drawArc(outerRect, -90f, 360f * stepProgress.coerceIn(0f, 1f), false, outerProgressPaint)
            canvas.drawArc(innerRect, -90f, 360f, false, innerTrack)
            canvas.drawArc(innerRect, -90f, 360f * scoreProgress.coerceIn(0f, 1f), false, innerProgressPaint)

            return bitmap
        }

        private fun dpToPx(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).roundToInt()
        }

        private fun isDarkMode(context: Context): Boolean {
            val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return mode == Configuration.UI_MODE_NIGHT_YES
        }

        private fun normalizeStepGoal(goal: Int): Int {
            return goal.coerceIn(1_000, 100_000)
        }

        private fun normalizeScoreGoal(goal: Int): Int {
            return goal.coerceIn(10, 1_000)
        }

        private fun inferIntensityFromCadence(cadenceSpm: Float?): String {
            if (cadenceSpm == null) return "Light"
            return when {
                cadenceSpm < 75f -> "Light"
                cadenceSpm < 115f -> "Moderate"
                else -> "Hard"
            }
        }

        private fun normalizeIntensityLabel(value: String, cadenceSpm: Float?): String {
            return when (value) {
                "Light", "Moderate", "Hard" -> value
                else -> inferIntensityFromCadence(cadenceSpm)
            }
        }

        private fun intensityColor(intensity: String, darkMode: Boolean): Int {
            return when (intensity) {
                "Light" -> if (darkMode) Color.parseColor("#66BB6A") else Color.parseColor("#2E7D32")
                "Moderate" -> if (darkMode) Color.parseColor("#FFD54F") else Color.parseColor("#F9A825")
                "Hard" -> if (darkMode) Color.parseColor("#CE93D8") else Color.parseColor("#7B1FA2")
                else -> if (darkMode) Color.parseColor("#90A4AE") else Color.parseColor("#607D8B")
            }
        }

        private fun scoreProgressColor(darkMode: Boolean): Int {
            return if (darkMode) Color.parseColor("#64B5F6") else Color.parseColor("#1976D2")
        }

        private fun calculateStrideScore(steps: Int, cadenceSpm: Float?, intensity: String): Int {
            if (steps <= 0) return 0
            val safeCadence = cadenceSpm?.takeIf { it.isFinite() && it > 0f }?.coerceIn(35f, 210f)
            val normalizedIntensity = when (intensity) {
                "Hard" -> "Hard"
                "Moderate" -> "Moderate"
                else -> "Light"
            }
            val cadenceForDuration = (safeCadence ?: when (normalizedIntensity) {
                "Hard" -> 130f
                "Moderate" -> 100f
                else -> 88f
            }).coerceIn(35f, 210f)
            val estimatedMinutes = (steps / cadenceForDuration).coerceAtLeast(0f)
            val pointsPerMinute = if (safeCadence != null) {
                when {
                    safeCadence >= 130f -> 2f
                    safeCadence >= 100f -> 1f
                    else -> 0f
                }
            } else {
                when (normalizedIntensity) {
                    "Hard" -> 2f
                    "Moderate" -> 1f
                    else -> 0f
                }
            }
            return (estimatedMinutes * pointsPerMinute).roundToInt().coerceAtLeast(0)
        }

        private fun formatOneDecimal(value: Float): String {
            return if (value % 1f == 0f) {
                value.toInt().toString()
            } else {
                String.format(Locale.getDefault(), "%.1f", value)
            }
        }

    }
}

object WalkMeterWidgetUpdater {
    fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, WalkMeterWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            WalkMeterWidgetProvider.updateWidgets(context, appWidgetManager, appWidgetIds)
        }
    }
}
