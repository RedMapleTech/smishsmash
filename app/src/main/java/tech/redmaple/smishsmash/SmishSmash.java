package tech.redmaple.smishsmash;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class SmishSmash extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        SmishSmash.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SmishSmash.context;
    }
}
