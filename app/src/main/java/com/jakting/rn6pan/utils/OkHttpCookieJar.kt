package com.jakting.rn6pan.utils

import android.text.TextUtils
import com.jakting.rn6pan.utils.CookiesUtils.Companion.encodeCookie
import com.jakting.rn6pan.utils.CookiesUtils.Companion.loadCookie
import com.jakting.rn6pan.utils.CookiesUtils.Companion.saveCookie
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl


class OkHttpCookieJar : CookieJar {

    //https://wenhaiz.xyz/cookie-persistence-in-okhttp

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookieStr: String = encodeCookie(cookies)
        saveCookie(url.host(), cookieStr)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies: MutableList<Cookie> = ArrayList()
        val cookieStr: String = loadCookie(url.host())
        if (!TextUtils.isEmpty(cookieStr)) {
            //获取所有 Cookie 字符串
            val cookieStrs = cookieStr.split("; ").toTypedArray()
            for (aCookieStr in cookieStrs) {
                //将字符串解析成 Cookie 对象
                val cookie = Cookie.parse(url, aCookieStr)
                cookies.add(cookie!!)
            }
        }
        //此方法返回 null 会引发异常
        return cookies
    }
}