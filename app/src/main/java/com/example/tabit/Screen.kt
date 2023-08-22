package com.example.tabit

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Add : Screen("add")
    object Detail : Screen("detail/{imageUrl}")
    object Tracker : Screen("tracker")
    object Drawer : Screen("drawer")
    object Users : Screen("users")
}

