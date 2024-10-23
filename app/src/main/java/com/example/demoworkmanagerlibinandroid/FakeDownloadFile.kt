package com.example.demoworkmanagerlibinandroid

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

class FakeDownloadFile {
  companion object {
    fun downloadFile(url: String): Flow<Int> = (0..100).asFlow()
      .onStart { println(">>> $url stated") }
      .onCompletion { println(">>> $url ended") }
      .onEach { delay(1000) }
  }
}