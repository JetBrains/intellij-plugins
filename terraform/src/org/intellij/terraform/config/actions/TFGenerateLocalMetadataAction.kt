// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.terraform.config.TerraformConstants
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.jetbrains.annotations.Nls

internal class TFGenerateLocalMetadataAction : TFExternalToolsAction() {

  override suspend fun invoke(project: Project, @Nls title: String, vararg virtualFiles: VirtualFile) {
    val localSchemaService = project.serviceAsync<LocalSchemaService>()
    val lockFile = virtualFiles.firstOrNull()?.let { localSchemaService.findLockFile(it) }
    if (lockFile == null) {
      val toolType = virtualFiles.firstOrNull()?.let { getApplicableToolType(it) }?.executableName
                     ?: TfToolType.TERRAFORM.executableName
      TerraformConstants.EXECUTION_NOTIFICATION_GROUP
        .createNotification(
          HCLBundle.message("notification.title.cant.generate.model"),
          HCLBundle.message("notification.content.there.no.terraform.lock.hcl.found.please.run.terraform.init", toolType),
          NotificationType.ERROR
        ).notify(project)
      return
    }
    localSchemaService.clearLocalModel(lockFile)
    localSchemaService.scheduleModelRebuild(setOf(lockFile), explicitlyAllowRunningProcess = true).getValue()
    TerraformConstants.EXECUTION_NOTIFICATION_GROUP
      .createNotification(
        title,
        HCLBundle.message("notification.content.local.model.has.been.generated.successfully"),
        NotificationType.INFORMATION
      ).notify(project)
  }

}