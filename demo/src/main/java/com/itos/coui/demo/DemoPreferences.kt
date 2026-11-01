package com.itos.coui.demo

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 应用状态由宿主持有；同任务的多个 Activity 共享，进程重建从本地偏好恢复。 */
@Stable
internal class DemoPreferences private constructor(context: Context) {
    private val store = context.applicationContext.getSharedPreferences("demo-settings", Context.MODE_PRIVATE)
    var darkOverride by mutableStateOf(if (store.contains("dark")) store.getBoolean("dark", false) else null)
        private set
    var materialTheme by mutableStateOf(store.getBoolean("materialTheme", true))
        private set
    fun materialTheme(value: Boolean) { materialTheme = value; store.edit().putBoolean("materialTheme", value).apply() }
    var notifications by mutableStateOf(store.getBoolean("notifications", true))
        private set
    var sound by mutableStateOf(store.getBoolean("sound", true))
        private set
    var automatic by mutableStateOf(store.getBoolean("automatic", true))
        private set
    var fontSize by mutableStateOf(store.getInt("font", 1))
        private set
    var colorMode by mutableStateOf(store.getInt("color", 0))
        private set
    var contour by mutableStateOf(store.getBoolean("contour", true))
        private set

    fun dark(value: Boolean) { darkOverride = value; store.edit().putBoolean("dark", value).apply() }
    fun notifications(value: Boolean) { notifications = value; store.edit().putBoolean("notifications", value).apply() }
    fun sound(value: Boolean) { sound = value; store.edit().putBoolean("sound", value).apply() }
    fun automatic(value: Boolean) { automatic = value; store.edit().putBoolean("automatic", value).apply() }
    fun font(value: Int) { fontSize = value; store.edit().putInt("font", value).apply() }
    fun color(value: Int) { colorMode = value; store.edit().putInt("color", value).apply() }
    fun contour(value: Boolean) { contour = value; store.edit().putBoolean("contour", value).apply() }
    fun reset() { notifications(true); sound(true); automatic(true); font(1); color(0); contour(true) }

    companion object {
        private var instance: DemoPreferences? = null
        fun get(context: Context): DemoPreferences = instance ?: DemoPreferences(context).also { instance = it }
    }
}
