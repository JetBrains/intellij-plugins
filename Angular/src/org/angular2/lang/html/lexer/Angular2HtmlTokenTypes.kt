// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.types.JSWhiteSpaceTokenType
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlLanguage

internal interface Angular2HtmlTokenTypes {
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

    @JvmField
    val BLOCK_NAME = Angular2HtmlTokenType("NG:BLOCK_NAME")

    @JvmField
    val BLOCK_START = Angular2HtmlTokenType("NG:BLOCK_START")

    @JvmField
    val BLOCK_END = Angular2HtmlTokenType("NG:BLOCK_END")

    @JvmField
    val BLOCK_PARAMETERS_START: IElementType = JSTokenTypes.LPAR

    @JvmField
    val BLOCK_SEMICOLON = Angular2HtmlTokenType("NG:BLOCK_SEMICOLON")

    @JvmField
    val BLOCK_PARAMETERS_END: IElementType = JSTokenTypes.RPAR

    // Highlight only tokens - TODO consider removing special highlighting for Angular expressionsxs
    @JvmField
    val EXPRESSION_WHITE_SPACE: IElementType = object : IElementType("NG:EXPRESSION_WHITE_SPACE", Angular2Language), JSWhiteSpaceTokenType {}

    @JvmField
    val EXPANSION_FORM_CONTENT: IElementType = IElementType("NG:EXPANSION_FORM_CONTENT", Angular2HtmlLanguage)

    @JvmField
    val EXPANSION_FORM_COMMA: IElementType = IElementType("NG:EXPANSION_FORM_COMMA", Angular2HtmlLanguage)
  }
}