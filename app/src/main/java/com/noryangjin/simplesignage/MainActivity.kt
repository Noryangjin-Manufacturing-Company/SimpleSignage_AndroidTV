package com.noryangjin.simplesignage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.navigation.compose.*
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.noryangjin.simplesignage.ui.theme.SimpleSignageTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleSignageTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Main.route
                ) {
                    composable(Screen.Main.route) {
                        MainScreen(navController)
                    }
                    composable(Screen.Photo.route) {
                        PhotoScreen()
                    }
                    composable(Screen.Calendar.route){
                        CalendarScreen()
                    }
                }
            }
        }
    }
}