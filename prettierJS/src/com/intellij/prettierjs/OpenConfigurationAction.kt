// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.lang.javascript.linter.JSLinterGuesser
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

internal class OpenConfigurationAction(
  private val project: Project,
  private val file: VirtualFile,
  icon: Icon? = null,
) : DumbAwareAction(PrettierBundle.message("prettier.action.open.configuration.file.label"), null, icon) {
  override fun actionPerformed(e: AnActionEvent) {
    val configFile = PrettierUtil.findFileConfig(project, file)

    if (configFile != null) {
      val fileEditorManager = FileEditorManager.getInstance(project)
      if (fileEditorManager.isFileOpen(configFile)) {
        fileEditorManager.closeFile(configFile)
      }
      fileEditorManager.openFile(configFile, true)
    }
    else {
      val notification = JSLinterGuesser
        .NOTIFICATION_GROUP
        .createNotification(
          PrettierBundle.message("prettier.formatter.notification.title"),
          PrettierBundle.message("prettier.notification.config.not.found"),
          NotificationType.INFORMATION
        )
      notification.notify(project)
    }
  }
}
