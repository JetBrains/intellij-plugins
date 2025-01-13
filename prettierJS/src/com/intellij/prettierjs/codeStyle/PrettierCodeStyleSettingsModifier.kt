// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeStyle

import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.prettierjs.PrettierBundle
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.PrettierUtil
import com.intellij.prettierjs.resolveConfigForFile
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.modifier.CodeStyleSettingsModifier
import com.intellij.psi.codeStyle.modifier.CodeStyleStatusBarUIContributor
import com.intellij.psi.codeStyle.modifier.TransientCodeStyleSettings
import java.util.function.Consumer

private val LOG: Logger
  get() = logger<PrettierCodeStyleSettingsModifier>()

private class PrettierCodeStyleSettingsModifier : CodeStyleSettingsModifier {
  override fun modifySettings(settings: TransientCodeStyleSettings, psiFile: PsiFile): Boolean {
    val project = psiFile.project
    val file = psiFile.virtualFile

    if (project.isDisposed) return false
    if (!PrettierConfiguration.getInstance(project).codeStyleSettingsModifierEnabled) return false
    if (!PrettierUtil.isFormattingAllowedForFile(project, file)) return false
    if (!PrettierUtil.checkNodeAndPackage(psiFile, null, PrettierUtil.NOOP_ERROR_HANDLER)) return false

    return doModifySettings(settings, psiFile)
  }

  override fun mayOverrideSettingsOf(project: Project): Boolean {
    return PrettierConfiguration.getInstance(project).codeStyleSettingsModifierEnabled
  }

  override fun getDisablingFunction(project: Project): Consumer<CodeStyleSettings?>? {
    return Consumer { settings: CodeStyleSettings? ->
      PrettierConfiguration.getInstance(project).state.codeStyleSettingsModifierEnabled = false
      CodeStyleSettingsManager.getInstance(project).notifyCodeStyleSettingsChanged()
    }
  }

  override fun getName(): String = PrettierBundle.message("prettier.code.style.display.name")

  override fun getStatusBarUiContributor(transientSettings: TransientCodeStyleSettings): CodeStyleStatusBarUIContributor? {
    return PrettierCodeStyleStatusBarUIContributor()
  }

  private fun doModifySettings(settings: TransientCodeStyleSettings, psiFile: PsiFile): Boolean {
    val prettierConfig = runBlockingCancellable {
      resolveConfigForFile(psiFile)
    }?.config ?: return false

    val codeStyle = getCodeStyleForLanguage(psiFile.language)

    if (codeStyle.isApplied(settings, psiFile, prettierConfig)) {
      LOG.debug { "No changes for ${psiFile.name}" }
      return false
    }
    else {
      codeStyle.applySettings(settings, psiFile, prettierConfig)
      LOG.debug { "Modified for ${psiFile.name}" }
      return true
    }
  }

  private fun getCodeStyleForLanguage(language: Language): PrettierCodeStyleConfigurator {
    return when (language) {
      HTMLLanguage.INSTANCE -> HtmlPrettierCodeStyleConfigurator()
      JavaScriptSupportLoader.TYPESCRIPT_JSX, JavaScriptSupportLoader.TYPESCRIPT -> JsPrettierCodeStyleConfigurator(TypeScriptCodeStyleSettings::class.java)
      is JSLanguageDialect -> JsPrettierCodeStyleConfigurator(JSCodeStyleSettings::class.java)
      else -> DefaultPrettierCodeStyleConfigurator()
    }
  }
}