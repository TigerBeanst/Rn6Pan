package com.jakting.rn6pan.utils

import android.app.Application
import android.content.Context
import com.jakting.rn6pan.api.data.UserInfo

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
        lateinit var userInfo: UserInfo
        var parentPathList = arrayListOf<String>("/")
        var orderFlag = 0 // 0 为 name，1 为 ctime
        var defaultOrder = true
        var nameOrderBy = ""
        var ctimeOrderBy = ""
    }
}