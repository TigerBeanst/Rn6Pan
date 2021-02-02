package com.jakting.rn6pan.utils

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_choose_path.*
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


/**
 * 获取 Toolbar 图标的 View
 * @param drawable Drawable?
 * @return View?
 */
fun Toolbar.getToolBarItemView(drawable: Drawable?): View? {
    val size: Int = this.childCount
//        logd("获取底部栏的详情：size为$size")
    for (i in 0 until size) {
        val child: View = this.getChildAt(i)
//            logd("获取底部栏的详情：view为$child")
        if (child is ImageButton) {
            if (child.drawable === drawable) {
//                    logd("获取底部栏的详情：drawable${child.drawable}")
                return child
            }
        }
    }
    return null
}
