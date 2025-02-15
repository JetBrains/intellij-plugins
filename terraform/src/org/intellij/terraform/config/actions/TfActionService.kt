// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.*
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.config.util.TfExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.runtime.ToolPathDetector
import org.intellij.terraform.runtime.showIncorrectPathNotification
import org.jetbrains.annotations.Nls
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
internal class TfActionService(private val project: Project, private val coroutineScope: CoroutineScope) {

  fun scheduleTerraformInit(directory: String, notifyOnSuccess: Boolean): Job {
    return coroutineScope.launch {
      val title = HCLBundle.message("progress.title.terraform.init", directory)
      val dirFile = LocalFileSystem.getInstance().findFileByNioFile(Path(directory))
      if (dirFile == null || !dirFile.isDirectory) {
        TfConstants.getNotificationGroup().createNotification(
            title,
            HCLBundle.message("notification.content.cannot.find.directory", directory),
            NotificationType.ERROR
          ).notify(project)
        return@launch
      }
      initTerraform(dirFile, notifyOnSuccess)
    }
  }

  fun scheduleTerraformInit(dirFile: VirtualFile, notifyOnSuccess: Boolean): Job {
    return coroutineScope.launch {
      initTerraform(dirFile, notifyOnSuccess)
    }
  }

  suspend fun initTerraform(dirFile: VirtualFile, notifyOnSuccess: Boolean) {
    val title = HCLBundle.message("progress.title.terraform.init", dirFile.name)
    val toolType = getApplicableToolType(dirFile)
    if (!ToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, false)) {
      showIncorrectPathNotification(project, toolType)
      return
    }

    withBackgroundProgress(project, title) {
      if (!execTerraformInit(dirFile, project, title)) {
        TfConstants.getNotificationGroup()
          .createNotification(
            title,
            HCLBundle.message("notification.content.terraform.init.failed", toolType.executableName, dirFile.name),
            NotificationType.WARNING
          ).notify(project)
        return@withBackgroundProgress
      }
      try {
        val localSchemaService = project.serviceAsync<LocalSchemaService>()
        localSchemaService.scheduleModelRebuild(setOf(dirFile), explicitlyAllowRunningProcess = true).let { result ->
          coroutineScope.launch {
            try {
              result.getValue()
            }
            catch (e: CancellationException) {
              throw e
            }
            catch (e: Exception) {
              notifyError(title, project, e)
            }
          }
        }
        if (notifyOnSuccess) {
          localSchemaService.awaitModelsReady()
          TfConstants.getNotificationGroup()
            .createNotification(
              title,
              HCLBundle.message("notification.content.terraform.init.succeed", toolType.executableName, dirFile.name),
              NotificationType.INFORMATION
            ).notify(project)
        }
      }
      catch (e: Exception) {
        if (e is CancellationException) throw e
        notifyError(title, project, e)
      }
    }
  }

  private suspend fun execTerraformInit(virtualFile: VirtualFile, project: Project, title: @Nls String): Boolean {
    withContext(Dispatchers.EDT) {
      writeIntentReadAction {
        FileDocumentManager.getInstance().saveAllDocuments()
      }
    }
    val directory = if (virtualFile.isDirectory) virtualFile else virtualFile.parent
    val success = TfExecutor.`in`(project, getApplicableToolType(directory))
      .withPresentableName(title)
      .withParameters("init")
      .showOutputOnError()
      .withWorkDirectory(directory.canonicalPath)
      .executeSuspendable()
    VfsUtil.markDirtyAndRefresh(false, true, true, directory)
    return success
  }

}