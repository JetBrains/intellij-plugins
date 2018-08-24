// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.*;

class Angular2HtmlFileHighlighter extends HtmlFileHighlighter {

  public static final TextAttributesKey NG_BANANA_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.BANANA_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

  public static final TextAttributesKey NG_EVENT_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.EVENT_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

  public static final TextAttributesKey NG_SCRIPT_DELIMITERS = TextAttributesKey.createTextAttributesKey(
    "NG.SCRIPT_DELIMITERS", DefaultLanguageHighlighterColors.SEMICOLON);

  public static final TextAttributesKey NG_PROPERTY_BINDING_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.PROPERTY_BINDING_ATTR_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

  public static final TextAttributesKey NG_REFERENCE_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.REFERENCE_ATTR_NAME", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

  public static final TextAttributesKey NG_TEMPLATE_BINDINGS_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.TEMPLATE_BINDINGS_ATTR_NAME", DefaultLanguageHighlighterColors.STATIC_METHOD);

  public static final TextAttributesKey NG_TEMPLATE_VARIABLE_ATTR_NAME = TextAttributesKey.createTextAttributesKey(
    "NG.TEMPLATE_VARIABLE_ATTR_NAME", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

  private static final Map<IElementType, TextAttributesKey> keys1;
  private static final Map<IElementType, TextAttributesKey> keys2;

  static {
    keys1 = new HashMap<>();
    keys2 = new HashMap<>();

    keys2.put(INTERPOLATION_START, NG_SCRIPT_DELIMITERS);
    keys2.put(INTERPOLATION_END, NG_SCRIPT_DELIMITERS);

    Stream.of(BANANA_BOX_BINDING, EVENT, PROPERTY_BINDING, REFERENCE, TEMPLATE_BINDINGS, VARIABLE)
      .forEach(type -> keys1.put(type, XmlHighlighterColors.HTML_ATTRIBUTE_NAME));

    keys2.put(BANANA_BOX_BINDING, NG_BANANA_BINDING_ATTR_NAME);
    keys2.put(EVENT, NG_EVENT_BINDING_ATTR_NAME);
    keys2.put(PROPERTY_BINDING, NG_PROPERTY_BINDING_ATTR_NAME);
    keys2.put(REFERENCE, NG_REFERENCE_ATTR_NAME);
    keys2.put(TEMPLATE_BINDINGS, NG_TEMPLATE_BINDINGS_ATTR_NAME);
    keys2.put(VARIABLE, NG_TEMPLATE_VARIABLE_ATTR_NAME);

    registerEmbeddedTokenAttributes(keys1, keys2);
  }

  private final boolean myTokenizeExpansionForms;
  private final Pair<String, String> myInterpolationConfig;

  public Angular2HtmlFileHighlighter(boolean tokenizeExpansionForms, Pair<String, String> interpolationConfig) {
    myTokenizeExpansionForms = tokenizeExpansionForms;
    myInterpolationConfig = interpolationConfig;
  }


  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new Angular2HtmlHighlightingLexer(myTokenizeExpansionForms, myInterpolationConfig);
  }

}
