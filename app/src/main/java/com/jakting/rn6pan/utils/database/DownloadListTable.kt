package com.jakting.rn6pan.utils.database

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport


class DownloadListTable : LitePalSupport() {
    @Column(unique = true, defaultValue = "unknown")
    var fileIdentity: String? = null

    @Column(unique = true, defaultValue = "unknown")
    var fileTaskId: Long = 0

    var fileName: String? = null

    var filePath: String? = null
}


