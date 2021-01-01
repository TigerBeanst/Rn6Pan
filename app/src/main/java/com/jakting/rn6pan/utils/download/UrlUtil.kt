package com.jakting.rn6pan.utils.download

import android.net.Uri

fun getNameFromUrl(url:String):String{
    return Uri.parse(url).lastPathSegment!!
}