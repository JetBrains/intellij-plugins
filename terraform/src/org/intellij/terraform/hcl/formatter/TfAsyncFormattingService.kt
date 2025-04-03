// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.platform.eel.execute
import com.intellij.platform.eel.getOrThrow
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.readWholeText
import com.intellij.platform.eel.provider.utils.sendWholeText
import com.intellij.psi.PsiFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.Constants.TF_FMT
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.runtime.TfToolPathDetector
import org.intellij.terraform.runtime.showIncorrectPathNotification

internal class TfAsyncFormattingService : AsyncDocumentFormattingService() {
  override fun getName(): String = TF_FMT

  override fun getNotificationGroupId(): String = TfConstants.getNotificationGroup().displayId

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

    return object : FormattingTask {
      override fun run() {
        try {
          val isToolConfigured = runBlockingCancellable {
            TfToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, false)
          }

          if (!isToolConfigured) {
            showIncorrectPathNotification(project, toolType)
            throw ProcessCanceledException()
          }

          runBlockingCancellable {
            withContext(Dispatchers.IO) {
              val exePath = toolType.getToolSettings(project).toolPath

              val eelApi = project.getEelDescriptor().upgrade()
              val process = eelApi.exec.execute(exePath).args("fmt", "-").getOrThrow()

              process.stdin.sendWholeText(request.documentText).getOrThrow()
              process.stdin.close()

              val formattedText = process.stdout.readWholeText().getOrThrow()
              val exitCode = process.exitCode.await()

              if (exitCode == 0) {
                request.onTextReady(formattedText)
              }
              else {
                request.onError(HCLBundle.message("terraform.formatter.error.title", toolType.executableName), process.stderr.readWholeText().getOrThrow())
              }
            }
          }
        }
        catch (e: ProcessCanceledException) {
          throw e
        }
        catch (e: Exception) {
          logger<TfAsyncFormattingService>().warn("Failed to run FormattingTask", e)
          request.onError(HCLBundle.message("terraform.formatter.error.title", toolType.executableName),
                          HCLBundle.message("terraform.formatter.error.message", virtualFile.name, toolType.executableName))
        }
      }

      override fun cancel(): Boolean {
        return true
      }

      override fun isRunUnderProgress(): Boolean = true
    }
  }
}

private const val TestsExtension = "tftest.hcl"