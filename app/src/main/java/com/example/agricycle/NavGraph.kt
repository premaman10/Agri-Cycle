package com.example.agricycle

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agricycle.navigation.Screen
import com.example.agricycle.ui.screens.*
import com.example.agricycle.ui.viewmodel.WeatherViewModel // Import WeatherViewModel
import com.example.agricycle.viewmodel.LoginViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    weatherViewModel: WeatherViewModel // Add WeatherViewModel parameter
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, loginViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController, loginViewModel)
        }
        composable(Screen.Weather.route) {
            // Pass the WeatherViewModel to WeatherScreen
            WeatherScreen(navController, weatherViewModel)
        }
        composable(Screen.Prediction.route) {
            PredictionScreen(navController)
        }
        composable(Screen.SoilAnalysis.route) {
            SoilAnalysisScreen(navController)
        }
        composable(Screen.MarketPrices.route) {
            MarketPricesScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController, loginViewModel)
        }
        composable(Screen.Analysis.route) {
            AnalysisScreen(navController)
        }
        composable(Screen.Map.route) {
            MapScreen(navController)
        }
        composable(Screen.Account.route) {
            AccountScreen(navController, loginViewModel)
        }
        composable(Screen.News.route) {
            NewsScreen(navController)
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController, weatherViewModel)
        }
    }
}