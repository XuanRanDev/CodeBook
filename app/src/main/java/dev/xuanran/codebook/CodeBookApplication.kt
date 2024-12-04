package dev.xuanran.codebook;

import android.app.Application;

import dev.xuanran.codebook.service.impl.CrashHandler;

public class CodeBookApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.init(this);
    }
}
