@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.userFlow.collectAsState()
    
    var nameInput by remember { mutableStateOf("") }
    var targetExamsInput by remember { mutableStateOf("") }
    
    // Initialize text inputs when profile details load
    LaunchedEffect(user) {
        user?.let {
            nameInput = it.name
            targetExamsInput = it.targetExams
        }
    }

    var showLanguagesModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_root_view"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("settings"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
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
            // Profile Card (Editable name & target exams)
            Text(text = "My Student Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = targetExamsInput,
                        onValueChange = { targetExamsInput = it },
                        label = { Text("Target Exams (e.g. UPSC, SSC)") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_exams_input"),
                        singleLine = true
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth().testTag("btn_save_profile"),
                        onClick = {
                            viewModel.updateProfile(nameInput, targetExamsInput)
                        }
                    ) {
                        Text("Save Profile Changes")
                    }
                }
            }

            // Language config card
            Text(text = "Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // App Theme
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Active App Theme", fontWeight = FontWeight.Bold)
                            Text("Standard dynamic color styling", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        var themeExpanded by remember { mutableStateOf(false) }
                        Box {
                            Button(
                                onClick = { themeExpanded = true },
                                modifier = Modifier.testTag("theme_picker_btn")
                            ) {
                                Text(user?.appTheme ?: "SYSTEM")
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = themeExpanded,
                                onDismissRequest = { themeExpanded = false }
                            ) {
                                listOf("LIGHT", "DARK", "SYSTEM").forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t) },
                                        onClick = {
                                            viewModel.updateTheme(t)
                                            themeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Languages (EN, HI, HINGLISH) Method
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Multi-Lingual support", fontWeight = FontWeight.Bold)
                            Text("English, Hindi (हिंदी), Hinglish", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { showLanguagesModal = true },
                            modifier = Modifier.testTag("lang_picker_btn")
                        ) {
                            Text(user?.appLanguage ?: "EN")
                        }
                    }
                }
            }

            // Database backups segment
            Text(text = "Data backups & utilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your offline database is securely stored locally on this Android device. Backup and recovery files are written to internal space automatically.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).testTag("settings_backup_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup XML", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).testTag("settings_restore_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore DB", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Language selection dialog
        if (showLanguagesModal) {
            AlertDialog(
                onDismissRequest = { showLanguagesModal = false },
                title = { Text("Select Application Language", fontWeight = FontWeight.Black) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateLanguage("EN")
                                    showLanguagesModal = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = user?.appLanguage == "EN", onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("English")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateLanguage("HI")
                                    showLanguagesModal = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = user?.appLanguage == "HI", onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hindi (हिंदी)")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateLanguage("HIN")
                                    showLanguagesModal = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = user?.appLanguage == "HIN", onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hinglish")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguagesModal = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
