package com.jakting.rn6pan.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import okhttp3.Cookie

class CookiesUtils {
    companion object{
        fun encodeCookie(cookies: List<Cookie>): String {
            val sb = StringBuilder()
            for (cookie in cookies) {
                //将Cookie转换成字符串
                sb.append(cookie.toString())
                //以#为分隔符
                sb.append("; ")
            }
            sb.deleteCharAt(sb.lastIndexOf("; "))
            return sb.toString()
        }

        fun loadCookie(host: String): String {
            val sp: SharedPreferences = MyApplication.appContext
                .getSharedPreferences("config_cookies", Context.MODE_PRIVATE)
            return if (!TextUtils.isEmpty(host) && sp.contains(host)) {
                sp.getString(host, "")!!
            } else {
                ""
            }
        }

        fun saveCookie(host: String, cookie: String) {
            val editor: SharedPreferences.Editor = MyApplication.appContext
                .getSharedPreferences("config_cookies", Context.MODE_PRIVATE)
                .edit()
            if (!TextUtils.isEmpty(host)) {
                editor.putString(host, cookie)
            }
            editor.apply()
        }
    }
}