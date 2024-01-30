// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.intellij.terraform.config.TerraformConstants
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
internal class TerraformActionService(private val project: Project, private val coroutineScope: CoroutineScope) {

  fun scheduleTerraformInit(directory: String): Job {

    return coroutineScope.launch {
      val title = HCLBundle.message("progress.title.terraform.init")
      val dirFile = LocalFileSystem.getInstance().findFileByNioFile(Path(directory))
      if (dirFile == null || !dirFile.isDirectory) {
        TerraformConstants.EXECUTION_NOTIFICATION_GROUP
          .createNotification(
            title,
            HCLBundle.message("notification.content.cannot.find.directory", directory),
            NotificationType.ERROR
          ).notify(project)
        return@launch
      }
      val success = withBackgroundProgress(project, title) {
        runTerraformInit(dirFile, project, module = null, title)
      }
      if (success) {
        TerraformConstants.EXECUTION_NOTIFICATION_GROUP
          .createNotification(
            title,
            HCLBundle.message("notification.content.terraform.init.succeed"),
            NotificationType.INFORMATION
          ).notify(project)
      }
      else {
        TerraformConstants.EXECUTION_NOTIFICATION_GROUP
          .createNotification(
            title,
            HCLBundle.message("notification.content.terraform.init.failed"),
            NotificationType.WARNING
          ).notify(project)
      }
    }
  }

  suspend fun runTerraformInit(virtualFile: VirtualFile,
                               project: Project,
                               module: Module?,
                               title: @Nls String): Boolean {
    val directory = if (virtualFile.isDirectory) virtualFile else virtualFile.parent
    val success = TFExecutor.`in`(project, module)
      .withPresentableName(title)
      .withParameters("init")
      .showOutputOnError()
      .withWorkDirectory(directory.canonicalPath)
      .executeSuspendable()
    VfsUtil.markDirtyAndRefresh(true, true, true, directory)
    return success
  }

}