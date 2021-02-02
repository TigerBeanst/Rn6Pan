package com.jakting.rn6pan.api.data

data class OfflineParse(
    var hash: String,
    var info: OfflineParseInfo
)

data class OfflineParseInfo(
    var textLink: String,
    var fileHash: String,
    var username: String,
    var password: String,
    var identity: String,
    var name: String,
    var size: Long,
    var dataList: List<*>,
    var type: Int
)

data class OfflineAdd(
    var task: MutableList<OfflineAddTaskInfo>,
    var savePath: String
)

data class OfflineAddTaskInfo(
    var hash: String
)

data class OfflineAddResponse(
    var successCount: Int
)


data class OfflineParseError(
    var success:Boolean,
    var status:Int,
    var reference:String,
    var message:String
)