package com.jakting.rn6pan.api.data

import android.graphics.drawable.Drawable

data class FileLabel(
    var identity: Int,
    var userIdentity: Int,
    var name: String,
    var type: Int,
    var createTime: Long
)

data class FileLabelList(
    var dataList: List<FileLabel>
)

data class FileLabelItem(
    var text: String,
    var drawable: Drawable,
    var identity: Int
)

data class FileActionReturn(
    var identity: String,
    var data: Int,
    var async: Boolean
)