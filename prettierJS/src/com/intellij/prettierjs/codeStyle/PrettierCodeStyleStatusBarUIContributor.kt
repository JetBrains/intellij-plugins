// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeStyle

import com.intellij.application.options.CodeStyle
import com.intellij.ide.actions.ShowSettingsUtilImpl
import com.intellij.lang.javascript.linter.JSLinterGuesser
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.prettierjs.CONFIGURABLE_ID
import com.intellij.prettierjs.OpenConfigurationAction
import com.intellij.prettierjs.PrettierBundle
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.IndentStatusBarUIContributor
import com.intellij.psi.codeStyle.modifier.CodeStyleStatusBarUIContributor

internal class PrettierCodeStyleStatusBarUIContributor : CodeStyleStatusBarUIContributor {
  private var myIndentOptionsForFileInEditor: CommonCodeStyleSettings.IndentOptions? = null

  override fun areActionsAvailable(file: VirtualFile): Boolean = true

  override fun getActionGroupTitle(): @NlsContexts.PopupTitle String = PrettierBundle.message("prettier.code.style.status.bar.action.group.title")

  override fun getActions(file: PsiFile): Array<out AnAction?> = createNavigationActions(file)

  @Suppress("DialogTitleCapitalization")
  override fun getTooltip(): String? {
    val optionsForFileInEditor = myIndentOptionsForFileInEditor
    if (optionsForFileInEditor == null) {
      // not ready yet
      return null
    }
    return IndentStatusBarUIContributor.createTooltip(
      IndentStatusBarUIContributor.getIndentInfo(optionsForFileInEditor),
      getActionGroupTitle())
  }

  override fun createDisableAction(project: Project): AnAction = createDisableCodeStyleModifierAction(project)

  override fun getStatusText(psiFile: PsiFile): @NlsContexts.StatusBarText String {
    val optionsForFileInEditor = CodeStyle.getSettings(psiFile).getIndentOptions(psiFile.getFileType())
    myIndentOptionsForFileInEditor = optionsForFileInEditor
    return IndentStatusBarUIContributor.getIndentInfo(optionsForFileInEditor)
  }

  private fun createNavigationActions(file: PsiFile): Array<AnAction> {
    return buildList {
      add(OpenConfigurationAction(file.project, file.virtualFile))
      add(OpenSettingsDialogAction())
    }.toTypedArray()
  }

  private fun createDisableCodeStyleModifierAction(project: Project): AnAction {
    return DumbAwareAction.create(PrettierBundle.message("prettier.action.disable.for.project.label")) { _ ->
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
}