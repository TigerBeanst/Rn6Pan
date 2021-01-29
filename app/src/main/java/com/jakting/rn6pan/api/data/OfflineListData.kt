package com.jakting.rn6pan.api.data

data class OfflineList(
    var dataList: List<OfflineFile>
)

data class OfflineFile(
    var taskIdentity: String,
    var userIdentity: Int,
    var createTime: Int,
    var name: String,
    var type: Int,
    var status: Int,
    var size: Int,
    var processedSize: Int,
    var progress: Int,
    var errorCode: Int,
    var errorMessage: String,
    var savePath: String,
    var saveIdentity: String,
    var accessPath: String,
    var accessIdentity: String,
    var fileMime: String,
    var fileType: Int,
    var createAddress: String,
    var data: String,
    var textLink: String,
    var fileHash: String,
    var op: Int,
    var username: String,
    var password: String,
    var kind: Int,
    var addon: String
)