// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.WrapConstant
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import org.intellij.terraform.hcl.HCLBundle

@Suppress("PropertyName")
internal class HclCodeStyleSettings(languageId: String, container: CodeStyleSettings) : CustomCodeStyleSettings(languageId, container) {
  @JvmField
  var PROPERTY_ALIGNMENT: PropertyAlignment = PropertyAlignment.ON_EQUALS

  @JvmField
  var LINE_COMMENTER_CHARACTER: LineCommenterPrefix = LineCommenterPrefix.POUND_SIGN

  @JvmField
  var RUN_TF_FMT_ON_REFORMAT: Boolean = true

  @JvmField
  var IMPORT_PROVIDERS_AUTOMATICALLY: Boolean = true

  @WrapConstant
  @JvmField
  var OBJECT_WRAPPING: Int = CommonCodeStyleSettings.WRAP_AS_NEEDED

  @WrapConstant
  @JvmField
  var ARRAY_WRAPPING: Int = CommonCodeStyleSettings.WRAP_AS_NEEDED
}

enum class PropertyAlignment {
  DO_NOT_ALIGN,
  ON_VALUE,
  ON_EQUALS;

  override fun toString(): String = when (this) {
    DO_NOT_ALIGN -> HCLBundle.message("code.style.align.properties.do.not.align")
    ON_VALUE -> HCLBundle.message("code.style.align.properties.on.value")
    ON_EQUALS -> HCLBundle.message("code.style.align.properties.on.equals")
  }
}

enum class LineCommenterPrefix(val prefix: String) {
  DOUBLE_SLASHES("//"),
  POUND_SIGN("#");

  override fun toString(): String = when (this) {
    DOUBLE_SLASHES -> HCLBundle.message("code.style.line.commenter.double.slashes")
    POUND_SIGN -> HCLBundle.message("code.style.line.commenter.pound.sign")
  }
}