package dev.xuanran.codebook.service.impl;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;

import dev.xuanran.codebook.activity.ExceptionActivity;
public class CrashHandler {

    private static final String FORMAT = "%1$s (%2$d)";

    public static void init(Application app) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            PackageInfo packageInfo = getPackageInfo(app);
            String log = generateLog(throwable, packageInfo);

            Intent intent = new Intent(app, ExceptionActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Intent.EXTRA_TEXT, log);

            app.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });
    }

    private static PackageInfo getPackageInfo(Application app) {
        try {
            return app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static String generateLog(Throwable throwable, PackageInfo packageInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("BRAND=").append(Build.BRAND);
        sb.append("\nMODEL=").append(Build.MODEL);
        sb.append("\nSDK Level=").append(String.format(FORMAT, Build.VERSION.RELEASE, Build.VERSION.SDK_INT));

        if (packageInfo != null) {
            sb.append("\nVersion=").append(String.format(FORMAT, packageInfo.versionName, packageInfo.versionCode));
        }

        sb.append("\n\n").append(getStackTrace(throwable));
        return sb.toString();
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }
        return sw.toString();
    }
}
