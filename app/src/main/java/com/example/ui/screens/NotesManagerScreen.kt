@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.ui.viewmodel.MainViewModel

@Composable
fun NotesManagerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.allNotes.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    
    var showDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteSubject by remember { mutableStateOf("Quantitative Aptitude") }

    val subjects = listOf("All", "Quantitative Aptitude", "General Intelligence & Reasoning", "English Language", "General Awareness", "UPSC General Studies", "School/College Subject")
    val creationSubjects = subjects.drop(1) // exclude "All"

    // Search and Filter Notes
    val filteredNotes = notes.filter { note ->
        val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) || 
                            note.content.contains(searchQuery, ignoreCase = true)
        val matchesSubject = selectedSubjectFilter == "All" || note.subject == selectedSubjectFilter
        matchesSearch && matchesSubject
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notes_root"),
        topBar = {
            TopAppBar(
                title = { Text(viewModel.translate("notes"), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notes_back")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    noteToEdit = null
                    noteTitle = ""
                    noteContent = ""
                    noteSubject = "Quantitative Aptitude"
                    showDialog = true
                },
                modifier = Modifier.testTag("fab_add_note"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Notes")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search your notes...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Subject Filters Horizontal Row
            HorizontalDivider()
            Text("Filter by Subject", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var filterExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { filterExpanded = true },
                        modifier = Modifier.testTag("subject_filter_btn")
                    ) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(selectedSubjectFilter)
                    }
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    selectedSubjectFilter = s
                                    filterExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No study notes found! Add brief outlines, formula lists, or exam revision lists.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotes) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("note_item_${note.id}")
                                .clickable {
                                    noteToEdit = note
                                    noteTitle = note.title
                                    noteContent = note.content
                                    noteSubject = note.subject
                                    showDialog = true
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = note.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.content,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 4
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = note.subject,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteNote(note.id) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .testTag("delete_note_${note.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete note",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (noteToEdit == null) "Add Revision Notes" else "Edit Notes", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("Note Title *") },
                            modifier = Modifier.fillMaxWidth().testTag("add_note_title"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("Describe details, shortcuts or reminders...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("add_note_content")
                        )

                        // Subject Selection
                        Column {
                            Text("Related Subject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            var createExpanded by remember { mutableStateOf(false) }
                            Box {
                                Button(
                                    onClick = { createExpanded = true },
                                    modifier = Modifier.testTag("note_subject_picker")
                                ) {
                                    Text(noteSubject)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = createExpanded,
                                    onDismissRequest = { createExpanded = false }
                                ) {
                                    creationSubjects.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s) },
                                            onClick = {
                                                noteSubject = s
                                                createExpanded = false
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
                        modifier = Modifier.testTag("add_note_submit"),
                        onClick = {
                            if (noteTitle.isNotEmpty()) {
                                if (noteToEdit == null) {
                                    viewModel.addNote(
                                        title = noteTitle,
                                        content = noteContent,
                                        subject = noteSubject
                                    )
                                } else {
                                    viewModel.updateNote(
                                        noteToEdit!!.copy(
                                            title = noteTitle,
                                            content = noteContent,
                                            subject = noteSubject
                                        )
                                    )
                                }
                                showDialog = false
                            }
                        }
                    ) {
                        Text(if (noteToEdit == null) "Create Note" else "Save Notes")
                    }
                }
            )
        }
    }
}
