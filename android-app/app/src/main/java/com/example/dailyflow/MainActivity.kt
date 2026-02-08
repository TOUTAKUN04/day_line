package com.example.dailyflow

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.dailyflow.ui.theme.DayLineTheme
import com.example.dailyflow.reminder.ReminderScheduler
import com.example.dailyflow.reminder.TaskAlarmScheduler
import com.example.dailyflow.widget.DailyTasksWidgetUpdater
import com.example.dailyflow.weather.WeatherDisplay
import com.example.dailyflow.weather.WeatherIconType
import com.example.dailyflow.weather.WeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        requestExactAlarmPermissionIfNeeded()
        requestLocationPermissionIfNeeded()
        setContent {
            DayLineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: TaskViewModel = viewModel(
                        factory = TaskViewModelFactory(TaskStore(this))
                    )
                    DayLineApp(viewModel)
                }
            }
        }
    }
}

private fun ComponentActivity.requestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
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

private fun ComponentActivity.requestExactAlarmPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = getSystemService(android.app.AlarmManager::class.java)
        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
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
        tasks = updated
        viewModelScope.launch {
            store.save(updated)
        }
    }

    fun addTask(task: Task) {
        saveTasks(tasks + task)
    }

    fun updateTask(task: Task) {
        saveTasks(tasks.map { if (it.id == task.id) task else it })
    }

    fun deleteTask(taskId: Long) {
        saveTasks(tasks.filterNot { it.id == taskId })
    }

    fun deleteSeries(templateId: Long) {
        saveTasks(tasks.filterNot { it.id == templateId || it.templateId == templateId })
    }

    fun ensureInstancesForRange(start: LocalDate, end: LocalDate) {
        val templates = tasks.filter { it.template && it.repeat != RepeatRule.NONE }
        val dateList = generateDateRange(start, end)
        val newItems = mutableListOf<Task>()

        for (template in templates) {
            val seed = LocalDate.parse(template.date)
            for (date in dateList) {
                if (!shouldOccur(template.repeat, seed, date)) continue
                val exists = tasks.any { !it.template && it.templateId == template.id && it.date == date.toString() }
                if (!exists) {
                    newItems += template.toInstanceFor(date)
                }
            }
        }

        if (newItems.isNotEmpty()) {
            saveTasks(tasks + newItems)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayLineApp(viewModel: TaskViewModel) {
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
                viewModel.saveTasks(updated)
            },
            onDismiss = { showDialog = false },
            onSave = { data ->
                coroutineScope.launch {
                    if (editingTask == null) {
                        val items = buildTasksFromForm(data)
                        items.forEach { viewModel.addTask(it) }
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
                            viewModel.updateTask(updated)
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
                        viewModel.updateTask(target.copy(completed = true))
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
                        viewModel.deleteTask(target.id)
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
                        viewModel.deleteSeries(target.templateId)
                    }
                    showSeriesDelete = false
                    pendingDelete = null
                }) { Text("Entire Series") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val target = pendingDelete
                    if (target != null) {
                        viewModel.deleteTask(target.id)
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
    val viewLabel = when (viewMode) {
        ViewMode.Today -> "Today"
        ViewMode.Tomorrow -> "Tomorrow"
        ViewMode.Week -> "This Week"
        ViewMode.Date -> "Selected: ${viewDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))}"
    }

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
            text = "${weather.location} • ${weather.temperature}",
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
        text = "Showing ${visibleTasks.size} of $total tasks • $completed completed",
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
            onRequestDelete(task)
        }
        false
    })

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd),
        background = {
            val offset = dismissState.offset.value
            if (offset != 0f) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                    val fraction = (offset / maxWidthPx).coerceIn(0f, 1f)
                    val bg = MaterialTheme.colorScheme.error.copy(alpha = fraction)
                    val fg = MaterialTheme.colorScheme.onError
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bg, RoundedCornerShape(16.dp))
                            .padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", color = fg, style = MaterialTheme.typography.labelMedium)
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
                    }.joinToString(" • ")
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
    var tags by remember { mutableStateOf(task?.tags?.joinToString(", ") ?: "") }
    var repeat by remember { mutableStateOf(task?.repeat ?: RepeatRule.NONE) }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    var completed by remember { mutableStateOf(task?.completed ?: false) }
    var reminders by remember { mutableStateOf(task?.reminders ?: emptyList()) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var clearCategoryMenuExpanded by remember { mutableStateOf(false) }
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
                    OutlinedButton(onClick = {
                        showTimePicker(context, time) { time = it }
                    }, colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )) { Text("Time: $time") }
                    OutlinedButton(onClick = {
                        showDatePicker(context, date) { date = it }
                    }, colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )) { Text("Date: ${date.format(DateTimeFormatter.ofPattern("MMM d"))}") }
                }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
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
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Add Category", maxLines = 1, style = MaterialTheme.typography.labelMedium)
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
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Clear Category", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }
                    DropdownMenu(
                        expanded = clearCategoryMenuExpanded,
                        onDismissRequest = { clearCategoryMenuExpanded = false }
                    ) {
                        existingCategories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    onClearCategory(item)
                                    if (category == item) {
                                        category = ""
                                    }
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
        Text(text = "Reminders", style = MaterialTheme.typography.labelMedium)
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

fun showTimePicker(context: Context, initial: String, onPicked: (String) -> Unit) {
    val parts = initial.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: LocalTime.now().hour
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    TimePickerDialog(
        context,
        { _, pickedHour, pickedMinute ->
            onPicked(String.format(Locale.getDefault(), "%02d:%02d", pickedHour, pickedMinute))
        },
        hour,
        minute,
        true
    ).show()
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

fun suggestTimeForDate(date: LocalDate, tasks: List<Task>): String {
    val occupied = tasks
        .filter { !it.template && it.date == date.toString() }
        .mapNotNull { parseTimeValue(it.time) }
        .map { it.withMinute(0).withSecond(0).withNano(0) }
        .toSet()

    for (hour in 8..20) {
        val candidate = LocalTime.of(hour, 0)
        if (candidate !in occupied) {
            return candidate.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    val fallback = if (date == LocalDate.now()) {
        LocalTime.now().plusHours(1).withMinute(0)
    } else {
        LocalTime.of(9, 0)
    }
    return fallback.format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun suggestCategoryFromTitle(title: String): String {
    val normalized = title.trim().lowercase(Locale.getDefault())
    if (normalized.isBlank()) return ""
    val rules = listOf(
        "meeting" to "Meetings",
        "call" to "Meetings",
        "interview" to "Meetings",
        "gym" to "Health",
        "workout" to "Health",
        "doctor" to "Health",
        "dentist" to "Health",
        "study" to "Study",
        "class" to "Study",
        "exam" to "Study",
        "grocer" to "Errands",
        "shopping" to "Errands",
        "delivery" to "Errands",
        "clean" to "Home",
        "laundry" to "Home",
        "repair" to "Home",
        "bill" to "Finance",
        "invoice" to "Finance",
        "pay" to "Finance",
        "code" to "Work",
        "review" to "Work",
        "design" to "Work",
        "email" to "Work",
        "plan" to "Work"
    )
    return rules.firstOrNull { normalized.contains(it.first) }?.second.orEmpty()
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
