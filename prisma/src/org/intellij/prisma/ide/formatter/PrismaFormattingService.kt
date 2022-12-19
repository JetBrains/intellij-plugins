// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.formatter

import com.google.gson.Gson
import com.intellij.application.options.CodeStyle
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.CoreFormattingService
import com.intellij.formatting.service.FormattingService
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.execution.NodeTargetRunOptions
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.wsl.WslNodeInterpreter
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.util.io.URLUtil
import com.intellij.util.text.CharArrayUtil
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.formatter.settings.PrismaCodeStyleSettings
import org.intellij.prisma.ide.ui.PrismaNotifications
import org.intellij.prisma.lang.PrismaFileType
import org.intellij.prisma.lang.psi.PrismaFile
import java.io.IOException
import java.nio.charset.StandardCharsets

class PrismaFormattingService : AsyncDocumentFormattingService() {
  companion object {
    private val LOG = thisLogger()

    @NlsSafe
    private const val PRISMA_FMT = "prisma-fmt"

    internal val USE_PRISMA_FMT = Key.create<Boolean>("prisma.use.prisma.fmt")
  }

  override fun getName(): String = PRISMA_FMT

  override fun getNotificationGroupId(): String = PrismaNotifications.NOTIFICATION_GROUP_ID

  override fun getFeatures() = emptySet<FormattingService.Feature>()

  override fun canFormat(file: PsiFile): Boolean = file is PrismaFile

  override fun createFormattingTask(formattingRequest: AsyncFormattingRequest): FormattingTask? {
    if (ApplicationManager.getApplication().isUnitTestMode &&
        USE_PRISMA_FMT.get(formattingRequest.context.containingFile) != true) {
      return null
    }

    val context = formattingRequest.context
    val documentText = formattingRequest.documentText
    if (!CodeStyle.getCustomSettings(context.containingFile, PrismaCodeStyleSettings::class.java).RUN_PRISMA_FMT_ON_REFORMAT) {
      return null
    }

    val interpreter = NodeJsInterpreterManager.getInstance(context.project).interpreter
    if (interpreter !is NodeJsLocalInterpreter && interpreter !is WslNodeInterpreter) {
      formattingRequest.onError(PrismaBundle.message("prisma.formatter.error.title"),
                                PrismaBundle.message("prisma.interpreter.not.configured"))
      return null
    }

    val formatter = JSLanguageServiceUtil.getPluginDirectory(javaClass, "language-server/prisma-fmt.js")
    if (formatter == null || !formatter.exists()) {
      formattingRequest.onError(PrismaBundle.message("prisma.formatter.error.title"),
                                PrismaBundle.message("prisma.formatter.runner.not.found"))
      return null
    }

    val nodeTargetRun = NodeTargetRun(interpreter, context.project, null, NodeTargetRunOptions.of(false))
    nodeTargetRun.commandLineBuilder.apply {
      charset = StandardCharsets.UTF_8
      setWorkingDirectory(nodeTargetRun.path(formatter.parent))
      addParameter(nodeTargetRun.path(formatter.path))
      addParameter(createFormattingParamsArg(context, nodeTargetRun))
    }
    val handler = nodeTargetRun.startProcess()

    return object : FormattingTask {
      override fun run() {
        handler.addProcessListener(object : ProcessListener {
          override fun startNotified(event: ProcessEvent) {
            try {
              handler.processInput?.use {
                documentText.byteInputStream().transferTo(it)
              }
            }
            catch (e: IOException) {
              formattingRequest.onError(PrismaBundle.message("prisma.formatter.error.title"), e.localizedMessage)
              LOG.warn("Can't send a document to prisma-fmt", e)
            }
          }
        })

        handler.addProcessListener(object : CapturingProcessAdapter() {
          override fun processTerminated(event: ProcessEvent) {
            if (event.exitCode != 0 || output.stderr.isNotEmpty()) {
              formattingRequest.onError(PrismaBundle.message("prisma.formatter.error.title"), output.stderr)
            }
            else {
              formattingRequest.onTextReady(processFormattedText(output.stdout))
            }
          }

          private fun processFormattedText(text: String): String {
            // prisma-fmt adds a redundant new line before the EOF
            val nonNewLineChar = CharArrayUtil.shiftBackward(text, text.length - 1, "\n")
            return text.replaceRange(nonNewLineChar + 1, text.length, "\n")
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

  private fun createFormattingParamsArg(context: FormattingContext, nodeTargetRun: NodeTargetRun): String {
    val indentOptions = context.codeStyleSettings.getIndentOptions(PrismaFileType)
    val params = PrismaDocumentFormattingParams(
      PrismaTextDocumentIdentifier(getFileUrl(context)),
      PrismaFormattingOptions(indentOptions.TAB_SIZE, !indentOptions.USE_TAB_CHARACTER)
    )
    return Gson().toJson(params)
  }

  private fun getFileUrl(context: FormattingContext): String {
    val file = context.virtualFile ?: return ""
    val url = VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, file.path)
    val uri = VfsUtil.toUri(url)
    return uri?.toString() ?: url
  }

  override fun runAfter(): Class<out FormattingService> {
    return CoreFormattingService::class.java
  }
}

private class PrismaDocumentFormattingParams(val textDocument: PrismaTextDocumentIdentifier, val options: PrismaFormattingOptions)
private class PrismaTextDocumentIdentifier(val uri: String)
private class PrismaFormattingOptions(val tabSize: Int,
                                      val insertSpaces: Boolean,
                                      val trimTrailingWhitespace: Boolean = true,
                                      val insertFinalNewline: Boolean = true,
                                      val trimFinalNewlines: Boolean = true)