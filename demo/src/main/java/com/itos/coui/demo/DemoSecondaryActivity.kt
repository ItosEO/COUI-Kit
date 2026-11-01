package com.itos.coui.demo

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable

/** 每个页面是同一任务中的真实 Activity；标准启动/结束交由当前 ROM 处理转场。 */
class DemoSecondaryActivity : DemoHostActivity() {
    @Composable
    internal override fun Page(dark: Boolean, preferences: DemoPreferences) {
        DemoSecondaryPage(page = intent.getStringExtra(PageExtra) ?: "settings", dark = dark,
            preferences = preferences, back = { finish() }, open = { open(this, it) })
    }

    companion object {
        private const val PageExtra = "com.itos.coui.demo.PAGE"
        internal fun open(activity: Activity, page: String) {
            activity.startActivity(Intent(activity, DemoSecondaryActivity::class.java).putExtra(PageExtra, page))
        }
    }
}
