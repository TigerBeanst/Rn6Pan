package com.jakting.rn6pan.activity.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.jakting.rn6pan.BaseActivity
import com.jakting.rn6pan.R
import com.jakting.rn6pan.utils.ACCOUNT_NUDE_URL
import com.jakting.rn6pan.utils.API_LOGIN_FILE_LIST_URL
import com.jakting.rn6pan.utils.MyApplication.Companion.COOKIES
import com.jakting.rn6pan.utils.MyApplication.Companion.DESTINATION
import kotlinx.android.synthetic.main.activity_user_login.*

class LoginActivity : BaseActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.login_toolbar_title)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        login_webview.settings.javaScriptEnabled = true
        login_webview.loadUrl("https://$ACCOUNT_NUDE_URL/login?destination=$DESTINATION&appid=bc088aa5e2ad&response=query&state=1234&lang=zh-CN")
        login_webview.webViewClient = MyWebViewClient()
    }

    inner class MyWebViewClient:WebViewClient(){
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if(url.contains(API_LOGIN_FILE_LIST_URL)){
                COOKIES = CookieManager.getInstance().getCookie(url)
                intent.putExtra("isLoginSuccess", true)
                setResult(RESULT_OK, intent)
                finishAfterTransition()
            }
        }

    }
}