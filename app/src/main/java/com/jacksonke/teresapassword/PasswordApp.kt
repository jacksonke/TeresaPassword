package com.jacksonke.teresapassword

import android.app.Application

class PasswordApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DBHelper.instance.init(this)
    }
}