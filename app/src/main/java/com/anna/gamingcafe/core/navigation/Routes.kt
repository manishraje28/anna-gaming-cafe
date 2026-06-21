package com.anna.gamingcafe.core.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Auth : Routes("auth")
    object CustomerHome : Routes("customer_home")
    object OwnerHome : Routes("owner_home")
}
