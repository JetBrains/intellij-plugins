// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.eel.EelProcess
import com.intellij.platform.eel.execute
import com.intellij.platform.eel.getOrThrow
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.readWholeText
import com.intellij.platform.eel.provider.utils.sendWholeText
import com.intellij.psi.PsiFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.Constants.TF_FMT
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
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
      private var job: Job? = null
      private var eelProcess: EelProcess? = null

      override fun run() {
        configureTfPath(project, toolType)
        val exePath = toolType.getToolSettings(project).toolPath

        runBlockingCancellable {
          job = launch {
            try {
              withContext(Dispatchers.IO) {
                val eelApi = project.getEelDescriptor().upgrade()
                eelProcess = eelApi.exec.execute(exePath).args("fmt", "-").getOrThrow()
                val process = eelProcess ?: throw ProcessCanceledException()

                process.stdin.sendWholeText(request.documentText).getOrThrow()
                process.stdin.close()

                val formattedText = process.stdout.readWholeText().getOrThrow()
                val exitCode = process.exitCode.await()

                if (exitCode == 0) {
                  request.onTextReady(formattedText)
                }
                else {
                  @NlsSafe val errorMessage = process.stderr.readWholeText().getOrThrow()
                  request.onError(HCLBundle.message("terraform.formatter.error.title", toolType.executableName), errorMessage)
                }
              }
            }
            catch (e: Exception) {
              request.onError(HCLBundle.message("terraform.formatter.error.title", toolType.executableName),
                              HCLBundle.message("terraform.formatter.error.message", virtualFile.name, toolType.executableName))
              throw e
            }
          }
        }
      }

      override fun cancel(): Boolean {
        job?.cancel()
        eelProcess?.let { killProcess(it) }
        return true
      }

      override fun isRunUnderProgress(): Boolean = true
    }
  }

  private fun configureTfPath(project: Project, toolType: TfToolType) {
    val isToolConfigured = runBlockingCancellable {
      TfToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, false)
    }

    if (!isToolConfigured) {
      showIncorrectPathNotification(project, toolType)
      throw ProcessCanceledException()
    }
  }

  private fun killProcess(process: EelProcess) {
    ApplicationManager.getApplication().executeOnPooledThread {
      runBlockingMaybeCancellable {
        withContext(Dispatchers.IO) {
          process.kill()
        }
      }
    }
  }
}

private const val TestsExtension = "tftest.hcl"