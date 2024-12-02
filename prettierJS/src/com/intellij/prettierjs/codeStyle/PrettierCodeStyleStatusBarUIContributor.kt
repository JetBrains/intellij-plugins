// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeStyle

import com.intellij.application.options.CodeStyle
import com.intellij.ide.actions.ShowSettingsUtilImpl
import com.intellij.ide.ui.LafManager
import com.intellij.lang.javascript.linter.JSLinterGuesser
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.prettierjs.CONFIGURABLE_ID
import com.intellij.prettierjs.PrettierBundle
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.PrettierUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.IndentStatusBarUIContributor
import com.intellij.psi.codeStyle.modifier.CodeStyleStatusBarUIContributor
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import javax.swing.Icon

internal class PrettierCodeStyleStatusBarUIContributor : CodeStyleStatusBarUIContributor {
  override fun areActionsAvailable(file: VirtualFile): Boolean = true

  override fun getActionGroupTitle(): @NlsContexts.PopupTitle String? = PrettierBundle.message("prettier.code.style.status.bar.action.group.title")

  override fun getActions(file: PsiFile): Array<out AnAction?>? = createNavigationActions(file)

  override fun getTooltip(): String = PrettierBundle.message("prettier.code.style.status.bar.tooltip")

  override fun createDisableAction(project: Project): AnAction? = createDisableCodeStyleModifierAction(project)

  override fun getStatusText(psiFile: PsiFile): @NlsContexts.StatusBarText String {
    val fileOptions = CodeStyle.getSettings(psiFile).getIndentOptions(psiFile.getFileType())
    val projectOptions = CodeStyle.getSettings(psiFile.getProject()).getIndentOptions(psiFile.getFileType())
    var indentInfo = IndentStatusBarUIContributor.getIndentInfo(fileOptions)

    if (projectOptions.INDENT_SIZE != fileOptions.INDENT_SIZE || projectOptions.USE_TAB_CHARACTER != fileOptions.USE_TAB_CHARACTER) {
      indentInfo += "*"
    }
    return indentInfo
  }

  override fun getIcon(): Icon? {
    val statusBarFriendlyColor = when {
      LafManager.getInstance().currentUIThemeLookAndFeel.isDark -> JBUI.CurrentTheme.StatusBar.Widget.FOREGROUND
      else -> JBUI.CurrentTheme.StatusBar.Widget.FOREGROUND.brighter()
    }

    return PrettierUtil.ICON?.let {
      IconUtil.colorize(it, statusBarFriendlyColor)
    }
  }

  private fun createNavigationActions(file: PsiFile): Array<AnAction> {
    return buildList {
      add(OpenConfigurationAction(file = file))
      add(OpenSettingsDialogAction())
    }.toTypedArray()
  }

  private fun createDisableCodeStyleModifierAction(project: Project): AnAction {
    return DumbAwareAction.create(PrettierBundle.message("prettier.action.disable.for.project.label")) { e ->
      PrettierConfiguration.getInstance(project).state.codeStyleSettingsModifierEnabled = false
      CodeStyleSettingsManager.getInstance(project).notifyCodeStyleSettingsChanged()

      JSLinterGuesser
        .NOTIFICATION_GROUP
        .createNotification(
          PrettierBundle.message("prettier.formatter.notification.title"),
          PrettierBundle.message("prettier.notification.content.code.style.settings.modifier.has.been.disabled"),
          NotificationType.INFORMATION
        ).apply {
          addActions(
            listOf(
              ReEnableAction(project, this),
              OpenSettingsDialogAction(),
            )
          )
          notify(project)
        }
    }
  }

  private class ReEnableAction(private val project: Project, private val notification: Notification) : DumbAwareAction(PrettierBundle.message("prettier.action.reenable.label")) {
    override fun actionPerformed(e: AnActionEvent) {
      PrettierConfiguration.getInstance(project).state.codeStyleSettingsModifierEnabled = true
      CodeStyleSettingsManager.getInstance(project).notifyCodeStyleSettingsChanged()
      notification.expire()
    }
  }

  private class OpenSettingsDialogAction() : DumbAwareAction(PrettierBundle.message("prettier.action.open.settings.label")) {
    override fun actionPerformed(e: AnActionEvent) {
      ShowSettingsUtilImpl.showSettingsDialog(e.project, CONFIGURABLE_ID, "Prettier")
    }
  }

  private class OpenConfigurationAction(private val file: PsiFile) : DumbAwareAction(PrettierBundle.message("prettier.action.open.configuration.file.label")) {
    override fun actionPerformed(e: AnActionEvent) {
      val project = file.project
      val configFile = PrettierUtil.findFileConfig(project, file.virtualFile)

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

}