package com.example.dailyflow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.dailyflow.MainActivity
import com.example.dailyflow.R
import com.example.dailyflow.Task
import com.example.dailyflow.TASK_STATE_KEY
import com.example.dailyflow.dataStore
import com.example.dailyflow.formatTime
import com.example.dailyflow.parseTimeValue
import com.example.dailyflow.weather.WeatherRepository
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
            val now = LocalDateTime.now()
            val upcoming = tasks
                .filter { !it.template && !it.completed }
                .mapNotNull { task ->
                    val whenTime = task.toDateTime() ?: return@mapNotNull null
                    if (whenTime.isBefore(now)) null else task to whenTime
                }
                .sortedBy { it.second }

            val weatherDisplay = WeatherRepository.loadWeatherDisplay(context)

            for (appWidgetId in appWidgetIds) {
                val views = buildRemoteViews(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId,
                    upcoming = upcoming,
                    temperature = weatherDisplay.temperature
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
            temperature: String
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_daily_tasks)

            val timeLabel = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
            val dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d"))
            val greeting = when (LocalTime.now().hour) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            }
            val tempLabel = if (temperature.contains("°")) temperature else "$temperature°C"

            views.setTextViewText(R.id.widget_greeting, greeting)
            views.setTextViewText(R.id.widget_date_temp, "$dateLabel • $tempLabel")

            val pendingCount = upcoming.size
            views.setTextViewText(R.id.widget_count, "$pendingCount tasks left")

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            val maxTasks = if (minHeight >= 180) 3 else 2

            val lineIds = listOf(R.id.widget_task_1, R.id.widget_task_2, R.id.widget_task_3)
            val topTasks = upcoming.take(maxTasks)

            if (topTasks.isEmpty()) {
                views.setViewVisibility(R.id.widget_empty, View.VISIBLE)
                views.setTextViewText(R.id.widget_empty, "No upcoming tasks")
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

            return views
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
