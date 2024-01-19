// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.util

import com.intellij.execution.ExecutionModes
import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

suspend fun TFExecutor.executeSuspendable(): Boolean {
  return withContext(Dispatchers.IO) {
    try {
      runInterruptible {
        execute(ExecutionModes.SameThreadMode().apply {
          setShouldCancelFun { Thread.interrupted() }
        })
      }
    }
    catch (e: Throwable) {
      if (e is ProcessCanceledException) throw CancellationException(e.message, e)
      throw e
    }
  }
}