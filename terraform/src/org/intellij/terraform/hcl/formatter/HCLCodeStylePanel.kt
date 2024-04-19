// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.codeStyle.OptionTableWithPreviewPanel
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import org.intellij.terraform.hcl.HCLBundle

/**
 * @author Vladislav Rassokhin
 */
class HCLCodeStylePanel(private val language: Language, settings: CodeStyleSettings) : OptionTableWithPreviewPanel(settings) {

  init {
    init()
  }

  override fun initTables() {
    // Format alignment
    val alignmentValues = HCLCodeStyleSettings.PropertyAlignment.values()
    val alignmentStrings = arrayOfNulls<String>(alignmentValues.size)
    val alignmentInts = IntArray(alignmentValues.size)
    for (i in alignmentValues.indices) {
      alignmentStrings[i] = alignmentValues[i].description
      alignmentInts[i] = alignmentValues[i].id
    }

    // Line Commenter character
    val lineCommenterPrefixValues = HCLCodeStyleSettings.LineCommenterPrefix.values()
    val lineCommenterPrefixStrings = arrayOfNulls<String>(lineCommenterPrefixValues.size)
    val lineCommenterPrefixInts = IntArray(lineCommenterPrefixValues.size)
    for (i in lineCommenterPrefixValues.indices) {
      lineCommenterPrefixStrings[i] = lineCommenterPrefixValues[i].description
      lineCommenterPrefixInts[i] = lineCommenterPrefixValues[i].id
    }

    showCustomOption(HCLCodeStyleSettings::class.java, "PROPERTY_ALIGNMENT", HCLBundle.message("code.style.align.properties.title"),
                     HCLBundle.message("code.style.formatting.options.group.name"), alignmentStrings, alignmentInts)
    showCustomOption(HCLCodeStyleSettings::class.java, "PROPERTY_LINE_COMMENTER_CHARACTER",
                     HCLBundle.message("code.style.line.commenter.character.title"),
                     HCLBundle.message("code.style.code.conventions.group.name"), lineCommenterPrefixStrings, lineCommenterPrefixInts)
  }

  override fun getSettingsType(): LanguageCodeStyleSettingsProvider.SettingsType {
    return LanguageCodeStyleSettingsProvider.SettingsType.LANGUAGE_SPECIFIC
  }

  override fun getDefaultLanguage(): Language = language

  override fun getPreviewText(): String = ALIGNMENT_SAMPLE

  companion object {
    const val ALIGNMENT_SAMPLE = "#first logical block\n" +
                                 "first = true\n" +
                                 "second = false\n" +
                                 "t_h_i_r_d = 1\n" +
                                 "object = {\n" +
                                 "  nested = true\n"+
                                 "  z = false\n"+
                                 "}\n" +
                                 "\n" +
                                 "#another logical block\n" +
                                 "x = true\n" +
                                 "long = 'acceptable'"
  }
}
