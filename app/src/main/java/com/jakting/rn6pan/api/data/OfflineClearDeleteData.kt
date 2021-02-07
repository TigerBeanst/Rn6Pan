package com.jakting.rn6pan.api.data

data class OfflineClear(
    var type: Int,
    var deleteFile: Boolean
)

data class OfflineDelete(
    var taskIdentity: String,
    var deleteFile: Boolean
)

data class OfflineClearDeleteReturn(
    var successCount: Int
)

