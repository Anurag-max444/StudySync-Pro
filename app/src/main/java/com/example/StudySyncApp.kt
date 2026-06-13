package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.StudyRepository

class StudySyncApp : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        StudyRepository(
            userDao = database.userDao(),
            taskDao = database.taskDao(),
            noteDao = database.noteDao(),
            studySessionDao = database.studySessionDao(),
            goalDao = database.goalDao(),
            mockTestDao = database.mockTestDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}
