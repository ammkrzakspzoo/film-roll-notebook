package com.filmroll.notebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.filmroll.notebook.core.di.ServiceLocator
import com.filmroll.notebook.core.theme.AppTheme
import com.filmroll.notebook.presentation.app.AppRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        setContent {
            AppTheme {
                AppRoot()
            }
        }
    }
}

