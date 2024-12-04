package dev.xuanran.codebook

import android.app.Application
import dev.xuanran.codebook.service.impl.CrashHandler

class CodeBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashHandler.init(this)
    }
}
