@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val sessions by viewModel.allSessions.collectAsState()
    val goals by viewModel.allGoals.collectAsState()
    val tests by viewModel.allMockTests.collectAsState()

    // Aggregate statistics
    val totalHours = sessions.sumOf { it.durationSeconds }.toDouble() / 3600.0
    val totalSessionsCount = sessions.size

    val subjectSessionHours = sessions.groupBy { it.subject }.mapValues { entry ->
        entry.value.sumOf { it.durationSeconds }.toDouble() / 3600.0
    }

    val completedGoals = goals.filter { it.isCompleted }.size
    val totalGoalsCount = goals.size
    val goalCompRate = if (totalGoalsCount > 0) (completedGoals.toDouble() / totalGoalsCount.toDouble()) * 100.0 else 0.0

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen_view"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("analytics"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("analytics_back_btn")) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General metrics cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cumulative Hours", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${String.format("%.1f", totalHours)} hrs", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }

                Card(
                     modifier = Modifier.weight(1f),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Goal Completion", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${goalCompRate.toInt()}% Rate", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Subject Distribution Pie Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Subject-Wise Time Distribution",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (subjectSessionHours.isEmpty()) {
                        Text(
                            text = "Track study session logs to analyze subject distribution ratios here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Drawing static mock distribution chart
                        SubjectDistributionChart(subjectSessionHours)
                    }
                }
            }

            // Analytics Report Overview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Detailed Performance Summary",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Sessions Run:", fontSize = 13.sp)
                        Text("$totalSessionsCount times", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Unfinished Goals:", fontSize = 13.sp)
                        Text("${totalGoalsCount - completedGoals} left", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mock Tests Attempted:", fontSize = 13.sp)
                        Text("${tests.size} entries", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

data class PieSegment(val subject: String, val hours: Double, val color: Color)

@Composable
fun SubjectDistributionChart(data: Map<String, Double>) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.inversePrimary
    )

    val segments = data.entries.mapIndexed { index, entry ->
        PieSegment(entry.key, entry.value, colors[index % colors.size])
    }

    val totalHours = segments.sumOf { it.hours }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Draw Pie Canvas
        Canvas(modifier = Modifier.size(110.dp)) {
            val canvasSize = size.width
            var startingAngle = 0f

            segments.forEach { segment ->
                val sweep = ((segment.hours / totalHours) * 360f).toFloat()
                drawArc(
                    color = segment.color,
                    startAngle = startingAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(canvasSize, canvasSize)
                )
                startingAngle += sweep
            }
        }

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            segments.take(5).forEach { segment ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = segment.color)
                    }
                    Text(
                        text = "${segment.subject}: ${String.format("%.1f", segment.hours)}h",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
