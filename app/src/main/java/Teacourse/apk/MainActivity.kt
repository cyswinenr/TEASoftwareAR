package Teacourse.apk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import Teacourse.apk.navigation.NavGraph
import Teacourse.apk.ui.theme.茶文化Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            茶文化Theme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}