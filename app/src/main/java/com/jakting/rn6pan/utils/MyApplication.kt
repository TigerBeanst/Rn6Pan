package com.jakting.rn6pan.utils

import android.app.Application
import android.content.Context

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
        var LOGIN_STATUS: Int = 0
        var DESTINATION = ""
        var STATE = ""
        var TOKEN = ""
        var COOKIES = ""
        lateinit var userInfo:UserInfo
        var nowPath = "/"
        var directoryOrderBy = ""
        var nameOrderBy = ""
        var typeOrderBy = ""
        var ctimeOrderBy = ""
    }
}