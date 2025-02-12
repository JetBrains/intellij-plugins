// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.util

import com.intellij.execution.ExecutionModes
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.*
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.opentofu.OpenTofuFileType

@Service(Service.Level.PROJECT)
internal class TfExecutorService(val project: Project, val coroutineScope: CoroutineScope) {
  fun executeInBackground(executor: TfExecutor) {
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

internal suspend fun TfExecutor.executeSuspendable(): Boolean {
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

internal fun getApplicableToolType(file: VirtualFile): TfToolType {
  val moduleFolder = if (file.isFile) file.parent else file
  val moduleFiles = moduleFolder?.children?.asList()?.takeIf { it.isNotEmpty() } ?: listOf(file)
  return if (moduleFiles.any {it.isFile && FileUtilRt.extensionEquals(it.name, OpenTofuFileType.DEFAULT_EXTENSION) })
    TfToolType.OPENTOFU
  else {
    TfToolType.TERRAFORM
  }
}
