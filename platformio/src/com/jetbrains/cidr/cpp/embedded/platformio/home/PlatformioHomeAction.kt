package com.jetbrains.cidr.cpp.embedded.platformio.home

import com.intellij.icons.AllIcons
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.project.useWebView
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase
import java.nio.file.Path
import javax.swing.JComponent

class PlatformioHomeAction : PlatformioActionBase(ClionEmbeddedPlatformioBundle.messagePointer("task.home"), { "pio home" },
                                                  pioIcon(AllIcons.Nodes.HomeFolder)) {
  override fun actionPerformed(e: AnActionEvent) {
    if (useWebView()) {
      actionPerformedDialog(e)
    }
    else {
      actionPerformed(e, false, false, false, "home")
    }
  }

  private fun actionPerformedDialog(e: AnActionEvent) {
    val project = e.project
    val homeDialog = PlatformioHomeDialog(project, e.inputEvent?.source.asSafely<JComponent>())
    if (homeDialog.showAndGet()) {
      if (homeDialog.getProjectLocationToOpen() != null) {
        ProjectUtil.openOrImport(Path.of(homeDialog.getProjectLocationToOpen()!!), project, false)
      }
      else {
        val virtualFile = homeDialog.getDocumentLocationToOpen()?.let { VfsUtil.findFile(Path.of(it), true) }
        if (project != null && virtualFile != null) {
          FileEditorManager.getInstance(project).openEditor(OpenFileDescriptor(project, virtualFile), true)
        }
        else {
          Messages.showErrorDialog(project, ClionEmbeddedPlatformioBundle.message("dialog.message.cant.open.file",
                                                                                  homeDialog.getDocumentLocationToOpen()),
                                   ClionEmbeddedPlatformioBundle.message("dialog.title.open.file"))
        }
      }
    }
  }
}