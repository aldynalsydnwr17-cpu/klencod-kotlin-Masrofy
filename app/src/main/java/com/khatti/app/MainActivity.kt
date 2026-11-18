package com.khatti.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.khatti.app.ui.KhattiApp
import com.khatti.app.ui.KhattiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KhattiTheme { KhattiApp() } }
    }
}
