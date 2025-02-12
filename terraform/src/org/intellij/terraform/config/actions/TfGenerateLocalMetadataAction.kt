// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls

internal class TfGenerateLocalMetadataAction : TfExternalToolsAction() {

  override suspend fun invoke(project: Project, @Nls title: String, vararg virtualFiles: VirtualFile) {
    val localSchemaService = project.serviceAsync<LocalSchemaService>()
    val anyFileInModuleDir = virtualFiles.firstOrNull()
    val lockFile = anyFileInModuleDir?.let { localSchemaService.findLockFile(it) }
    if (lockFile == null) {
      val moduleDir = anyFileInModuleDir?.let { if (anyFileInModuleDir.isDirectory) anyFileInModuleDir else anyFileInModuleDir.parent }
      TfConstants.getNotificationGroup()
        .createNotification(
          HCLBundle.message("notification.title.cant.generate.model"),
          HCLBundle.message("notification.content.there.no.terraform.lock.hcl.found.please.run.terraform.init", moduleDir?.name),
          NotificationType.ERROR
        ).addAction(InitFolderAction(moduleDir)).notify(project)
      return
    }
    localSchemaService.clearLocalModel(lockFile)
    localSchemaService.scheduleModelRebuild(setOf(lockFile), explicitlyAllowRunningProcess = true).getValue()
    TfConstants.getNotificationGroup()
      .createNotification(
        title,
        HCLBundle.message("notification.content.local.model.has.been.generated.successfully"),
        NotificationType.INFORMATION
      ).notify(project)
  }

  private class InitFolderAction(private val file: VirtualFile?): NotificationAction(HCLBundle.message("action.TFInitRequiredAction.text")) {
    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
      notification.expire()
      val tfInitAction = ActionManager.getInstance().getAction("TFInitRequiredAction")
      val dataContext = DataContext { dataId ->
        when (dataId) {
          CommonDataKeys.VIRTUAL_FILE.name -> { file }
          CommonDataKeys.PROJECT.name -> { e.project }
          else -> { null }
        }
      }
      ActionUtil.invokeAction(tfInitAction, e.withDataContext(dataContext), null)
    }
  }

}