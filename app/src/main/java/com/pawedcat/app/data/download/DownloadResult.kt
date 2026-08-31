package com.pawedcat.app.data.download

sealed class DownloadResult {
    data class Success(val filePath: String, val bytesDownloaded: Long) : DownloadResult()
    data class Error(val message: String, val throwable: Throwable? = null) : DownloadResult()
    object Cancelled : DownloadResult()
}
