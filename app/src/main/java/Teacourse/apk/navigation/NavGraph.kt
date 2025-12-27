package Teacourse.apk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import Teacourse.apk.ui.screens.InfoInputScreen
import Teacourse.apk.ui.screens.SplashScreen
import Teacourse.apk.ui.screens.TaskDetailScreen
import Teacourse.apk.ui.screens.TaskOverviewScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object InfoInput : Screen("info_input")
    object TaskOverview : Screen("task_overview")
    object Task1 : Screen("task_1")
    object Task2 : Screen("task_2")
    object Thinking1 : Screen("thinking_1")
    object Thinking2 : Screen("thinking_2")
    object Creative : Screen("creative")
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
                    navController.navigate(Screen.InfoInput.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.InfoInput.route) {
            InfoInputScreen(
                onNextClick = {
                    navController.navigate(Screen.TaskOverview.route) {
                        popUpTo(Screen.InfoInput.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.TaskOverview.route) {
            TaskOverviewScreen(
                onTaskClick = { taskRoute ->
                    navController.navigate(taskRoute)
                }
            )
        }
        
        composable(Screen.Task1.route) {
            TaskDetailScreen(
                title = "任务一：泡茶体验、品茶时",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Task2.route) {
            TaskDetailScreen(
                title = "任务二：泡出你心中的那杯茶",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Thinking1.route) {
            TaskDetailScreen(
                title = "思考题一",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Thinking2.route) {
            TaskDetailScreen(
                title = "思考题二",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Creative.route) {
            TaskDetailScreen(
                title = "创意题",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

