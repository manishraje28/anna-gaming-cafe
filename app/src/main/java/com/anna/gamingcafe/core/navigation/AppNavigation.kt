package com.anna.gamingcafe.core.navigation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anna.gamingcafe.core.theme.*
import com.anna.gamingcafe.feature.auth.AuthScreen
import com.anna.gamingcafe.feature.auth.AuthViewModel
import com.anna.gamingcafe.feature.customer.*
import com.anna.gamingcafe.feature.owner.*

@Composable
fun AppNavigation() {
    val authVm: AuthViewModel = viewModel()
    val state = authVm.uiState

    when {
        !state.isAuthenticated -> AuthScreen(authVm)
        state.userRole == "OWNER" -> {
            val dashVm: OwnerDashboardViewModel = viewModel()
            val chatVm: OwnerChatViewModel = viewModel()
            OwnerShell(dashVm, chatVm, onSignOut = { authVm.signOut() })
        }
        else -> CustomerShell(onSignOut = { authVm.signOut() })
    }
}

@Composable
fun CustomerShell(onSignOut: () -> Unit) {
    var activeTab by remember { mutableStateOf("home") }
    val homeVm: HomeViewModel = viewModel()
    val bookingVm: BookingViewModel = viewModel()
    val gamesVm: GamesViewModel = viewModel()
    val chatVm: ChatViewModel = viewModel()
    val walletVm: WalletViewModel = viewModel()

    val tabs = listOf(
        "home" to Icons.Default.Explore,
        "book" to Icons.Default.CalendarMonth,
        "games" to Icons.Default.SportsEsports,
        "chat" to Icons.Default.Forum,
        "wallet" to Icons.Default.Person
    )

    Scaffold(
        containerColor = BackBg,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 0.dp,
                modifier = Modifier.border(BorderStroke(1.dp, EdgeBorder), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                tabs.forEach { (id, icon) ->
                    val sel = activeTab == id
                    NavigationBarItem(
                        selected = sel,
                        onClick = { activeTab = id },
                        icon = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
                        label = { Text(id.replaceFirstChar { it.uppercase() }, fontSize = 9.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = TextPrimary,
                            unselectedTextColor = TextMuted,
                            indicatorColor = NeonCyan.copy(0.1f)
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (activeTab) {
                "home" -> HomeScreen(homeVm, onNavigate = { activeTab = it })
                "book" -> BookingScreen(bookingVm)
                "games" -> GamesCatalogScreen(gamesVm)
                "chat" -> ChatScreen(chatVm)
                "wallet" -> WalletScreen(walletVm, onSignOut = onSignOut)
            }
        }
    }
}
