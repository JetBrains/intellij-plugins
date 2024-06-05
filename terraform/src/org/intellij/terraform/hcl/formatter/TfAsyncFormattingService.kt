// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.Constants.TF_FMT
import org.intellij.terraform.config.Constants.isTerraformFmtEnabled
import org.intellij.terraform.config.TerraformConstants.EXECUTION_NOTIFICATION_GROUP
import org.intellij.terraform.config.actions.isTerraformExecutable
import org.intellij.terraform.config.isTerraformFile
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLFile
import java.nio.charset.StandardCharsets

class TfAsyncFormattingService : AsyncDocumentFormattingService() {
  override fun getName(): String = TF_FMT

  override fun getNotificationGroupId(): String = EXECUTION_NOTIFICATION_GROUP.displayId

  override fun getFeatures(): Set<FormattingService.Feature> = emptySet()

  override fun canFormat(file: PsiFile): Boolean = file is HCLFile && isTerraformFmtEnabled

  override fun createFormattingTask(formattingRequest: AsyncFormattingRequest): FormattingTask? {
    val context = formattingRequest.context
    val project = context.project
    if (!isTerraformExecutable(project)) {
      return null
    }

    val virtualFile = context.virtualFile ?: return null
    if (!virtualFile.extension.isTerraformFile()) {
      EXECUTION_NOTIFICATION_GROUP.createNotification(
        HCLBundle.message("terraform.formatter.error.title"),
        HCLBundle.message("terraform.formatter.file.extension.error"),
        NotificationType.ERROR).notify(project)

      return null
    }

    val filePath = virtualFile.canonicalPath ?: return null
    val commandLine = createCommandLine(project, filePath)
    val handler = OSProcessHandler(commandLine.withCharset(StandardCharsets.UTF_8))

    return object : FormattingTask {
      override fun run() {
        handler.addProcessListener(object : CapturingProcessAdapter() {
          override fun processTerminated(event: ProcessEvent) {
            val exitCode = event.exitCode
            if (exitCode == 0) {
              VfsUtil.markDirtyAndRefresh(true, true, true, virtualFile)
              formattingRequest.onTextReady(null)
            }
            else {
              formattingRequest.onError(HCLBundle.message("terraform.formatter.error.title"), output.stderr)
            }
          }
        })

        handler.startNotify()
      }

      override fun cancel(): Boolean {
        handler.destroyProcess()
        return true
      }

      override fun isRunUnderProgress(): Boolean = true
    }
  }

  private fun createCommandLine(project: Project, filePath: String): GeneralCommandLine =
    TFExecutor.`in`(project)
      .withPresentableName(TF_FMT)
      .withParameters("fmt", filePath)
      .createCommandLine()
}