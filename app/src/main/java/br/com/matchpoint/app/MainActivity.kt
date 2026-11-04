package br.com.matchpoint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.matchpoint.presentation.navigation.AppNavHost
import br.com.matchpoint.presentation.theme.MatchPointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MatchPointTheme {
                AppNavHost()
            }
        }
    }
}
