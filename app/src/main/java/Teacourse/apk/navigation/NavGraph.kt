package Teacourse.apk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import Teacourse.apk.ui.screens.InfoInputScreen
import Teacourse.apk.ui.screens.SplashScreen
import Teacourse.apk.ui.screens.Task1Screen
import Teacourse.apk.ui.screens.Task2Screen
import Teacourse.apk.ui.screens.TaskDetailScreen
import Teacourse.apk.ui.screens.TaskOverviewScreen
import Teacourse.apk.ui.screens.Thinking1Screen
import Teacourse.apk.ui.screens.Thinking2Screen
import Teacourse.apk.ui.screens.CreativeScreen
import Teacourse.apk.ui.screens.SummaryScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object InfoInput : Screen("info_input")
    object TaskOverview : Screen("task_overview")
    object Task1 : Screen("task_1")
    object Task2 : Screen("task_2")
    object Thinking1 : Screen("thinking_1")
    object Thinking2 : Screen("thinking_2")
    object Creative : Screen("creative")
    object Summary : Screen("summary")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onStartClick = {
                    navController.navigate(Screen.InfoInput.route)
                }
            )
        }
        
        composable(Screen.InfoInput.route) {
            InfoInputScreen(
                onNextClick = {
                    navController.navigate(Screen.TaskOverview.route) {
                        popUpTo(Screen.InfoInput.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.InfoInput.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.TaskOverview.route) {
            TaskOverviewScreen(
                onTaskClick = { taskRoute ->
                    navController.navigate(taskRoute)
                },
                onBackClick = {
                    navController.navigate(Screen.InfoInput.route) {
                        popUpTo(Screen.TaskOverview.route) { inclusive = true }
                    }
                },
                onSummaryClick = {
                    navController.navigate(Screen.Summary.route)
                }
            )
        }
        
        composable(Screen.Task1.route) {
            Task1Screen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Task2.route) {
            Task2Screen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Thinking1.route) {
            Thinking1Screen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Thinking2.route) {
            Thinking2Screen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Creative.route) {
            CreativeScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Summary.route) {
            SummaryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToTask1 = {
                    navController.navigate(Screen.Task1.route) {
                        popUpTo(Screen.Summary.route)
                    }
                },
                onNavigateToTask2 = {
                    navController.navigate(Screen.Task2.route) {
                        popUpTo(Screen.Summary.route)
                    }
                },
                onNavigateToThinking1 = {
                    navController.navigate(Screen.Thinking1.route) {
                        popUpTo(Screen.Summary.route)
                    }
                },
                onNavigateToThinking2 = {
                    navController.navigate(Screen.Thinking2.route) {
                        popUpTo(Screen.Summary.route)
                    }
                }
            )
        }
    }
}

