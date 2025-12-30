// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.WrapConstant
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

@Suppress("PropertyName")
class Angular2HtmlCodeStyleSettings(settings: CodeStyleSettings) : CustomCodeStyleSettings("Angular2HtmlCodeStyleSettings", settings) {

  @JvmField
  var SPACES_WITHIN_INTERPOLATION_EXPRESSIONS: Boolean = true

  @WrapConstant
  @JvmField
  var INTERPOLATION_WRAP: Int = CommonCodeStyleSettings.DO_NOT_WRAP

  @JvmField
  var INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER: Boolean = true

  @JvmField
  var INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER: Boolean = true

}
