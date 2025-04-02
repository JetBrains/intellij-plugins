// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeStyle

import com.intellij.application.options.codeStyle.properties.AbstractCodeStylePropertyMapper
import com.intellij.application.options.codeStyle.properties.GeneralCodeStylePropertyMapper
import com.intellij.ide.DataManager
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.options.ex.ConfigurableWrapper
import com.intellij.openapi.options.ex.Settings
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.prettierjs.*
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
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
      DataManager.getInstance().dataContextFromFocusAsync.then { dataContext ->
        val settings = Settings.KEY.getData(dataContext) ?: return@then
        val configurable = settings.getConfigurableWithInitializedUiComponent(CONFIGURABLE_ID, false) ?: return@then
        val unwrapped = (configurable as? ConfigurableWrapper)?.rawConfigurable ?: configurable
        val prettierConfigurable = unwrapped as? PrettierConfigurable ?: return@then
        prettierConfigurable.uncheckCodeStyleModifierCheckBox()
      }
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

    val changedBasic = applyBasicPrettierMappings(settings, prettierConfig)

    var changedAdvanced = false
    for (configurator in getAdvancedCodeStyleConfigurators()) {
      if (!configurator.isApplied(settings, psiFile, prettierConfig)) {
        configurator.applySettings(settings, psiFile, prettierConfig)
        changedAdvanced = true
      }
    }

    if (changedBasic || changedAdvanced) {
      LOG.debug { "Modified for ${psiFile.name}" }
      return true
    }
    else {
      LOG.debug { "No changes for ${psiFile.name}" }
      return false
    }
  }

  private fun getAdvancedCodeStyleConfigurators(): List<PrettierCodeStyleConfigurator> {
    return listOf(
      HtmlPrettierCodeStyleConfigurator(),
      JsPrettierCodeStyleConfigurator(TypeScriptCodeStyleSettings::class.java),
      JsPrettierCodeStyleConfigurator(JSCodeStyleSettings::class.java),
    )
  }

  private fun applyBasicPrettierMappings(
    settings: TransientCodeStyleSettings,
    prettierConfig: PrettierConfig,
  ): Boolean {
    var changed = false

    val basicPropertyMap = mapOf(
      "indent_size" to prettierConfig.tabWidth.toString(),
      "tab_width" to prettierConfig.tabWidth.toString(),
      "continuation_indent_size" to prettierConfig.tabWidth.toString(),
      "indent_style" to if (prettierConfig.useTabs) "tab" else "space",
      "visual_guides" to prettierConfig.printWidth.toString()
    )

    getAllMappers(settings).forEach { mapper ->
      for ((propertyName, value) in basicPropertyMap) {
        mapper.getAccessor(propertyName)?.let {
          changed = changed or it.setFromString(value)
        }
      }

      mapper.getAccessor("end_of_line")?.let { accessor ->
        prettierConfig.lineSeparator?.let { lineSep ->
          changed = changed or accessor.setFromString(StringUtil.toLowerCase(lineSep.name))
        }
      }
    }

    return changed
  }

  private fun getAllMappers(
    settings: TransientCodeStyleSettings,
  ): Collection<AbstractCodeStylePropertyMapper> {
    return buildSet {
      addAll(LanguageCodeStyleSettingsProvider.getAllProviders().map { it.getPropertyMapper(settings) })
      add(GeneralCodeStylePropertyMapper(settings))
    }
  }
}

