// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.formatter

import com.intellij.lang.html.HTMLLanguage
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.FileIndentOptionsProvider
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueFileIndentOptionsProvider : FileIndentOptionsProvider() {

  override fun getIndentOptions(settings: CodeStyleSettings, file: PsiFile): CommonCodeStyleSettings.IndentOptions? {
    if (file.language is VueLanguage) {
      val fileType = file.originalFile.virtualFile?.fileType
      if (fileType === null || fileType === VueFileType.INSTANCE) {
        val vueSettings = settings.getCustomSettings(VueCodeStyleSettings::class.java)
        if (vueSettings.UNIFORM_INDENT) {
          val indentOptions = settings.getLanguageIndentOptions(VueLanguage.INSTANCE)?.clone() as CommonCodeStyleSettings.IndentOptions?
                              ?: return null
          indentOptions.isOverrideLanguageOptions = true
          return indentOptions
        }
        return settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE)
      }
      return settings.getIndentOptions(fileType)
    }
    return null
  }
}