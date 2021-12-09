// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.prettier

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.prettierjs.PrettierConfig
import com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller
import com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller.applyCommonPrettierSettings
import com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller.commonPrettierSettingsApplied
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import org.jetbrains.annotations.NotNull
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings

internal class VuePrettierCodeStyleInstaller : PrettierCodeStyleInstaller {
  override fun install(project: Project, config: PrettierConfig, settings: CodeStyleSettings) {
    settings.getCustomSettings(VueCodeStyleSettings::class.java).let {
      it.INDENT_CHILDREN_OF_TOP_LEVEL = getIndentChildrenOfTopLevelSequence(it)
        .plus("template")
        .let { tags ->
          if (config.vueIndentScriptAndStyle) {
            tags.plus(sequenceOf("script", "style"))
          }
          else {
            tags.minus(sequenceOf("script", "style"))
          }
        }
        .distinct()
        .joinToString(", ")

      // Default prettier settings
      it.UNIFORM_INDENT = true
      it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = true
      it.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER = false
      it.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER = false
      it.INTERPOLATION_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    }
    applyCommonPrettierSettings(config, settings, VueLanguage.INSTANCE)
  }

  override fun isInstalled(project: Project, config: PrettierConfig, settings: CodeStyleSettings): Boolean {
    return settings.getCustomSettings(VueCodeStyleSettings::class.java)
             .let {
               it.UNIFORM_INDENT
               && it.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS
               && !it.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER
               && !it.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER
               && it.INTERPOLATION_WRAP == CommonCodeStyleSettings.DO_NOT_WRAP
               && getIndentChildrenOfTopLevelSequence(it)
                 .toSet()
                 .let { tags ->
                   if (config.vueIndentScriptAndStyle) {
                     tags.contains("script") && tags.contains("style")
                   }
                   else {
                     !tags.contains("script") && !tags.contains("style")
                   }
                   && tags.contains("template")
                 }
             }
           && commonPrettierSettingsApplied(config, settings, VueLanguage.INSTANCE)
  }

  private fun getIndentChildrenOfTopLevelSequence(settings: @NotNull VueCodeStyleSettings): Sequence<String> {
    return settings.INDENT_CHILDREN_OF_TOP_LEVEL
      .split(',')
      .asSequence()
      .map { StringUtil.toLowerCase(it.trim()) }
  }
}