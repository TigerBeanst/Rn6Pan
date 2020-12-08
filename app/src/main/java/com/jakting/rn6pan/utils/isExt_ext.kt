package com.jakting.rn6pan.utils

fun isExtVideo(ext: String): Boolean {
    return ext.contains(".mp4",true)
            || ext.contains(".mpg",true)
            || ext.contains(".mov",true)
            || ext.contains(".rm",true)
            || ext.contains(".wmv",true)
            || ext.contains(".flv",true)
            || ext.contains(".3gp",true)
            || ext.contains(".mpeg",true)
            || ext.contains(".mkv",true)
            || ext.contains(".rmvb",true)
            || ext.contains(".ts",true)
            || ext.contains(".webm",true)
            || ext.contains(".avi",true)
            || ext.contains(".f4v",true)
}

fun isExtImage(ext: String): Boolean {
    return ext.contains(".bmp",true)
            || ext.contains(".gif",true)
            || ext.contains(".jpg",true)
            || ext.contains(".jpeg",true)
            || ext.contains(".png",true)
            || ext.contains(".psd",true)
            || ext.contains(".webp",true)
}

fun isExtCompressed(ext: String): Boolean {
    return ext.contains(".zip",true)
            || ext.contains(".rar",true)
            || ext.contains(".7z",true)
            || ext.contains(".gz",true)
            || ext.contains(".gzip",true)
            || ext.contains(".package",true)
}

fun isExtAndroid(ext: String): Boolean {
    return ext.contains(".apk",true)
            || ext.contains(".xapk",true)
}
