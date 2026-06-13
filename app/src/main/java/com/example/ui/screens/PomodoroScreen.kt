@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PomodoroScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val secsRemaining by viewModel.pomodoroSecondsRemaining.collectAsState()
    val totalSecs by viewModel.pomodoroTotalSeconds.collectAsState()
    val isRunning by viewModel.isPomodoroRunning.collectAsState()
    val isBreak by viewModel.isPomodoroBreak.collectAsState()
    val completedSessions by viewModel.pomodoroSessionsCompleted.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var workSliderVal by remember { mutableFloatStateOf(25f) }
    var breakSliderVal by remember { mutableFloatStateOf(5f) }

    // Formatter
    val m = secsRemaining / 60
    val s = secsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", m, s)
    val progress = if (totalSecs > 0) secsRemaining.toFloat() / totalSecs.toFloat() else 1f

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pomodoro_root"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("pomodoro"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pomodoro_back")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier.testTag("pomo_config_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Configure Timer")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Mode announcement
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isBreak) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("mode_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isBreak) Icons.Default.Coffee else Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (isBreak) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBreak) "Break Time! Relax and Breathe." else "Focus Mode! Don't get distracted.",
                        fontWeight = FontWeight.Bold,
                        color = if (isBreak) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Circular progress timer indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    color = if (isBreak) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("pomo_timer_display")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Session Counter: $completedSessions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isRunning) {
                    Button(
                        onClick = { viewModel.startPomodoro() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("pomo_start_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start focus")
                    }
                } else {
                    Button(
                        onClick = { viewModel.pausePomodoro() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("pomo_pause_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause focus")
                    }
                }

                Button(
                    onClick = { viewModel.stopPomodoro() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("pomo_reset_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset")
                }
            }

            // Options list card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tomato settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Automatic Break", fontWeight = FontWeight.Bold)
                            Text("Auto-start breaks after completions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = viewModel.autoBreakMode,
                            onCheckedChange = { viewModel.autoBreakMode = it },
                            modifier = Modifier.testTag("auto_break_switch")
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Notifications sound", fontWeight = FontWeight.Bold)
                            Text("Buzz when session is complete", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            modifier = Modifier.testTag("toggle_sound")
                        )
                    }
                }
            }
        }

        // Custom config Dialog
        if (showConfigDialog) {
            AlertDialog(
                onDismissRequest = { showConfigDialog = false },
                title = { Text("Custom durations", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("Focus (Work) duration: ${workSliderVal.toInt()} mins")
                            Slider(
                                value = workSliderVal,
                                onValueChange = { workSliderVal = it },
                                valueRange = 5f..60f,
                                modifier = Modifier.testTag("work_slider")
                            )
                        }

                        Column {
                            Text("Break duration: ${breakSliderVal.toInt()} mins")
                            Slider(
                                value = breakSliderVal,
                                onValueChange = { breakSliderVal = it },
                                valueRange = 1f..30f,
                                modifier = Modifier.testTag("break_slider")
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        modifier = Modifier.testTag("pomo_dialog_confirm"),
                        onClick = {
                            viewModel.configurePomodoroDurations(workSliderVal.toInt(), breakSliderVal.toInt())
                            showConfigDialog = false
                        }
                    ) {
                        Text("Save configuration")
                    }
                }
            )
        }
    }
}
