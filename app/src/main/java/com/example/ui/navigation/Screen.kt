package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object SessionTracker : Screen("session_tracker")
    object Pomodoro : Screen("pomodoro")
    object TaskManager : Screen("task_manager")
    object NotesManager : Screen("notes_manager")
    object GoalTracker : Screen("goal_tracker")
    object MockTestTracker : Screen("mock_test_tracker")
    object Analytics : Screen("analytics")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
}
