// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.Constants.TF_FMT
import org.intellij.terraform.config.TerraformConstants.EXECUTION_NOTIFICATION_GROUP
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.actions.isExecutableToolFileConfigured
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.runtime.ToolPathDetector

internal class TfAsyncFormattingService : AsyncDocumentFormattingService() {
  override fun getName(): String = TF_FMT

  override fun getNotificationGroupId(): String = EXECUTION_NOTIFICATION_GROUP.displayId

  override fun getFeatures(): Set<FormattingService.Feature> = emptySet()

  override fun canFormat(file: PsiFile): Boolean {
    val settings = CodeStyle.getCustomSettings(file, HclCodeStyleSettings::class.java)
    return (file.fileType is TerraformFileType || file.name.endsWith(TestsExtension)) && settings.RUN_TF_FMT_ON_REFORMAT
  }

  override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
    val context = request.context
    val project = context.project
    val virtualFile = context.virtualFile ?: return null
    val toolType = getApplicableToolType(virtualFile)
    ToolPathDetector.getInstance(project).detectPathAndUpdateSettingsIfEmpty(toolType)
    if (!isExecutableToolFileConfigured(project, toolType)) {
      return null
    }

    val commandLine = createCommandLine(project, toolType)

    return object : FormattingTask {
      private var processHandler: CapturingProcessHandler? = null

      override fun run() {
        try {
          processHandler = CapturingProcessHandler(commandLine)

          val handler = processHandler ?: return
          handler.processInput.write(request.documentText.toByteArray())
          handler.processInput.close()

          val output = handler.runProcess()
          if (output.exitCode == 0) {
            request.onTextReady(output.stdout)
          }
          else {
            request.onError(HCLBundle.message("terraform.formatter.error.title", toolType.executableName), output.stderr)
          }
        }
        catch (e: Exception) {
          logger<TfAsyncFormattingService>().warn("Failed to run FormattingTask", e)
          request.onError(HCLBundle.message("terraform.formatter.error.title", toolType.executableName),
                          HCLBundle.message("terraform.formatter.error.message", virtualFile.name, toolType.executableName))
        }
      }

      override fun cancel(): Boolean {
        processHandler?.destroyProcess()
        return true
      }

      override fun isRunUnderProgress(): Boolean = true
    }
  }

  private fun createCommandLine(project: Project, applicableToolType: TfToolType): GeneralCommandLine =
    TFExecutor.`in`(project, applicableToolType)
      .withPresentableName(HCLBundle.message("tool.format.display", applicableToolType.displayName))
      .withParameters("fmt", "-")
      .createCommandLine()
}

private const val TestsExtension = "tftest.hcl"