// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.editor

import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.javascript.linter.tslint.TsLintBundle
import com.intellij.lang.javascript.linter.tslint.TslintUtil
import com.intellij.lang.javascript.linter.tslint.codestyle.TsLintImportCodeStyleAction
import com.intellij.lang.javascript.linter.tslint.codestyle.rules.TsLintConfigWrapper
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications

private val KEY = Key.create<EditorNotificationPanel>("TsLint.Import.Code.Style.Notification")

class TsLintCodeStyleEditorNotificationProvider(private val project: Project) : EditorNotifications.Provider<EditorNotificationPanel>() {

  private val NOTIFICATION_DISMISSED_PROPERTY = "tslint.code.style.apply.dismiss"

  private fun isNotificationDismissed(file: VirtualFile): Boolean {
    return PropertiesComponent.getInstance(project).getBoolean(NOTIFICATION_DISMISSED_PROPERTY) ||
           !TslintUtil.isConfigFile(file)
  }

  private fun dismissNotification() {
    PropertiesComponent.getInstance(project).setValue(NOTIFICATION_DISMISSED_PROPERTY, true)
    EditorNotifications.getInstance(project).updateAllNotifications()
  }

  override fun getKey() = KEY

  override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor, project: Project): EditorNotificationPanel? {
    if (fileEditor !is TextEditor || fileEditor.editor !is EditorEx) return null
    if (isNotificationDismissed(file)) return null

    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null
    val wrapper = TsLintConfigWrapper.getConfigForFile(psiFile) ?: return null
    val rules = wrapper.getRulesToApply(project)

    if (rules.isEmpty()) return null

    return object : EditorNotificationPanel(EditorColors.GUTTER_BACKGROUND) {
      init {
        text = TsLintBundle.message("tslint.code.style.apply.message")
        createActionLabel(TsLintBundle.message("tslint.code.style.apply.text"), TsLintImportCodeStyleAction.ACTION_ID, false)
        createActionLabel(TsLintBundle.message("tslint.code.style.dismiss.text"), Runnable { dismissNotification() }, false)
      }
    }
  }
}