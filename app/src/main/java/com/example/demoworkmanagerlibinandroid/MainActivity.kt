package com.example.demoworkmanagerlibinandroid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.example.demoworkmanagerlibinandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
  private val binding: ActivityMainBinding by lazy(LazyThreadSafetyMode.NONE) {
    ActivityMainBinding.inflate(layoutInflater)
  }
  
  private val workManager: WorkManager by lazy { WorkManager.getInstance(this) }
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    
    binding.enqueueWorkBtn.setOnClickListener {
      enqueueWork()
    }
    
    binding.cancelWorkBtn.setOnClickListener {
      cancelWork()
    }
    
    observeWork()
  }
  
  private fun observeWork() { // observe work states
    val workInfos: Flow<MutableList<WorkInfo>> =
      workManager.getWorkInfosByTagFlow(DownloadFileWorker.WORK_TAG)
    
    val separator: String = "\n" + "-".repeat(75) + "\n"
    
    lifecycleScope.launch {
      workInfos.collect { workInfos: MutableList<WorkInfo> ->
        binding.workProgressText.text = workInfos.joinToString(separator) { workInfo: WorkInfo ->
          "Work ID: ${workInfo.id}, \n" +
          "Work State: ${workInfo.state}, \n" +
          "Work Progress: ${workInfo.progress}, \n" +
          "Work Output Data:${workInfo.outputData}"
        }
      }
    }
  }
  
  private fun enqueueWork() {
    // If constraints are not satisfied, the work will not run
    val constraint: Constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresCharging(false)
      .setRequiresBatteryNotLow(true)
      .setRequiresStorageNotLow(true)
      .build()
    
    // Define work requests
    val workRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<DownloadFileWorker>()
      .setInitialDelay(5, TimeUnit.SECONDS)
      .addTag(DownloadFileWorker.WORK_TAG)
      .setInputData(workDataOf(DownloadFileWorker.KEY_FILE_URL to "https://example.com/file.zip"))
      .setConstraints(constraint)
      .build()
    
    // Submit the WorkRequest to the system
    lifecycleScope.launch {
      try {
        workManager.enqueue(workRequest).await() // await() to know SUCCESS or FAIL
        Log.d("MainActivity", "enqueueWork: Work enqueued")
      } catch (cancel: CancellationException) {
        throw cancel
      } catch (e: Exception) {
        Log.d("MainActivity", "enqueueWork: ${e.message}")
      }
    }
    
    // or

//    workManager.enqueueUniqueWork(
//      /* uniqueWorkName = */ "DownloadFileUniqueWork",
//      /* existingWorkPolicy = */ ExistingWorkPolicy.REPLACE,
//      /* work = */ workRequest
//    )
    
    // or

//    workManager.enqueueUniquePeriodicWork(
//      /* uniqueWorkName = */ "DownloadFilePeriodicWork",
//      /* existingPeriodicWorkPolicy = */ ExistingWorkPolicy.REPLACE,
//      /* periodicWork = */ PeriodicWorkRequest.Builder(DownloadFileWorker::class.java, 15, TimeUnit.MINUTES)
//    )
  }
  
  private fun cancelWork() {
    lifecycleScope.launch {
      try {
        workManager.cancelAllWorkByTag(DownloadFileWorker.WORK_TAG)
          .await() // await() to know SUCCESS or FAIL
        Log.d("MainActivity", "cancelWork: Work canceled")
      } catch (cancel: CancellationException) {
        throw cancel
      } catch (e: Exception) {
        Log.d("MainActivity", "cancelWork: ${e.message}")
      }
    }
  }
}