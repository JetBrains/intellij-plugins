// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer

import com.intellij.psi.xml.XmlTokenType

interface Angular2HtmlTokenTypes : XmlTokenType {
  companion object {
    @JvmField
    val INTERPOLATION_START = Angular2HtmlTokenType("NG:INTERPOLATION_START")

    @JvmField
    val INTERPOLATION_END = Angular2HtmlTokenType("NG:INTERPOLATION_END")

    @JvmField
    val EXPANSION_FORM_START = Angular2HtmlTokenType("NG:EXPANSION_FORM_START")

    @JvmField
    val EXPANSION_FORM_END = Angular2HtmlTokenType("NG:EXPANSION_FORM_END")

    @JvmField
    val EXPANSION_FORM_CASE_START = Angular2HtmlTokenType("NG:EXPANSION_FORM_CASE_START")

    @JvmField
    val EXPANSION_FORM_CASE_END = Angular2HtmlTokenType("NG:EXPANSION_FORM_CASE_END")
  }
}