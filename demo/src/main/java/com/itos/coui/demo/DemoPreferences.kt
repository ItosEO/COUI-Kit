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
    var materialTheme by mutableStateOf(store.getBoolean("materialTheme", false))
        private set
    fun materialTheme(value: Boolean) { materialTheme = value; store.edit().putBoolean("materialTheme", value).apply() }
    var contour by mutableStateOf(store.getBoolean("contour", true))
        private set

    fun dark(value: Boolean) { darkOverride = value; store.edit().putBoolean("dark", value).apply() }
    fun contour(value: Boolean) { contour = value; store.edit().putBoolean("contour", value).apply() }
    fun reset() { materialTheme(false); contour(true) }

    companion object {
        private var instance: DemoPreferences? = null
        fun get(context: Context): DemoPreferences = instance ?: DemoPreferences(context).also { instance = it }
    }
}
