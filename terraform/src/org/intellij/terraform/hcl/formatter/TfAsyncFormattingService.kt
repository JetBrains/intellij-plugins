// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.eel.EelProcess
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.toEelApi
import com.intellij.platform.eel.provider.utils.readWholeText
import com.intellij.platform.eel.provider.utils.sendWholeText
import com.intellij.platform.eel.spawnProcess
import com.intellij.psi.PsiFile
import com.intellij.util.AnsiCsiUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.Constants.TF_FMT
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.runtime.TfToolPathDetector
import java.util.concurrent.CancellationException
import kotlin.io.path.Path

internal class TfAsyncFormattingService : AsyncDocumentFormattingService() {
  override fun getName(): String = TF_FMT

  override fun getNotificationGroupId(): String = TfConstants.getNotificationGroup().displayId

  override fun getFeatures(): Set<FormattingService.Feature> = emptySet()

  override fun canFormat(file: PsiFile): Boolean {
    if (ApplicationManager.getApplication().isUnitTestMode) return false

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

      override fun run() {
        try {
          runBlockingCancellable {
            val isToolConfigured = TfToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, false)
            val exePath = toolType.getToolSettings(project).toolPath
            if (!isToolConfigured) {
              request.onError(
                HCLBundle.message("run.configuration.terraform.path.title", toolType.displayName),
                HCLBundle.message("run.configuration.terraform.path.incorrect", exePath.ifEmpty { toolType.executableName }, toolType.displayName)
              )
            }
            else {
              job = launch {
                var process: EelProcess? = null
                try {
                  withContext(Dispatchers.IO) {
                    val eelApi = project.getEelDescriptor().toEelApi()
                    val eelExePath = Path(exePath).asEelPath()
                    val envVariables = eelApi.exec.fetchLoginShellEnvVariables()
                    process = @Suppress("checkedExceptions") eelApi.exec.spawnProcess(eelExePath)
                      .args("fmt", "-")
                      .env(envVariables)
                      .eelIt()

                    process.stdin.sendWholeText(request.documentText)
                    process.stdin.close(null)

                    val formattedText = process.stdout.readWholeText()
                    val exitCode = process.exitCode.await()
                    if (exitCode == 0) {
                      request.onTextReady(formattedText)
                    }
                    else {
                      val errorMessage = process.stderr.readWholeText()
                      @NlsSafe val cleanedMessage = normalizeAnsiText(errorMessage)
                      request.onError(HCLBundle.message("terraform.formatter.error.title", toolType.executableName), cleanedMessage)
                    }
                  }
                }
                finally {
                  withContext(NonCancellable) {
                    process?.kill()
                  }
                }
              }
            }
          }
        }
        catch (e: CancellationException) {
          throw e
        }
        catch (e: Exception) {
          logger<TfAsyncFormattingService>().warn("Failed to run FormattingTask", e)
          request.onError(HCLBundle.message("terraform.formatter.error.title", toolType.executableName),
                          HCLBundle.message("terraform.formatter.error.message", virtualFile.name, toolType.executableName))
        }
      }

      override fun cancel(): Boolean {
        job?.cancel()
        return true
      }

      override fun isRunUnderProgress(): Boolean = true
    }
  }
}

internal fun normalizeAnsiText(text: String): String = AnsiCsiUtil.stripAnsi(text).replace(BoxDrawingRegex, "").trim()

private const val TestsExtension = "tftest.hcl"
private val BoxDrawingRegex: Regex = Regex("[\u2500-\u257F]")