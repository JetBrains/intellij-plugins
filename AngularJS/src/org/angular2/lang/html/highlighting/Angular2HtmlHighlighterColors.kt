// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

interface Angular2HtmlHighlighterColors {
  companion object {
    @JvmField
    val NG_BANANA_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
      "NG.BANANA_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)

    @JvmField
    val NG_EVENT_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
      "NG.EVENT_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)

    @JvmField
    val NG_INTERPOLATION_DELIMITER = TextAttributesKey.createTextAttributesKey(
      "NG.SCRIPT_DELIMITERS", DefaultLanguageHighlighterColors.SEMICOLON)

    @JvmField
    val NG_EXPANSION_FORM_DELIMITER = TextAttributesKey.createTextAttributesKey(
      "NG.EXPANSION_FORM_DELIMITERS", DefaultLanguageHighlighterColors.SEMICOLON)

    @JvmField
    val NG_PROPERTY_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
      "NG.PROPERTY_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)

    @JvmField
    val NG_REFERENCE_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
      "NG.REFERENCE_ATTR_NAME", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)

    @JvmField
    val NG_TEMPLATE_BINDINGS_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
      "NG.TEMPLATE_BINDINGS_ATTR_NAME", DefaultLanguageHighlighterColors.STATIC_FIELD)

    @JvmField
    val NG_TEMPLATE_LET_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
      "NG.TEMPLATE_VARIABLE_ATTR_NAME", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)

    @JvmField
    val NG_EXPRESSION = TextAttributesKey.createTextAttributesKey(
      "NG.EXPRESSIONS", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)

    @JvmField
    val NG_EXPANSION_FORM = TextAttributesKey.createTextAttributesKey(
      "NG.EXPANSION_FORM", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)

    @JvmField
    val NG_EXPANSION_FORM_COMMA = TextAttributesKey.createTextAttributesKey(
      "NG.EXPANSION_FORM_COMMA", DefaultLanguageHighlighterColors.COMMA)
  }
}