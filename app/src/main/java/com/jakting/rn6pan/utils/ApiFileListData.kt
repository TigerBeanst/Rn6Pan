package com.jakting.rn6pan.utils

data class FileOrDirectoryList(
    var parent: FileOrDirectory,
    var dataList: List<FileOrDirectory>
)

data class FileOrDirectory(
    var identity: String,
    var hash: String,
    var userIdentity: Long,
    var path: String,
    var name: String,
    var ext: String,
    var size: Long,
    var mime: String,
    var deleted: Boolean,
    var hidden: Boolean,
    var label: Int,
    var parent: String,
    var type: Int,
    var directory: Boolean,
    var atime: Long,
    var ctime: Long,
    var mtime: Long,
    var version: Int,
    var locking: Boolean,
    var op: String,
    var preview: Boolean,
    var previewType: Int,
    var flag: Int,
    var uniqueIdentity: String,
    var share: Boolean,
    var downloadAddress: String,
    var lockTime: Long,
    var children: Int,
    var childrenTotal: Int,
    var sticky: Int
)