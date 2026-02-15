package com.toutakun04.dayline.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.toutakun04.dayline.MainActivity
import com.toutakun04.dayline.R
import com.toutakun04.dayline.Task
import com.toutakun04.dayline.TASK_STATE_KEY
import com.toutakun04.dayline.dataStore
import com.toutakun04.dayline.formatTime
import com.toutakun04.dayline.parseTimeValue
import com.toutakun04.dayline.weather.WeatherIconType
import com.toutakun04.dayline.weather.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DailyTasksWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            updateWidgets(context, appWidgetManager, appWidgetIds, null)
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
            appWidgetIds: IntArray,
            tasksOverride: List<Task>?
        ) {
            val tasks = tasksOverride ?: loadTasks(context)
            val today = LocalDate.now()
            val upcoming = tasks
                .filter { !it.template && !it.completed }
                .mapNotNull { task ->
                    val whenTime = task.toDateTime() ?: return@mapNotNull null
                    if (whenTime.toLocalDate() != today) null else task to whenTime
                }
                .sortedBy { it.second }

            val weatherDisplay = WeatherRepository.loadWeatherDisplay(context)

            for (appWidgetId in appWidgetIds) {
                val views = buildRemoteViews(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId,
                    upcoming = upcoming,
                    temperature = weatherDisplay.temperature,
                    weatherIcon = weatherDisplay.icon
                )
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private suspend fun loadTasks(context: Context): List<Task> {
            val prefs = context.dataStore.data.first()
            val raw = prefs[TASK_STATE_KEY] ?: return emptyList()
            return runCatching { json.decodeFromString<List<Task>>(raw) }.getOrElse { emptyList() }
        }

        private fun buildRemoteViews(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            upcoming: List<Pair<Task, LocalDateTime>>,
            temperature: String,
            weatherIcon: WeatherIconType
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_daily_tasks)

            val timeLabel = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
            val dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d"))
            val greeting = when (LocalTime.now().hour) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            }
            val tempLabel = formatWidgetTemperature(temperature)
            val weatherSymbol = widgetWeatherSymbol(weatherIcon)
            val weatherPart = if (weatherSymbol.isBlank()) "" else " $weatherSymbol"

            views.setTextViewText(R.id.widget_greeting, greeting)
            views.setTextViewText(R.id.widget_date_temp, "$dateLabel$weatherPart - $tempLabel")

            val pendingCount = upcoming.size
            views.setTextViewText(R.id.widget_count, "$pendingCount tasks left")

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            val maxTasks = if (minHeight >= 180) 3 else 2

            val lineIds = listOf(R.id.widget_task_1, R.id.widget_task_2, R.id.widget_task_3)
            val topTasks = upcoming.take(maxTasks)

            if (topTasks.isEmpty()) {
                views.setViewVisibility(R.id.widget_empty, View.VISIBLE)
                views.setTextViewText(R.id.widget_empty, "No tasks today")
            } else {
                views.setViewVisibility(R.id.widget_empty, View.GONE)
            }

            for (index in lineIds.indices) {
                val viewId = lineIds[index]
                val pair = topTasks.getOrNull(index)
                if (pair == null) {
                    views.setViewVisibility(viewId, View.GONE)
                } else {
                    val (task, whenTime) = pair
                    val dayLabel = whenTime.toLocalDate().format(DateTimeFormatter.ofPattern("EEE"))
                    val line = "$dayLabel ${formatTime(task.time)} - ${task.title}"
                    views.setTextViewText(viewId, line)
                    views.setViewVisibility(viewId, View.VISIBLE)
                }
            }

            if (pendingCount > maxTasks) {
                views.setViewVisibility(R.id.widget_more, View.VISIBLE)
                views.setTextViewText(R.id.widget_more, "and ${pendingCount - maxTasks} more")
            } else {
                views.setViewVisibility(R.id.widget_more, View.GONE)
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            val quickAddIntent = Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_QUICK_ADD
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val quickAddPendingIntent = PendingIntent.getActivity(
                context,
                1,
                quickAddIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add, quickAddPendingIntent)

            return views
        }

        private fun formatWidgetTemperature(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty() || trimmed == "--") return "--"

            val numeric = Regex("-?\\d+(?:\\.\\d+)?").find(trimmed)?.value
            return if (numeric != null) {
                "${numeric}\u00B0C"
            } else {
                trimmed
            }
        }

        private fun widgetWeatherSymbol(icon: WeatherIconType): String {
            return when (icon) {
                WeatherIconType.Sun -> "\u2600"
                WeatherIconType.Moon -> "\u263D"
                WeatherIconType.Sunrise -> "\u2197"
                WeatherIconType.Sunset -> "\u2198"
                WeatherIconType.Cloud -> "\u2601"
                WeatherIconType.Rain -> "\u2602"
                WeatherIconType.Storm -> "\u26A1"
                WeatherIconType.Snow -> "\u2744"
                WeatherIconType.LocationOff -> "\u2316"
            }
        }

        private fun Task.toDateTime(): LocalDateTime? {
            return runCatching {
                val dateValue = LocalDate.parse(date)
                val timeValue = parseTimeValue(time) ?: LocalTime.of(9, 0)
                LocalDateTime.of(dateValue, timeValue)
            }.getOrNull()
        }
    }
}

object DailyTasksWidgetUpdater {
    fun updateAll(context: Context, tasksOverride: List<Task>? = null) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, DailyTasksWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            DailyTasksWidgetProvider.updateWidgets(context, appWidgetManager, appWidgetIds, tasksOverride)
        }
    }
}
