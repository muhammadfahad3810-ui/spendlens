package com.fahad.spendlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fahad.spendlens.ui.SpendLensApp
import com.fahad.spendlens.ui.theme.SpendlensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendlensTheme {
                SpendLensApp()
            }
        }
    }
}