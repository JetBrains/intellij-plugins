// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.util

import com.intellij.execution.ExecutionModes
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.intellij.terraform.install.TFToolType
import org.intellij.terraform.opentofu.OpenTofuFileType

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

internal fun getApplicableToolType(project: Project, file: VirtualFile): TFToolType {
  val moduleFolder = if (file.isFile) file.parent else file
  val tool = if (moduleFolder.children.any { child -> child.extension == OpenTofuFileType.DEFAULT_EXTENSION })
    TFToolType.OPENTOFU
  else
    TFToolType.TERRAFORM
  return tool
}
