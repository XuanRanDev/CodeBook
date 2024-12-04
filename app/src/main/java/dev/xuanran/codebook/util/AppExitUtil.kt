package dev.xuanran.codebook.util;

import android.os.Process;


public class AppExitUtil {

    /**
     * Exit the app by killing the process
     */
    public static void exitApp() {
        // 终止当前进程
        Process.killProcess(Process.myPid());
        // 确保JVM关闭
        System.exit(0);
    }
}
