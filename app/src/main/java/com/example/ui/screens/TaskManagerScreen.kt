@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.MainViewModel

@Composable
fun TaskManagerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    
    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var priorityInput by remember { mutableStateOf("MEDIUM") }
    var subjectInput by remember { mutableStateOf("Quantitative Aptitude") }

    val priorities = listOf("HIGH", "MEDIUM", "LOW")
    val subjects = listOf("Quantitative Aptitude", "General Intelligence & Reasoning", "English Language", "General Awareness", "UPSC General Studies", "School/College Subject")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tasks_root"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("tasks"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tasks_back")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskToEdit = null
                    titleInput = ""
                    descInput = ""
                    priorityInput = "MEDIUM"
                    subjectInput = "Quantitative Aptitude"
                    showDialog = true
                },
                modifier = Modifier.testTag("fab_add_task"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AssignmentLate,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No pending tasks. Press the + button to add task priorities now!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_item_${task.id}")
                                .clickable {
                                    taskToEdit = task
                                    titleInput = task.title
                                    descInput = task.description
                                    priorityInput = task.priority
                                    subjectInput = task.subject
                                    showDialog = true
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                    modifier = Modifier.testTag("task_complete_check_${task.id}")
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Priority pill
                                        Surface(
                                            color = when (task.priority) {
                                                "HIGH" -> MaterialTheme.colorScheme.errorContainer
                                                "MEDIUM" -> MaterialTheme.colorScheme.primaryContainer
                                                else -> MaterialTheme.colorScheme.secondaryContainer
                                            },
                                            shape = CircleShape,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = task.priority,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (task.description.isNotEmpty()) {
                                        Text(
                                            text = task.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.Subject, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Text(text = task.subject, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { viewModel.deleteTask(task) },
                                        modifier = Modifier.testTag("delete_task_${task.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (taskToEdit == null) "Create Study Task" else "Edit Task", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Task Title *") },
                            modifier = Modifier.fillMaxWidth().testTag("add_task_title"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text("Task Description") },
                            modifier = Modifier.fillMaxWidth().testTag("add_task_desc")
                        )

                        // Priority pickers
                        Column {
                            Text("Priority Level", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                priorities.forEach { prio ->
                                    FilterChip(
                                        selected = priorityInput == prio,
                                        onClick = { priorityInput = prio },
                                        label = { Text(prio) },
                                        modifier = Modifier.testTag("priority_chip_$prio")
                                    )
                                }
                            }
                        }

                        // Subject Selection
                        Column {
                            Text("Subject Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                var tempExpanded by remember { mutableStateOf(false) }
                                Box {
                                    Button(
                                        onClick = { tempExpanded = true },
                                        modifier = Modifier.testTag("subject_select_pills")
                                    ) {
                                        Text(subjectInput)
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = tempExpanded,
                                        onDismissRequest = { tempExpanded = false }
                                    ) {
                                        subjects.forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text(s) },
                                                onClick = {
                                                    subjectInput = s
                                                    tempExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (titleInput.isNotEmpty()) {
                                if (taskToEdit == null) {
                                    viewModel.addTask(
                                        title = titleInput,
                                        description = descInput,
                                        priority = priorityInput,
                                        dueDate = System.currentTimeMillis() + 86400000L,
                                        subject = subjectInput
                                    )
                                } else {
                                    viewModel.updateTask(
                                        taskToEdit!!.copy(
                                            title = titleInput,
                                            description = descInput,
                                            priority = priorityInput,
                                            subject = subjectInput
                                        )
                                    )
                                }
                                showDialog = false
                            }
                        },
                        modifier = Modifier.testTag("add_task_submit")
                    ) {
                        Text(if (taskToEdit == null) "Create" else "Save Changes")
                    }
                }
            )
        }
    }
}
