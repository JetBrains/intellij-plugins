package com.intellij.lang.javascript.linter.tslint.editor

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.lang.javascript.JSBundle
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.linter.JSLinterUtil
import com.intellij.lang.javascript.linter.LinterCodeStyleImportSourceTracker
import com.intellij.lang.javascript.linter.tslint.TsLintBundle
import com.intellij.lang.javascript.linter.tslint.config.style.rules.TsLintConfigWrapper
import com.intellij.lang.javascript.linter.tslint.config.style.rules.TsLintSimpleRule
import com.intellij.lang.javascript.linter.tslint.config.style.rules.TslintRulesSet
import com.intellij.lang.javascript.linter.tslint.ide.TsLintConfigFileType
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.util.*
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import java.util.*

private val KEY = Key.create<EditorNotificationPanel>("TsLint.Import.Code.Style.Notification")

val RULES_CACHE_KEY = Key.create<ParameterizedCachedValue<TsLintConfigWrapper, PsiFile>>("tslint.cache.key.config.json")

class TsLintCodeStyleEditorNotificationProvider(project: Project) : EditorNotifications.Provider<EditorNotificationPanel>() {
  private val mySourceTracker: LinterCodeStyleImportSourceTracker = LinterCodeStyleImportSourceTracker(
    project, "tslint", { it.fileType == TsLintConfigFileType.INSTANCE })
  private val RULES_TO_APPLY: ParameterizedCachedValueProvider<TsLintConfigWrapper, PsiFile> = ParameterizedCachedValueProvider {
    if (it == null || PsiTreeUtil.hasErrorElements(it)) {
      return@ParameterizedCachedValueProvider CachedValueProvider.Result.create(null, it)
    }

    var jsonElement: JsonElement? = null
    try {
      jsonElement = JsonParser().parse(it.text)
    }
    catch (e: Exception) {
      //do nothing
    }
    if (jsonElement == null) {
      return@ParameterizedCachedValueProvider CachedValueProvider.Result.create(null, it)
    }

    val result = (if (jsonElement.isJsonObject) TsLintConfigWrapper(jsonElement.asJsonObject) else null)

    return@ParameterizedCachedValueProvider CachedValueProvider.Result.create(result, it)
  }

  override fun getKey(): Key<EditorNotificationPanel> = KEY

  override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
    if (fileEditor !is TextEditor || fileEditor.editor !is EditorEx) return null

    val project = fileEditor.editor.project ?: return null

    if (mySourceTracker.shouldDismiss(file)) return null

    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null

    val wrapper = CachedValuesManager.getManager(project)
                    .getParameterizedCachedValue(psiFile, RULES_CACHE_KEY, RULES_TO_APPLY, false, psiFile) ?: return null

    val settings = CodeStyleSettingsManager.getInstance(project).currentSettings
    val languageSettings = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT)
    val jsCodeStyleSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings::class.java)

    if (languageSettings == null || jsCodeStyleSettings == null) {
      return null
    }

    val rules = TslintRulesSet.filter { it.isAvailable(project, languageSettings, jsCodeStyleSettings, wrapper) }

    if (rules.isEmpty()) return null

    return object : EditorNotificationPanel(EditorColors.GUTTER_BACKGROUND) {
      init {
        setText(TsLintBundle.message("tslint.code.style.apply.message"))
        val okAction: Runnable = Runnable {
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
    WriteAction.run<RuntimeException> {
      val settingsManager = CodeStyleSettingsManager.getInstance(project)
      if (!settingsManager.USE_PER_PROJECT_SETTINGS) {
        settingsManager.PER_PROJECT_SETTINGS = settingsManager.currentSettings.clone()
        settingsManager.USE_PER_PROJECT_SETTINGS = true
      }
      val newSettings = settingsManager.currentSettings
      val newLanguageSettings = newSettings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT)
      val newJsCodeStyleSettings = newSettings.getCustomSettings(TypeScriptCodeStyleSettings::class.java)
      rules.forEach { rule -> rule.apply(project, newLanguageSettings, newJsCodeStyleSettings, wrapper) }
    }
    EditorNotifications.getInstance(project).updateAllNotifications()
    val rulesSet: Set<String> = HashSet()
    JSLinterUtil.reportCodeStyleSettingsImported(project, JSBundle.message("settings.javascript.linters.tslint.configurable.name"), file,
                                                 rulesSet.plus(rules.map { it.optionId }))
  }
}