package com.jakting.rn6pan.utils

import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.HttpException
import java.math.BigDecimal

val NUDE_URL = "2dland.cn"
val API_NUDE_URL = "api.$NUDE_URL"
val ACCOUNT_NUDE_URL = "account.$NUDE_URL"
val API_BASE_URL = "https://$API_NUDE_URL/v3/"
val API_LOGIN_FILE_LIST_URL = "https://$API_NUDE_URL/v3/user/oauthQueryLogin"

val INTENT_ACTIVITY_LOGIN_CODE = 1

fun logd(message: String) =
    Log.d("hjt", message)

fun toast(message: CharSequence) =
    Toast.makeText(MyApplication.appContext, message, Toast.LENGTH_SHORT).show()

fun longtoast(message: CharSequence) =
    Toast.makeText(MyApplication.appContext, message, Toast.LENGTH_LONG).show()

fun View.sbar(message: CharSequence) =
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)

fun View.sbarlong(message: CharSequence) =
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)

fun View.sbarin(message: CharSequence) =
    Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE)

fun getPrintSize(size: Long): String? {
    // 如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
    var value = size.toDouble()
    value = if (value < 1024) {
        return value.toString() + "B"
    } else {
        BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).toDouble()
    }
    // 如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
    // 因为还没有到达要使用另一个单位的时候
    // 接下去以此类推
    value = if (value < 1024) {
        return value.toString() + "KB"
    } else {
        BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).toDouble()
    }

    value = if (value < 1024) {
        return value.toString() + "MB"
    } else {
        BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).toDouble()
    }

    return if (value < 1024) {
        value.toString() + "GB"
    } else {
        value = BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).toDouble()
        value.toString() + "TB"
    }
}

fun getErrorString(t: Throwable): String {
    return (t as HttpException).response()?.errorBody()?.string().let { return@let it.toString() }
}

fun isStringIllegal(string: String): Boolean { //6盘专用
    return (string.trim().isEmpty() || string.contains(":"))
}

fun getPostBody(jsonForPost: String): RequestBody {
    return RequestBody.create(
        MediaType.parse("application/json"), jsonForPost
    )
}

