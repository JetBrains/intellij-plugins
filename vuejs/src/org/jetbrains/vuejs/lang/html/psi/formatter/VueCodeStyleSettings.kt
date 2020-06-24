// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.formatter

import com.intellij.application.options.codeStyle.properties.CommaSeparatedValues
import com.intellij.configurationStore.Property
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.WrapConstant
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import org.jetbrains.annotations.NonNls

@Suppress("PropertyName")
class VueCodeStyleSettings(settings: CodeStyleSettings) : CustomCodeStyleSettings("VueCodeStyleSettings", settings) {

  @Property(externalName = "indent_children_of_top_level_tags")
  @CommaSeparatedValues
  @NonNls
  @JvmField
  var INDENT_CHILDREN_OF_TOP_LEVEL = "template"

  @Property(externalName = "uniform_indent")
  @JvmField
  var UNIFORM_INDENT = true

  @JvmField
  var SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = true

  @WrapConstant
  @JvmField
  var INTERPOLATION_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP

  @JvmField
  var INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER = true

  @JvmField
  var INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER = true

}
