// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public interface Angular2HtmlHighlighterColors {

  TextAttributesKey NG_BANANA_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.BANANA_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

  TextAttributesKey NG_EVENT_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.EVENT_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

  TextAttributesKey NG_INTERPOLATION_DELIMITER = TextAttributesKey.createTextAttributesKey(
    "NG.SCRIPT_DELIMITERS", DefaultLanguageHighlighterColors.SEMICOLON);

  TextAttributesKey NG_EXPANSION_FORM_DELIMITER = TextAttributesKey.createTextAttributesKey(
    "NG.EXPANSION_FORM_DELIMITERS", DefaultLanguageHighlighterColors.SEMICOLON);

  TextAttributesKey NG_PROPERTY_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.PROPERTY_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

  TextAttributesKey NG_REFERENCE_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.REFERENCE_ATTR_NAME", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

  TextAttributesKey NG_TEMPLATE_BINDINGS_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.TEMPLATE_BINDINGS_ATTR_NAME", DefaultLanguageHighlighterColors.STATIC_FIELD);

  TextAttributesKey NG_TEMPLATE_LET_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.TEMPLATE_VARIABLE_ATTR_NAME", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

  TextAttributesKey NG_EXPRESSION = TextAttributesKey.createTextAttributesKey(
    "NG.EXPRESSIONS", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR);

  TextAttributesKey NG_EXPANSION_FORM = TextAttributesKey.createTextAttributesKey(
    "NG.EXPANSION_FORM", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR);

  TextAttributesKey NG_EXPANSION_FORM_COMMA = TextAttributesKey.createTextAttributesKey(
    "NG.EXPANSION_FORM_COMMA", DefaultLanguageHighlighterColors.COMMA);
}
