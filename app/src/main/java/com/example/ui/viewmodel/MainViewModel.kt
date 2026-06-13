package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.StudySyncApp
import com.example.data.model.*
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(
    application: Application,
    private val repository: StudyRepository
) : AndroidViewModel(application) {

    // --- Localization definitions ---
    val languages = listOf("EN", "HI", "HIN")
    
    // User Session
    val userFlow: StateFlow<User?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Set up default user if none exists
    init {
        viewModelScope.launch {
            val user = repository.getUserSync()
            if (user == null) {
                repository.insertUser(
                    User(
                        name = "Aspirant",
                        targetExams = "SSC, Railway",
                        streakCount = 3,
                        lastActiveDate = getTodayDateString(),
                        appTheme = "SYSTEM",
                        appLanguage = "EN"
                    )
                )
            } else {
                // Check and update streak if needed
                val today = getTodayDateString()
                if (user.lastActiveDate != today && user.lastActiveDate.isNotEmpty()) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    try {
                        val d1 = sdf.parse(user.lastActiveDate)
                        val d2 = sdf.parse(today)
                        val diff = d2.time - d1.time
                        val diffDays = diff / (24 * 60 * 60 * 1000)
                        if (diffDays == 1L) {
                            repository.updateUser(user.copy(streakCount = user.streakCount + 1, lastActiveDate = today))
                        } else if (diffDays > 1L) {
                            repository.updateUser(user.copy(streakCount = 1, lastActiveDate = today))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (user.lastActiveDate.isEmpty()) {
                    repository.updateUser(user.copy(lastActiveDate = today))
                }
            }
        }
    }

    // --- State Streams ---
    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMockTests: StateFlow<List<MockTest>> = repository.allMockTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Pomodoro State ---
    private val _pomodoroSecondsRemaining = MutableStateFlow(25 * 60)
    val pomodoroSecondsRemaining = _pomodoroSecondsRemaining.asStateFlow()

    private val _pomodoroTotalSeconds = MutableStateFlow(25 * 60)
    val pomodoroTotalSeconds = _pomodoroTotalSeconds.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning = _isPomodoroRunning.asStateFlow()

    private val _isPomodoroBreak = MutableStateFlow(false)
    val isPomodoroBreak = _isPomodoroBreak.asStateFlow()

    private val _pomodoroSessionsCompleted = MutableStateFlow(0)
    val pomodoroSessionsCompleted = _pomodoroSessionsCompleted.asStateFlow()

    var autoBreakMode = true
    var pomodoroWorkDurationMinutes = 25
    var pomodoroBreakDurationMinutes = 5

    private var pomodoroJob: Job? = null

    // --- Study Session Tracker State ---
    private val _isTrackingSession = MutableStateFlow(false)
    val isTrackingSession = _isTrackingSession.asStateFlow()

    private val _isSessionPaused = MutableStateFlow(false)
    val isSessionPaused = _isSessionPaused.asStateFlow()

    private val _sessionElapsedSeconds = MutableStateFlow(0L)
    val sessionElapsedSeconds = _sessionElapsedSeconds.asStateFlow()

    private val _selectedSessionSubject = MutableStateFlow("Quantitative Aptitude")
    val selectedSessionSubject = _selectedSessionSubject.asStateFlow()

    private var trackingJob: Job? = null

    fun startTrackingSession(subject: String) {
        _selectedSessionSubject.value = subject
        _isTrackingSession.value = true
        _isSessionPaused.value = false
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            while (_isTrackingSession.value) {
                if (!_isSessionPaused.value) {
                    delay(1000)
                    _sessionElapsedSeconds.value += 1
                } else {
                    delay(500)
                }
            }
        }
    }

    fun pauseTrackingSession() {
        _isSessionPaused.value = true
    }

    fun resumeTrackingSession() {
        _isSessionPaused.value = false
    }

    fun stopTrackingSession(notes: String) {
        viewModelScope.launch {
            val elapsed = _sessionElapsedSeconds.value
            if (elapsed > 0) {
                // Save study history
                repository.insertSession(
                    StudySession(
                        subject = _selectedSessionSubject.value,
                        durationSeconds = elapsed,
                        timestamp = System.currentTimeMillis(),
                        notes = notes
                    )
                )

                // Logically update any goals matching type
                val hoursAdded = elapsed.toDouble() / 3600.0
                val activeGoals = allGoals.value
                activeGoals.forEach { goal ->
                    if (!goal.isCompleted) {
                        val progress = goal.progressHours + hoursAdded
                        val completed = progress >= goal.targetHours
                        repository.updateGoal(goal.copy(progressHours = progress, isCompleted = completed))
                    }
                }
            }
            // Clear tracking state
            _isTrackingSession.value = false
            _isSessionPaused.value = false
            _sessionElapsedSeconds.value = 0
            trackingJob?.cancel()
            trackingJob = null
        }
    }

    // --- Pomodoro Operations ---
    fun startPomodoro() {
        if (_isPomodoroRunning.value) return
        _isPomodoroRunning.value = true
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (_isPomodoroRunning.value && _pomodoroSecondsRemaining.value > 0) {
                delay(1000)
                _pomodoroSecondsRemaining.value -= 1
            }
            if (_pomodoroSecondsRemaining.value == 0) {
                handlePomodoroCycleCompletion()
            }
        }
    }

    fun pausePomodoro() {
        _isPomodoroRunning.value = false
        pomodoroJob?.cancel()
        pomodoroJob = null
    }

    fun stopPomodoro() {
        pausePomodoro()
        configurePomodoroDurations(pomodoroWorkDurationMinutes, pomodoroBreakDurationMinutes)
        _isPomodoroBreak.value = false
    }

    fun configurePomodoroDurations(workMin: Int, breakMin: Int) {
        pomodoroWorkDurationMinutes = workMin
        pomodoroBreakDurationMinutes = breakMin
        val mins = if (_isPomodoroBreak.value) breakMin else workMin
        _pomodoroSecondsRemaining.value = mins * 60
        _pomodoroTotalSeconds.value = mins * 60
    }

    private fun handlePomodoroCycleCompletion() {
        viewModelScope.launch {
            if (!_isPomodoroBreak.value) {
                // Work session completed
                _pomodoroSessionsCompleted.value += 1
                // Automatically save as a micro study session
                repository.insertSession(
                    StudySession(
                        subject = "Pomodoro Focus Mode",
                        durationSeconds = (pomodoroWorkDurationMinutes * 60).toLong(),
                        timestamp = System.currentTimeMillis(),
                        notes = "Completed Pomodoro Session #${_pomodoroSessionsCompleted.value}"
                    )
                )
                if (autoBreakMode) {
                    _isPomodoroBreak.value = true
                    _pomodoroSecondsRemaining.value = pomodoroBreakDurationMinutes * 60
                    _pomodoroTotalSeconds.value = pomodoroBreakDurationMinutes * 60
                    startPomodoro()
                } else {
                    stopPomodoro()
                }
            } else {
                // Break session completed
                _isPomodoroBreak.value = false
                _pomodoroSecondsRemaining.value = pomodoroWorkDurationMinutes * 60
                _pomodoroTotalSeconds.value = pomodoroWorkDurationMinutes * 60
                stopPomodoro()
            }
        }
    }

    // --- Profile Operations ---
    fun updateProfile(name: String, exams: String) {
        viewModelScope.launch {
            userFlow.value?.let { current ->
                repository.updateUser(current.copy(name = name, targetExams = exams))
            }
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            userFlow.value?.let { current ->
                repository.updateUser(current.copy(appLanguage = lang))
            }
        }
    }

    fun updateTheme(themeStr: String) {
        viewModelScope.launch {
            userFlow.value?.let { current ->
                repository.updateUser(current.copy(appTheme = themeStr))
            }
        }
    }

    // --- Operations: Tasks ---
    fun addTask(title: String, description: String, priority: String, dueDate: Long, subject: String) {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    subject = subject
                )
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Operations: Notes ---
    fun addNote(title: String, content: String, subject: String) {
        viewModelScope.launch {
            repository.insertNote(Note(title = title, content = content, subject = subject))
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }

    // --- Operations: Goals ---
    fun addGoal(title: String, targetHours: Double, type: String, dueDate: Long) {
        viewModelScope.launch {
            repository.insertGoal(Goal(title = title, targetHours = targetHours, type = type, dueDate = dueDate))
        }
    }

    fun updateGoalProgress(goal: Goal, progress: Double) {
        viewModelScope.launch {
            val completed = progress >= goal.targetHours
            repository.updateGoal(goal.copy(progressHours = progress, isCompleted = completed))
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            repository.deleteGoalById(goalId)
        }
    }

    // --- Operations: Mock Test ---
    fun addMockTest(examName: String, score: Double, totalMarks: Double, subject: String, date: Long) {
        viewModelScope.launch {
            repository.insertMockTest(
                MockTest(
                    examName = examName,
                    score = score,
                    totalMarks = totalMarks,
                    subject = subject,
                    date = date
                )
            )
        }
    }

    fun deleteMockTest(testId: Int) {
        viewModelScope.launch {
            repository.deleteMockTestById(testId)
        }
    }

    // --- Helper String Utilities ---
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- Translations Handler ---
    fun translate(key: String): String {
        val lang = userFlow.value?.appLanguage ?: "EN"
        return Translations.get(key, lang)
    }
}

// Singleton Factory for MainViewModel (standard robust Constructor Injection)
class MainViewModelFactory(
    private val application: Application,
    private val repository: StudyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- High-Fidelity Localization Engine for English, Hindi, and Hinglish ---
object Translations {
    private val data = mapOf(
        "app_name" to mapOf(
            "EN" to "StudySync Pro",
            "HI" to "स्टडीसिंक प्रो",
            "HIN" to "StudySync Pro"
        ),
        "streak" to mapOf(
            "EN" to "Daily Streak",
            "HI" to "दैनिक लय (स्ट्रोक)",
            "HIN" to "Daily Streak"
        ),
        "days" to mapOf(
            "EN" to "days",
            "HI" to "दिन",
            "HIN" to "days"
        ),
        "study_hours_today" to mapOf(
            "EN" to "Study Hours Today",
            "HI" to "आज के पढ़ाई के घंटे",
            "HIN" to "Aaj ki Study Hours"
        ),
        "tasks_due" to mapOf(
            "EN" to "Tasks Due",
            "HI" to "शेष कार्य",
            "HIN" to "Tasks Bachen hai"
        ),
        "quick_actions" to mapOf(
            "EN" to "Quick Actions",
            "HI" to "त्वरित विकल्प",
            "HIN" to "Quick Actions"
        ),
        "tracker" to mapOf(
            "EN" to "Timer / Tracker",
            "HI" to "समय / ट्रैकर",
            "HIN" to "Timer / Tracker"
        ),
        "goal_tracker" to mapOf(
            "EN" to "Goal Tracker",
            "HI" to "लक्ष्य ट्रैकर",
            "HIN" to "Goal Tracker"
        ),
        "mock_tests" to mapOf(
            "EN" to "Mock Tests",
            "HI" to "मॉक टेस्ट",
            "HIN" to "Mock Tests"
        ),
        "notes" to mapOf(
            "EN" to "Notes",
            "HI" to "टिप्पणियाँ",
            "HIN" to "Notes"
        ),
        "analytics" to mapOf(
            "EN" to "Analytics",
            "HI" to "विश्लेषण",
            "HIN" to "Analytics State"
        ),
        "settings" to mapOf(
            "EN" to "Settings",
            "HI" to "सेटिंग्स",
            "HIN" to "Settings"
        ),
        "dashboard" to mapOf(
            "EN" to "Dashboard",
            "HI" to "डैशबोर्ड",
            "HIN" to "Dashboard"
        ),
        "tasks" to mapOf(
            "EN" to "Tasks",
            "HI" to "काम",
            "HIN" to "Kaam"
        ),
        "pomodoro" to mapOf(
            "EN" to "Pomodoro",
            "HI" to "पोमोडोरो",
            "HIN" to "Pomodoro"
        ),
        "quote_title" to mapOf(
            "EN" to "Inspiration of the Day",
            "HI" to "आज का विचार",
            "HIN" to "Aaj ka Motivational Quote"
        ),
        "quote" to mapOf(
            "EN" to "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            "HI" to "सफलता अंतिम नहीं है, असफलता घातक नहीं है: जारी रखने का साहस ही मायने रखता है।",
            "HIN" to "Success final nahi hai, failure fatal nahi hai: aage badhte rehne ka dumm hi matter karta hai."
        ),
        "start_session" to mapOf(
            "EN" to "Start Session",
            "HI" to "सत्र शुरू करें",
            "HIN" to "Session Start karo"
        ),
        "session_active" to mapOf(
            "EN" to "Study Session Running",
            "HI" to "अध्ययन सत्र चल रहा है",
            "HIN" to "Study Session Chal raha hai"
        ),
        "pause" to mapOf(
            "EN" to "Pause",
            "HI" to "रोकें",
            "HIN" to "Pause karo"
        ),
        "resume" to mapOf(
            "EN" to "Resume",
            "HI" to "जारी रखें",
            "HIN" to "Resume karo"
        ),
        "stop_and_save" to mapOf(
            "EN" to "Stop and Save",
            "HI" to "रोकें और सहेजें",
            "HIN" to "Stop karke Save karo"
        ),
        "subject" to mapOf(
            "EN" to "Subject",
            "HI" to "विषय",
            "HIN" to "Subject"
        ),
        "study_notes" to mapOf(
            "EN" to "Study Session Notes",
            "HI" to "अध्ययन के नोट्स",
            "HIN" to "Study ke Notes"
        ),
        "calendar" to mapOf(
            "EN" to "Calendar",
            "HI" to "कैलेंडर",
            "HIN" to "Calendar"
        )
    )

    fun get(key: String, lang: String): String {
        return data[key]?.get(lang) ?: data[key]?.get("EN") ?: key
    }
}
