package com.itos.coui.demo

import android.app.Application
import com.itos.coui.material3.CouiMaterial3Style
import com.itos.coui.theme.Coui

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Coui.initialize(defaultStyle = CouiMaterial3Style())
    }
}
