@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockTest
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MockTestTrackerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val tests by viewModel.allMockTests.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var examNameInput by remember { mutableStateOf("") }
    var scoreInput by remember { mutableStateOf("") }
    var totalMarksInput by remember { mutableStateOf("100") }
    var subjectInput by remember { mutableStateOf("Quantitative Aptitude") }

    val subjects = listOf("Quantitative Aptitude", "General Intelligence & Reasoning", "English Language", "General Awareness", "UPSC General Studies", "School/College Subject")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mock_tests_root"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("mock_tests"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("mocks_back")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    examNameInput = ""
                    scoreInput = ""
                    totalMarksInput = "100"
                    subjectInput = "Quantitative Aptitude"
                    showDialog = true
                },
                modifier = Modifier.testTag("fab_add_mock"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Mock Test Result")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Analytics line graph of improvement
            if (tests.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "My Test Score Trend",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LineImprovementChart(tests)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Historical test score card list",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (tests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No mock tests logged yet. Log your prep answers to evaluate trends!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(tests) { test ->
                    val percentage = if (test.totalMarks > 0) (test.score / test.totalMarks) * 100 else 0.0
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mock_item_${test.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = test.examName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "Subject: ${test.subject}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Marks: ${test.score} / ${test.totalMarks}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = if (percentage >= 60.0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", percentage)}%",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteMockTest(test.id) },
                                    modifier = Modifier.testTag("delete_mock_${test.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete mock", tint = MaterialTheme.colorScheme.error)
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
                title = { Text("Log Mock Test Result", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = examNameInput,
                            onValueChange = { examNameInput = it },
                            label = { Text("Exam Name (e.g., SSC CGL Mock #5)") },
                            modifier = Modifier.fillMaxWidth().testTag("add_mock_name"),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = scoreInput,
                                onValueChange = { scoreInput = it },
                                label = { Text("Obtained Score") },
                                modifier = Modifier.weight(1f).testTag("add_mock_score"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = totalMarksInput,
                                onValueChange = { totalMarksInput = it },
                                label = { Text("Total Marks") },
                                modifier = Modifier.weight(1f).testTag("add_mock_total"),
                                singleLine = true
                            )
                        }

                        // Subject Selection
                        Column {
                            Text("Mock Subject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            var mockSubjectExpanded by remember { mutableStateOf(false) }
                            Box {
                                Button(
                                    onClick = { mockSubjectExpanded = true },
                                    modifier = Modifier.testTag("mock_subject_picker")
                                ) {
                                    Text(subjectInput)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = mockSubjectExpanded,
                                    onDismissRequest = { mockSubjectExpanded = false }
                                ) {
                                    subjects.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s) },
                                            onClick = {
                                                subjectInput = s
                                                mockSubjectExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        modifier = Modifier.testTag("add_mock_submit"),
                        onClick = {
                            if (examNameInput.isNotEmpty()) {
                                val s = scoreInput.toDoubleOrNull() ?: 0.0
                                val tm = totalMarksInput.toDoubleOrNull() ?: 100.0
                                viewModel.addMockTest(
                                    examName = examNameInput,
                                    score = s,
                                    totalMarks = tm,
                                    subject = subjectInput,
                                    date = System.currentTimeMillis()
                                )
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Log Score")
                    }
                }
            )
        }
    }
}

@Composable
fun LineImprovementChart(tests: List<MockTest>) {
    val colorScheme = MaterialTheme.colorScheme
    val scores = tests.reversed().map { test ->
        if (test.totalMarks > 0) ((test.score / test.totalMarks) * 100).toFloat() else 0f
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalPoints = scores.size

        if (totalPoints > 1) {
            val stepX = width / (totalPoints - 1)
            val path = androidx.compose.ui.graphics.Path()

            for (i in 0 until totalPoints) {
                // Percentage scales 0 to 100
                val percentVal = scores[i]
                val x = i * stepX
                val y = height - (percentVal / 100f) * height

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Draw bullet circles
                drawCircle(
                    color = colorScheme.secondary,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = colorScheme.primary,
                style = Stroke(width = 3.dp.toPx())
            )
        } else if (totalPoints == 1) {
            val percentVal = scores[0]
            val x = width / 2
            val y = height - (percentVal / 100f) * height
            drawCircle(
                color = colorScheme.primary,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
