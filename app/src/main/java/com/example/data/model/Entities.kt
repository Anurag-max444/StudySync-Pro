package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1, // Single user local profile
    val name: String,
    val targetExams: String, // Comma separated, e.g., "SSC, UPSC"
    val streakCount: Int = 0,
    val lastActiveDate: String = "",
    val appTheme: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val appLanguage: String = "EN", // EN (English), HI (Hindi), HIN (Hinglish)
    val notificationsEnabled: Boolean = true
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // HIGH, MEDIUM, LOW
    val dueDate: Long, // timestamp
    val isCompleted: Boolean = false,
    val subject: String
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val subject: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetHours: Double,
    val progressHours: Double = 0.0,
    val type: String, // DAILY, WEEKLY, MONTHLY
    val isCompleted: Boolean = false,
    val dueDate: Long
)

@Entity(tableName = "mock_tests")
data class MockTest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examName: String,
    val score: Double,
    val totalMarks: Double = 100.0,
    val date: Long = System.currentTimeMillis(),
    val subject: String
)
