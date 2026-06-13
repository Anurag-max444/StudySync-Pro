package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingData(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    
    val pages = listOf(
        OnboardingData(
            title = "Track Your Sessions",
            description = "Start precise timers for your subjects. Monitor your study routines with granular data entry tailored for SSC, UPSC, Banking, and school tests.",
            icon = Icons.Default.Book
        ),
        OnboardingData(
            title = "Boost Focus with Pomodoro",
            description = "Boost focus with custom Pomodoro timers. Seamless integration lets you log concentration intervals dynamically into your progress charts.",
            icon = Icons.Default.Schedule
        ),
        OnboardingData(
            title = "Analyze Your Progress",
            description = "Study intelligent dashboards, weekly scorecards, subject comparison indices and mock analytics to crush your exams successfully.",
            icon = Icons.Default.Analytics
        )
    )

    val currentData = pages[currentPage]

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_root"),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentPage + 1}/${pages.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onGetStarted,
                        modifier = Modifier.testTag("skip_button")
                    ) {
                        Text("Skip")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                        .testTag("onboarding_icon_bg"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentData.icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = currentData.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("onboarding_title")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentData.description,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("onboarding_desc")
                )
            }

            // Indicator and Navigation Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(width = if (index == currentPage) 24.dp else 8.dp, height = 8.dp)
                                .background(
                                    color = if (index == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            onGetStarted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (currentPage == pages.size - 1) "Get Started" else "Next",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
