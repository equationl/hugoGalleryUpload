package com.equationl.hugo_gallery_uploader.util

import com.obs.services.ObsClient
import com.obs.services.model.ProgressStatus
import com.obs.services.model.PutObjectRequest
import java.io.File


object ObsUtil {
    var obsClient: ObsClient? = null

    fun initObsClient(
        ak: String,
        sk: String,
        endPoint: String,
        changeConfig: Boolean = false
    ): Result<Boolean> {
        try {
            if (obsClient != null) {
                if (changeConfig) {
                    obsClient?.close()
                }
                else {
                    return Result.success(true)
                }
            }
            obsClient = ObsClient(ak, sk, endPoint)
            return Result.success(true)
        } catch (tr: Throwable) {
            println("initObsClient error: ${tr.stackTraceToString()}")
            return Result.failure(tr)
        }
    }

    fun uploadFile(file: File, bucketName: String, obsSaveFolder: String, onProgressStatus: ((status: ProgressStatus) -> Unit)? = null): Result<Boolean> {
        if (obsClient == null) {
            println("obsClient == null!!")
            return Result.failure(Exception("obsClient == null!!"))
        }

        try {
            val request = PutObjectRequest()
            request.bucketName = bucketName
            request.objectKey = "$obsSaveFolder/${file.name}"
            request.file = file

            request.setProgressListener { status ->
                onProgressStatus(status)
            }

            obsClient!!.putObject(request)
            return Result.success(true)
        } catch (tr: Throwable) {
            println("uploadFile error: ${tr.stackTraceToString()}")
            return Result.failure(tr)
        }
    }


}