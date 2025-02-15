// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.config.util.TfExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls
import kotlin.io.path.Path

internal class TfFmtFileAction : TfExternalToolsAction() {

  override suspend fun invoke(project: Project, title: @Nls String, vararg virtualFiles: VirtualFile) {
    withBackgroundProgress(project, title) {
      withContext(Dispatchers.EDT) {
        writeIntentReadAction {
          FileDocumentManager.getInstance().saveAllDocuments()
        }
      }

      if (virtualFiles.isEmpty()) return@withBackgroundProgress

      val filePaths = virtualFiles.map { it.toNioPath().asEelPath().toString() }.toTypedArray()
      val toolType = getApplicableToolType(virtualFiles.first())
      TfExecutor.`in`(project, toolType)
        .withPresentableName(title)
        .withParameters("fmt", *filePaths)
        .showOutputOnError()
        .executeSuspendable()
      VfsUtil.markDirtyAndRefresh(true, true, true, *virtualFiles)
      TfConstants.getNotificationGroup()
        .createNotification(
          title,
          HCLBundle.message("notification.action.TfFmtFileAction.finished",
                            toolType.executableName,
                            filePaths.map { Path(it).fileName }.joinToString(" ")),
          NotificationType.INFORMATION
        ).notify(project)
    }
  }
}
