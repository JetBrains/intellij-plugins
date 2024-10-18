// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.util

import com.intellij.execution.ExecutionModes
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.*
import org.intellij.terraform.install.TFToolType
import org.intellij.terraform.opentofu.OpenTofuFileType
import org.intellij.terraform.runtime.TerraformProjectSettings

@Service(Service.Level.PROJECT)
internal class TFExecutorService(val project: Project, val coroutineScope: CoroutineScope) {
  fun executeInBackground(executor: TFExecutor) {
    coroutineScope.launch {
      withBackgroundProgress(project, executor.presentableName) {
        executor.executeSuspendable()
        executor.workDirectory?.let {
          val workDirectoryFile = service<VirtualFileManager>().findFileByUrl("file://$it")
          workDirectoryFile?.let { RefreshQueue.getInstance().refresh(true, true, null, workDirectoryFile) }
        }
      }
    }
  }
}

internal suspend fun TFExecutor.executeSuspendable(): Boolean {
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
  val tool = if (moduleFolder.children.any { FileUtilRt.extensionEquals(it.name, OpenTofuFileType.DEFAULT_EXTENSION) })
    TFToolType.OPENTOFU
  else {
    if (project.service<TerraformProjectSettings>().toolPath.isBlank()) {
      logger<TFExecutor>().warn("Tool path is not set for ${TFToolType.TERRAFORM.displayName}, using ${TFToolType.OPENTOFU.displayName}")
      TFToolType.OPENTOFU
    }
    else {
      TFToolType.TERRAFORM
    }
  }
  return tool
}
