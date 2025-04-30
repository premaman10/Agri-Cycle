package com.example.agricycle.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Analysis : Screen("analysis")
    object Prediction : Screen("prediction")
    object Weather : Screen("weather")
    object SoilAnalysis : Screen("soil_analysis")
    object MarketPrices : Screen("market_prices")
    object Settings : Screen("settings")
    object Map : Screen("map")
    object Login : Screen("login")
    object Account : Screen("account")
    object News : Screen("news")
    object Notifications : Screen("notifications")
}