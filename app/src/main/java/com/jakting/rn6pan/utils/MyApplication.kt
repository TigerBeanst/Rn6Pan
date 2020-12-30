package com.jakting.rn6pan.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.jakting.rn6pan.api.data.UserInfo
import com.maning.imagebrowserlibrary.model.ImageBrowserConfig

class MyApplication : Application() {
    @SuppressLint("CommitPrefEdits")
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        settingSharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        settingSharedPreferencesEditor = getSharedPreferences("settings", MODE_PRIVATE).edit()
    }

    companion object {
        lateinit var appContext: Context
        lateinit var settingSharedPreferences: SharedPreferences
        lateinit var settingSharedPreferencesEditor: SharedPreferences.Editor
        var LOGIN_STATUS: Int = 0
        var DESTINATION = ""
        var STATE = ""
        var TOKEN = ""
        var COOKIES = ""
        lateinit var userInfo: UserInfo
        var nowTimeStamp:Long = 0
        var parentPathList = arrayListOf<String>("/")
        var orderFlag = 0 // 0 为 name，1 为 ctime
        var defaultOrder = true
        var labelFilter = 0
        var nameOrderBy = ""
        var ctimeOrderBy = ""
    }
}