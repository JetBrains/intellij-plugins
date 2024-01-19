// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.intellij.terraform.config.TerraformConstants
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.hcl.HCLBundle

class TFGenerateLocalMetadataAction : AnAction() {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
    if (project == null || file == null || !TFExternalToolsAction.isAvailableOnFile(file, true)) {
      e.presentation.isEnabled = false
      return
    }
    e.presentation.isEnabled = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    project.service<CoroutineScopeProvider>().coroutineScope.launch {
      val localSchemaService = project.service<LocalSchemaService>()
      val lockFile = localSchemaService.findLockFile(file)
      if (lockFile == null) {
        TerraformConstants.EXECUTION_NOTIFICATION_GROUP
          .createNotification(
            HCLBundle.message("notification.title.cant.generate.model"),
            HCLBundle.message("notification.content.there.no.terraform.lock.hcl.found.please.run.terraform.init"),
            NotificationType.ERROR
          ).notify(project)
        return@launch
      }
      try {
        localSchemaService.clearLocalModel(lockFile)
        localSchemaService.scheduleModelRebuild(setOf(lockFile)).await()
      }
      catch (e: Exception) {
        if (e is CancellationException) throw e
        TerraformConstants.EXECUTION_NOTIFICATION_GROUP
          .createNotification(
            HCLBundle.message("notification.title.cant.generate.model"),
            @Suppress("HardCodedStringLiteral")
            generateSequence<Throwable>(e) { it.cause }
              .mapNotNull { it.message }
              .distinct()
              .joinToString("\n"),
            NotificationType.ERROR
          ).notify(project)
      }
    }
  }

  @Service(Service.Level.PROJECT)
  private class CoroutineScopeProvider(val coroutineScope: CoroutineScope)

}