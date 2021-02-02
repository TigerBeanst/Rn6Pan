package com.jakting.rn6pan.api.data

data class AppUpdateData(
    var versionCode: Int,
    var versionName: String,
    var category: String,
    var changelog: String,
    var release: String,
    var originLink: String,
    var mirrorLink: String
)