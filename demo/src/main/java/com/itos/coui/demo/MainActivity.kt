package com.itos.coui.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat

class MainActivity : DemoHostActivity() {
    @androidx.compose.runtime.Composable
    internal override fun Page(dark: Boolean, preferences: DemoPreferences) {
        DemoApp(dark, preferences, onToggleTheme = { preferences.dark(!dark) }, openPage = { DemoSecondaryActivity.open(this, it) })
    }
}

/** 普通不透明 Activity，不覆盖 ROM 的 Window 动画，不截获 Activity 根级返回。 */
abstract class DemoHostActivity : ComponentActivity() {
    @androidx.compose.runtime.Composable
    internal abstract fun Page(dark: Boolean, preferences: DemoPreferences)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val preferences = androidx.compose.runtime.remember { DemoPreferences.get(this) }
            val dark = preferences.darkOverride ?: systemDark
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            DemoTheme(dark = dark, materialTheme = preferences.materialTheme) {
                Page(dark, preferences)
            }
        }
    }
}
