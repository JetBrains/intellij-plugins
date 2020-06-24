package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.io.Compressor
import java.io.File
import java.io.IOException


/**
 * This action may act as an example of how to access various C/C++ project model information from a plugin
 *
 * @see collectToolchains
 * @see collectCidrWorkspaces
 * @see collectOCWorkspace
 * @see collectOCWorkspaceEvents
 */
class CppDiagnosticsAction : DumbAwareAction() {
  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = getEventProject(e) != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = getEventProject(e) ?: return

    val task = object : Task.Modal(project, CppDiagnosticsBundle.message("cpp.diagnostics.progress.title"), true) {
      override fun run(indicator: ProgressIndicator) {
        val toolchains = readActionWithText(indicator, CppDiagnosticsBundle.message("cpp.diagnostics.progress.toolchains")) {
          collectToolchains()
        }

        val cidrWorkspaces = readActionWithText(indicator, CppDiagnosticsBundle.message("cpp.diagnostics.progress.workspace")) {
          collectCidrWorkspaces(project)
        }

        val ocWorkspace = readActionWithText(indicator, CppDiagnosticsBundle.message("cpp.diagnostics.progress.ocWorkspace")) {
          collectOCWorkspace(project)
        }

        val ocWorkspaceEvents = readActionWithText(indicator, CppDiagnosticsBundle.message("cpp.diagnostics.progress.ocWorkspaceEvents")) {
          collectOCWorkspaceEvents(project)
        }


        val files = listOf(
          FileWithContents("Toolchains.txt", toolchains),
          FileWithContents("CidrWorkspaces.txt", cidrWorkspaces),
          FileWithContents("OCWorkspaceEvents.txt", ocWorkspaceEvents),
          FileWithContents("OCWorkspace.txt", ocWorkspace)
        )

        zipAndReveal(files)

        if (Registry.`is`("cpp.diagnostics.also.open.in.editor")) {
          AppUIExecutor.onUiThread().expireWith(project).submit {
            for (file in files) {
              val virtualFile = LightVirtualFile(file.filename, file.contents)
              FileEditorManager.getInstance(project).openFile(virtualFile, true)
            }
          }
        }
      }
    }

    ProgressManager.getInstance().run(task)
  }

  companion object {
    private data class FileWithContents(val filename: String, val contents: String)

    @Throws(IOException::class)
    private fun createZip(files: Iterable<FileWithContents>): File {
      val productName = StringUtil.toLowerCase(ApplicationNamesInfo.getInstance().lowercaseProductName)
      val zippedLogsFile = FileUtil.createTempFile(productName + "-cpp-diag-" + formatCurrentTime(), ".zip")
      try {
        val zip = Compressor.Zip(zippedLogsFile)
        for (file in files) {
          zip.addFile(file.filename, file.contents.byteInputStream())
        }
        zip.close()
      }
      catch (exception: IOException) {
        FileUtil.delete(zippedLogsFile)
        throw exception
      }
      return zippedLogsFile
    }

    private fun zipAndReveal(files: Iterable<FileWithContents>) {
      try {
        val zippedDiagnostics = createZip(files)

        if (RevealFileAction.isSupported()) {
          RevealFileAction.openFile(zippedDiagnostics)
        }
        else {
          val logNotification = Notification(
            NOTIFICATION_GROUP.displayId,
            "",
            CppDiagnosticsBundle.message("cpp.diagnostics.are.collected.0", zippedDiagnostics.absolutePath),
            NotificationType.INFORMATION)
          Notifications.Bus.notify(logNotification)
        }
      }
      catch(e: IOException) {
        val errorNotification = Notification(
          NOTIFICATION_GROUP.displayId,
          "",
          CppDiagnosticsBundle.message("cpp.diagnostics.cant.write.0", e.localizedMessage),
          NotificationType.ERROR)
        Notifications.Bus.notify(errorNotification)
      }
    }


    private fun <R> readActionWithText(indicator: ProgressIndicator, text: String, block: () -> R): R {
      val oldText = indicator.text
      indicator.text = text
      try {
        return runReadAction(block);
      }
      finally {
        indicator.text = oldText
      }
    }

    private val NOTIFICATION_GROUP = NotificationGroup("C/C++ Diagnostics", NotificationDisplayType.BALLOON, true)
  }
}