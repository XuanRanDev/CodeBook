package dev.xuanran.codebook.util

import android.os.Process

object AppExitUtil {
    /**
     * Exit the app by killing the process
     */
    @JvmStatic
    fun exitApp() {
        // 终止当前进程
        Process.killProcess(Process.myPid())
        // 确保JVM关闭
        System.exit(0)
    }
}
