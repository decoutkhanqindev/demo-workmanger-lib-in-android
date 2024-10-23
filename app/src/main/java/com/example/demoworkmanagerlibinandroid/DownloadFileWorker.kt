package com.example.demoworkmanagerlibinandroid

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadFileWorker(appContext: Context, params: WorkerParameters) :
// or Worker(appContext, params)
  CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    val url: String = requireNotNull(inputData.getString(KEY_FILE_URL)) {
      "File URL is required. Please set KEY_FILE_URL in data"
    }
    
    Log.d("DownloadFileWorker", "doWork: Start downloading file ")
    
    FakeDownloadFile.downloadFile(url = url).collect { progress ->
      setProgress(workDataOf(KEY_PROGRESS to progress))
    }
    
    Log.d("DownloadFileWorker", "doWork: Finish downloading file ")
    
    Result.success(
      workDataOf(
        // output data
        KEY_FILE_URL to url,
        KEY_LOCAL_URL to "file:///path/to/downloaded/file"
      )
    )
  }
  
  companion object {
    const val KEY_FILE_URL = "KEY_FILE_URL"
    const val KEY_PROGRESS = "KEY_PROGRESS"
    const val KEY_LOCAL_URL = "KEY_LOCAL_URL"
    const val WORK_TAG = "DownloadFileWorker"
  }
}