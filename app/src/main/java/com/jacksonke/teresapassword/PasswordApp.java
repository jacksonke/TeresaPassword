package com.jacksonke.teresapassword;

import android.app.Application;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class PasswordApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        DBHelper.getInstance().init(this);
    }
}
