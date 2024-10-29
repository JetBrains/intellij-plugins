// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.WrapConstant
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

@Suppress("PropertyName")
class HclCodeStyleSettings(container: CodeStyleSettings, language: Language) : CustomCodeStyleSettings(language.id, container) {
  @JvmField
  var PROPERTY_ALIGNMENT: PropertyAlignment = PropertyAlignment.ON_EQUALS
  @JvmField
  var LINE_COMMENTER_CHARACTER: LineCommenterPrefix = LineCommenterPrefix.POUND_SIGN
  @JvmField
  var RUN_TF_FMT_ON_REFORMAT: Boolean = false
  @JvmField
  var IMPORT_PROVIDERS_AUTOMATICALLY: Boolean = true

  @WrapConstant
  @JvmField
  var OBJECT_WRAPPING: Int = CommonCodeStyleSettings.WRAP_AS_NEEDED

  @WrapConstant
  @JvmField
  var ARRAY_WRAPPING: Int = CommonCodeStyleSettings.WRAP_AS_NEEDED
}

enum class PropertyAlignment(private val description: String) {
  DO_NOT_ALIGN("Do not align"),
  ON_VALUE("On value"),
  ON_EQUALS("On equals");

  override fun toString(): String {
    return description
  }
}

enum class LineCommenterPrefix(private val description: String, val prefix: String) {
  DOUBLE_SLASHES("Double Slashes (//)", "//"),
  POUND_SIGN("Pound Sign (#)", "#");

  override fun toString(): String {
    return description
  }
}