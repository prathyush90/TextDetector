package com.example.noob.textdetector;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.FirebaseApp;

import static com.example.noob.textdetector.BuildConfig.APPLICATION_ID;

public class SignageApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

            FirebaseApp.initializeApp(this);

    }

    private boolean isMainProcess(Context context) {
        if (null == context) {
            return true;
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        int pid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            String name = processInfo.processName;
            if (!TextUtils.isEmpty(name) && pid == processInfo.pid && name.equals(APPLICATION_ID)) {
                return true;
            }
        }
        return false;
    }
}
