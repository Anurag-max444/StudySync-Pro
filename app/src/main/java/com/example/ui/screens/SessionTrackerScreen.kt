package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SessionTrackerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val isTracking by viewModel.isTrackingSession.collectAsState()
    val isPaused by viewModel.isSessionPaused.collectAsState()
    val elapsedSeconds by viewModel.sessionElapsedSeconds.collectAsState()
    val selectedSubject by viewModel.selectedSessionSubject.collectAsState()

    val subjects = listOf(
        "Quantitative Aptitude",
        "General Intelligence & Reasoning",
        "English Language",
        "General Awareness",
        "UPSC General Studies",
        "Core Academic Subject"
    )

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }

    // Pulse animation for tracking status
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Formatter for elapsed time
    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tracker_root"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("tracker"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tracker_back")) {
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dropdown selection if NOT tracking
            if (!isTracking) {
                Text(
                    text = "Select Study Subject",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = !subjectMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        readOnly = true,
                        value = selectedSubject,
                        onValueChange = {},
                        label = { Text(viewModel.translate("subject")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("dropdown_subject")
                    )
                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjects.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    viewModel.startTrackingSession(selectionOption)
                                    subjectMenuExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.startTrackingSession(selectedSubject) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_start_tracker"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Text(viewModel.translate("start_session"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Timer Interface
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(if (!isPaused) pulseScale else 1.0f)
                        .shadow(4.dp, CircleShape)
                        .background(
                            if (isPaused) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PauseCircle else Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = timeFormatted,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedSubject,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Post-it Note area inside active tracking
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Session Notes / Raw Scribbles") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("tracker_note_input"),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("What did you read? formulas, theorems or pages completed...") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isPaused) {
                        Button(
                            onClick = { viewModel.pauseTrackingSession() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("btn_pause_tracker"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.translate("pause"))
                        }
                    } else {
                        Button(
                            onClick = { viewModel.resumeTrackingSession() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("btn_resume_tracker"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.translate("resume"))
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.stopTrackingSession(notesText)
                            notesText = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_stop_tracker"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save")
                    }
                }
            }

            // History Header
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Study History logs",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            // Dynamic session entries
            val allHistory by viewModel.allSessions.collectAsState()
            if (allHistory.isEmpty()) {
                Text(
                    text = "No study history yet. Take your first study sprint!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                allHistory.take(8).forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = session.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (session.notes.isNotEmpty()) {
                                    Text(
                                        text = session.notes,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${session.durationSeconds / 60} min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
