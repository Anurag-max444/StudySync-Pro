@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val sessions by viewModel.allSessions.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()

    var selectedDayOffset by remember { mutableIntStateOf(0) }

    // Target Day date details
    val targetCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, selectedDayOffset)
    }

    val startOfDay = targetCalendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    val endOfDay = startOfDay + 24 * 60 * 60 * 1000L

    val dayFormat = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
    val formattedTargetDay = dayFormat.format(targetCalendar.time)

    // Filter sessions & completed tasks on specific day
    val targetDaySessions = sessions.filter { it.timestamp in startOfDay until endOfDay }
    val completedTasks = tasks.filter { it.isCompleted } // We display historically completed tasks as list logs

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_screen_root"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("calendar"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("calendar_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Day selector slider bar (Stylized as a Minimalist Pill container)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedDayOffset-- },
                    modifier = Modifier.testTag("cal_prev_day")
                ) {
                    Icon(imageVector = Icons.Default.ArrowLeft, contentDescription = "Previous Day", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                }

                Text(
                    text = if (selectedDayOffset == 0) "Today" else if (selectedDayOffset == -1) "Yesterday" else formattedTargetDay,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("cal_date_label")
                )

                IconButton(
                    onClick = { selectedDayOffset++ },
                    modifier = Modifier.testTag("cal_next_day")
                ) {
                    Icon(imageVector = Icons.Default.ArrowRight, contentDescription = "Next Day", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Overview grid list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1.5f)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Day study sessions:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Text("${targetDaySessions.size} logged sessions", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Card(
                     modifier = Modifier
                        .weight(1.5f)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Task entries completed:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Text("${completedTasks.size} tasks total", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Text("Historical Completed Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (completedTasks.isEmpty()) {
                Text("No finished task entries in history database.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(completedTasks) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = task.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Subject: ${task.subject}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
