@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Goal
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GoalTrackerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val goals by viewModel.allGoals.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var goalTitle by remember { mutableStateOf("") }
    var targetHrsInput by remember { mutableStateOf("10.0") }
    var goalTypeInput by remember { mutableStateOf("DAILY") }

    val goalTypes = listOf("DAILY", "WEEKLY", "MONTHLY")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("goals_root_view"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("goal_tracker"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("goals_back")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    goalTitle = ""
                    targetHrsInput = "10.0"
                    goalTypeInput = "DAILY"
                    showDialog = true
                },
                modifier = Modifier.testTag("fab_add_goal"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No study goals created! Set up daily/weekly study targets to stay completely on track.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goals) { goal ->
                        val percent = if (goal.targetHours > 0) (goal.progressHours / goal.targetHours).coerceIn(0.0, 1.0) else 0.0
                        val displayPercentage = (percent * 100).toInt()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_item_${goal.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = goal.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(
                                            text = "Category: ${goal.type}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteGoal(goal.id) },
                                        modifier = Modifier.testTag("delete_goal_${goal.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete goal",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                // Linear Progress bar
                                LinearProgressIndicator(
                                    progress = { percent.toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", goal.progressHours)} / ${String.format("%.1f", goal.targetHours)} study hrs",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$displayPercentage% completed",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (goal.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Create Study Goal", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = goalTitle,
                            onValueChange = { goalTitle = it },
                            label = { Text("Goal Title (e.g. UPSC Prelims Prep)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_goal_title")
                        )

                        OutlinedTextField(
                            value = targetHrsInput,
                            onValueChange = { targetHrsInput = it },
                            label = { Text("Target Hours *") },
                            modifier = Modifier.fillMaxWidth().testTag("add_goal_target")
                        )

                        // Goal Type Picker
                        Column {
                            Text("Goal Span Duration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                goalTypes.forEach { type ->
                                    FilterChip(
                                        selected = goalTypeInput == type,
                                        onClick = { goalTypeInput = type },
                                        label = { Text(type) },
                                        modifier = Modifier.testTag("goal_type_$type")
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        modifier = Modifier.testTag("add_goal_submit"),
                        onClick = {
                            if (goalTitle.isNotEmpty()) {
                                val hrs = targetHrsInput.toDoubleOrNull() ?: 10.0
                                viewModel.addGoal(
                                    title = goalTitle,
                                    targetHours = hrs,
                                    type = goalTypeInput,
                                    dueDate = System.currentTimeMillis() + 604800000L
                                )
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Create Goal")
                    }
                }
            )
        }
    }
}
