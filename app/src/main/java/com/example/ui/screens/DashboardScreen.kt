@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val user by viewModel.userFlow.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val goals by viewModel.allGoals.collectAsState()

    // Calculate daily sessions
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    val studySecsToday = sessions
        .filter { it.timestamp >= todayStart }
        .sumOf { it.durationSeconds }
    val studyHrsTodayStr = String.format(Locale.getDefault(), "%.1f", studySecsToday.toDouble() / 3600.0)

    val pendingTasks = tasks.filter { !it.isCompleted }
    val dueTodayTasks = pendingTasks.take(3)

    // Layout
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_root"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "StudySync ",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pro",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Good Day, ${user?.name ?: "Aspirant"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.testTag("settings_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    val initials = remember(user?.name) {
                        val name = user?.name ?: "Aspirant"
                        val parts = name.split(" ").filter { it.isNotBlank() }
                        if (parts.size >= 2) {
                            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                        } else {
                            name.take(2).uppercase()
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .border(width = 2.dp, color = Color.White, shape = CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Focus Progress Summary Card (Styling from mockup HTML)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Spun custom circular load bar indicator container
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(width = 2.dp, color = Color.White, shape = CircleShape)
                                )
                            }
                            Column {
                                Text(
                                    text = "TODAY'S FOCUS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$studyHrsTodayStr hrs / 8h",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val pct = remember(studySecsToday) {
                                ((studySecsToday.toDouble() / 3600.0) / 8.0 * 100.0).coerceAtMost(100.0).toInt()
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$pct%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user?.targetExams ?: "SSC CGL Prep",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily hrs
                    StatCard(
                        title = viewModel.translate("study_hours_today"),
                        value = "$studyHrsTodayStr hrs",
                        icon = Icons.Default.Timer,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    // Streak
                    StatCard(
                        title = viewModel.translate("streak"),
                        value = "${user?.streakCount ?: 0} ${viewModel.translate("days")}",
                        icon = Icons.Default.LocalFireDepartment,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Bar
            item {
                Text(
                    text = viewModel.translate("quick_actions"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionIcon(
                        icon = Icons.Default.PlayArrow,
                        label = "Tracker",
                        testTag = "btn_tracker",
                        onClick = { onNavigate("session_tracker") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.HourglassEmpty,
                        label = "Pomo",
                        testTag = "btn_pomo",
                        onClick = { onNavigate("pomodoro") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.CheckCircle,
                        label = "Tasks",
                        testTag = "btn_tasks",
                        onClick = { onNavigate("task_manager") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Edit,
                        label = "Notes",
                        testTag = "btn_notes",
                        onClick = { onNavigate("notes_manager") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionIcon(
                        icon = Icons.Default.TrendingUp,
                        label = "Goals",
                        testTag = "btn_goals",
                        onClick = { onNavigate("goal_tracker") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Assignment,
                        label = "Mock Test",
                        testTag = "btn_mock",
                        onClick = { onNavigate("mock_test_tracker") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.QueryStats,
                        label = "Analytics",
                        testTag = "btn_analytics",
                        onClick = { onNavigate("analytics") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.CalendarToday,
                        label = "Calendar",
                        testTag = "btn_calendar",
                        onClick = { onNavigate("calendar") }
                    )
                }
            }

            // Weekly Study Progress Canvas
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .shadow(1.dp, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Weekly Study Progress",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        WeeklyProgressChart(sessions)
                    }
                }
            }

            // Tasks Due Today
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tasks Due Today (${pendingTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigate("task_manager") }) {
                        Text("See All")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (dueTodayTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending tasks today. Great job!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(dueTodayTasks) { task ->
                TaskSmallItem(task = task, onCheckedChange = { viewModel.toggleTaskCompletion(task) })
            }

            // Inspirational Quote with active action button mapped from mockup HTML
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF6750A4), Color(0xFF4F378B))
                                )
                            )
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.translate("quote_title").uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"${viewModel.translate("quote")}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 22.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Mockup active action button
                        Button(
                            onClick = { onNavigate("session_tracker") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_start_focus_session"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "Start Focus Session",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Padding at the bottom
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val isStreak = title.lowercase().contains("streak")
    val valueColor = if (isStreak) Color(0xFFF97316) else MaterialTheme.colorScheme.primary
    val iconColor = if (isStreak) Color(0xFFF97316) else MaterialTheme.colorScheme.primary

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(1.dp, RoundedCornerShape(24.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
        }
    }
}

@Composable
fun QuickActionIcon(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .shadow(2.dp, CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun WeeklyProgressChart(sessions: List<com.example.data.model.StudySession>) {
    val calendar = Calendar.getInstance()
    val dailyHours = FloatArray(7)

    // Calculate hours for last 7 days
    for (i in 0..6) {
        val testCal = Calendar.getInstance()
        testCal.add(Calendar.DAY_OF_YEAR, -i)
        val startOfToday = testCal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val endOfToday = startOfToday + 86400000L

        val durationToday = sessions
            .filter { it.timestamp in startOfToday until endOfToday }
            .sumOf { it.durationSeconds }
        dailyHours[6 - i] = (durationToday.toDouble() / 3600.0).toFloat()
    }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxHrs = (dailyHours.maxOrNull() ?: 1.0f).coerceAtLeast(4.0f)
    val colorScheme = MaterialTheme.colorScheme

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(top = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / 7f

        // Draw horizontal guide lines
        for (grid in 1..3) {
            val y = height - (height * (grid / 3.0f))
            drawLine(
                color = colorScheme.outlineVariant.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Draw bars
        for (i in 0..6) {
            val barHeight = (dailyHours[i] / maxHrs) * (height - 30f)
            val left = (i * spacing) + (spacing * 0.15f)
            val right = (i * spacing) + (spacing * 0.85f)
            val bottom = height - 20f
            val top = bottom - barHeight

            // Draw Bar Background
            drawRoundRect(
                color = colorScheme.primaryContainer.copy(alpha = 0.2f),
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(right - left, bottom),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )

            // Draw Bar Foreground Study Progress
            if (barHeight > 0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(colorScheme.primary, colorScheme.secondary)
                    ),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(right - left, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TaskSmallItem(task: Task, onCheckedChange: (Boolean) -> Unit) {
    val indicatorColor = when (task.priority.uppercase()) {
        "HIGH" -> Color(0xFFEF4444)
        "MEDIUM" -> Color(0xFF3B82F6)
        else -> Color(0xFF10B981)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left priority boundary strip from clean mockup HTML
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(indicatorColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.testTag("task_check_${task.id}")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${task.subject} • Priority: ${task.priority.lowercase().replaceFirstChar { it.uppercase() }}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
