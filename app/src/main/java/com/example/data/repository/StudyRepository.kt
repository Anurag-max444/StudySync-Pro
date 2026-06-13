package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class StudyRepository(
    private val userDao: UserDao,
    private val taskDao: TaskDao,
    private val noteDao: NoteDao,
    private val studySessionDao: StudySessionDao,
    private val goalDao: GoalDao,
    private val mockTestDao: MockTestDao
) {
    // User operations
    val userFlow: Flow<User?> = userDao.getUserFlow()
    suspend fun getUserSync(): User? = userDao.getUserSync()
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    // Task operations
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Int) = taskDao.deleteTaskById(id)

    // Note operations
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Int) = noteDao.deleteNoteById(id)

    // StudySession operations
    val allSessions: Flow<List<StudySession>> = studySessionDao.getAllSessions()
    suspend fun insertSession(session: StudySession) = studySessionDao.insertSession(session)
    suspend fun deleteSession(session: StudySession) = studySessionDao.deleteSession(session)

    // Goal operations
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)
    suspend fun deleteGoalById(id: Int) = goalDao.deleteGoalById(id)

    // MockTest operations
    val allMockTests: Flow<List<MockTest>> = mockTestDao.getAllMockTests()
    suspend fun insertMockTest(mockTest: MockTest) = mockTestDao.insertMockTest(mockTest)
    suspend fun deleteMockTest(mockTest: MockTest) = mockTestDao.deleteMockTest(mockTest)
    suspend fun deleteMockTestById(id: Int) = mockTestDao.deleteMockTestById(id)
}
