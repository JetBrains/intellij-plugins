package com.intellij.lang.javascript.linter.tslint.editor

import com.intellij.lang.javascript.JSBundle
import com.intellij.lang.javascript.linter.JSLinterUtil
import com.intellij.lang.javascript.linter.LinterCodeStyleImportSourceTracker
import com.intellij.lang.javascript.linter.tslint.TsLintBundle
import com.intellij.lang.javascript.linter.tslint.TslintUtil
import com.intellij.lang.javascript.linter.tslint.config.style.rules.TsLintConfigWrapper
import com.intellij.lang.javascript.linter.tslint.config.style.rules.TsLintSimpleRule
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

class TsLintCodeStyleEditorNotificationProvider(project: Project) : EditorNotifications.Provider<EditorNotificationPanel>() {
  private val mySourceTracker: LinterCodeStyleImportSourceTracker = LinterCodeStyleImportSourceTracker(
    project, "tslint", TslintUtil::isConfigFile)

  override fun getKey(): Key<EditorNotificationPanel> = KEY

  override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
    if (fileEditor !is TextEditor || fileEditor.editor !is EditorEx) return null

    val project = fileEditor.editor.project ?: return null

    if (mySourceTracker.shouldDismiss(file)) return null

    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null
    val wrapper = TsLintConfigWrapper.getConfigForFile(psiFile) ?: return null
    val rules: Collection<TsLintSimpleRule<*>> = wrapper.getRulesToApply(project)

    if (rules.isEmpty()) return null

    return object : EditorNotificationPanel(EditorColors.GUTTER_BACKGROUND) {
      init {
        setText(TsLintBundle.message("tslint.code.style.apply.message"))
        val okAction = Runnable {
          runWriteActionAndUpdateNotifications(project, file, wrapper, rules)
        }
        createActionLabel(TsLintBundle.message("tslint.code.style.apply.text"), okAction)
        createActionLabel(TsLintBundle.message("tslint.code.style.dismiss.text"), mySourceTracker.dismissAction)
      }
    }
  }

  private fun runWriteActionAndUpdateNotifications(project: Project,
                                                   file: VirtualFile,
                                                   wrapper: TsLintConfigWrapper,
                                                   rules: Collection<TsLintSimpleRule<*>>) {
    wrapper.applyRules(project, rules)
    EditorNotifications.getInstance(project).updateAllNotifications()
    JSLinterUtil.reportCodeStyleSettingsImported(project, JSBundle.message("settings.javascript.linters.tslint.configurable.name"), file,
                                                 rules.map { it.optionId }.toSet())
  }
}