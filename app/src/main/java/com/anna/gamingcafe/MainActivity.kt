package com.anna.gamingcafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.anna.gamingcafe.core.navigation.AppNavigation
import com.anna.gamingcafe.core.theme.AnnaGamingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnnaGamingTheme {
                AppNavigation()
            }
        }
    }
}
