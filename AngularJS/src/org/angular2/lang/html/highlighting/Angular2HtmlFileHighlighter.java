// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.highlighting.JSHighlighter;
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.lexer.Angular2TokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.lang.javascript.highlighting.TypeScriptHighlighter.*;
import static com.intellij.openapi.editor.XmlHighlighterColors.HTML_CODE;
import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.containers.ContainerUtil.newArrayList;
import static org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors.*;
import static org.angular2.lang.html.highlighting.Angular2HtmlHighlightingLexer.EXPANSION_FORM_COMMA;
import static org.angular2.lang.html.highlighting.Angular2HtmlHighlightingLexer.EXPANSION_FORM_CONTENT;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.*;

class Angular2HtmlFileHighlighter extends HtmlFileHighlighter {

  private static final Map<IElementType, TextAttributesKey[]> keys = new HashMap<>();
  private static final JSHighlighter ourJsHighlighter = new JSHighlighter(DialectOptionHolder.JS_1_5);
  private static final TypeScriptHighlighter ourTsHighlighter = new TypeScriptHighlighter(false);
  private static final Map<Pair<TextAttributesKey, IElementType>, TextAttributesKey> ourTsKeyMap = new ConcurrentHashMap<>();

  private static void put(IElementType token, TextAttributesKey... keysArr) {
    keys.put(token, keysArr);
  }

  static {
    newArrayList(INTERPOLATION_START, INTERPOLATION_END).forEach(
      token -> put(token, HTML_CODE, NG_EXPRESSION, NG_INTERPOLATION_DELIMITER)
    );

    newArrayList(EXPANSION_FORM_START, EXPANSION_FORM_CASE_START, EXPANSION_FORM_END, EXPANSION_FORM_CASE_END).forEach(
      token -> put(token, HTML_CODE, NG_EXPANSION_FORM, NG_EXPANSION_FORM_DELIMITER)
    );

    put(EXPANSION_FORM_CONTENT, HTML_CODE, NG_EXPANSION_FORM);
    put(EXPANSION_FORM_COMMA, HTML_CODE, NG_EXPANSION_FORM, NG_EXPANSION_FORM_COMMA);

    newArrayList(
      pair(BANANA_BOX_BINDING, NG_BANANA_BINDING_ATTR_NAME),
      pair(EVENT, NG_EVENT_BINDING_ATTR_NAME),
      pair(PROPERTY_BINDING, NG_PROPERTY_BINDING_ATTR_NAME),
      pair(REFERENCE, NG_REFERENCE_ATTR_NAME),
      pair(TEMPLATE_BINDINGS, NG_TEMPLATE_BINDINGS_ATTR_NAME),
      pair(LET, NG_TEMPLATE_LET_ATTR_NAME)).forEach(
      p -> put(p.first, HTML_CODE, XmlHighlighterColors.HTML_TAG, XmlHighlighterColors.HTML_ATTRIBUTE_NAME, p.second)
    );

    newArrayList(Angular2TokenTypes.KEYWORDS.getTypes()).forEach(
      token -> put(token, HTML_CODE, NG_EXPRESSION, TS_KEYWORD)
    );

    newArrayList(
      pair(Angular2TokenTypes.ESCAPE_SEQUENCE, TS_VALID_STRING_ESCAPE),
      pair(Angular2TokenTypes.INVALID_ESCAPE_SEQUENCE, TS_INVALID_STRING_ESCAPE),
      pair(Angular2TokenTypes.XML_CHAR_ENTITY_REF, XmlHighlighterColors.HTML_ENTITY_REFERENCE),
      pair(JSTokenTypes.STRING_LITERAL_PART, TS_STRING)
    ).forEach(
      p -> put(p.first, HTML_CODE, NG_EXPRESSION, p.second)
    );
  }

  private final boolean myTokenizeExpansionForms;
  private final Pair<String, String> myInterpolationConfig;

  Angular2HtmlFileHighlighter(boolean tokenizeExpansionForms, Pair<String, String> interpolationConfig) {
    myTokenizeExpansionForms = tokenizeExpansionForms;
    myInterpolationConfig = interpolationConfig;
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    TextAttributesKey[] result = keys.get(tokenType);
    if (result != null) {
      return result;
    }
    result = super.getTokenHighlights(tokenType);
    if (tokenType.getLanguage() instanceof Angular2Language
        || tokenType.getLanguage() instanceof JavascriptLanguage) {
      result = ArrayUtil.insert(result, 1, NG_EXPRESSION);
    }
    return mapToTsKeys(result, tokenType);
  }

  @Override
  public @NotNull Lexer getHighlightingLexer() {
    return new Angular2HtmlHighlightingLexer(myTokenizeExpansionForms, myInterpolationConfig,
                                             FileTypeRegistry.getInstance().findFileTypeByName("CSS"));
  }

  private static TextAttributesKey @NotNull [] mapToTsKeys(TextAttributesKey @NotNull [] tokenHighlights, @NotNull IElementType tokenType) {
    return ContainerUtil.map2Array(tokenHighlights, TextAttributesKey.class, key -> getTsMappedKey(key, tokenType));
  }

  private static @NotNull TextAttributesKey getTsMappedKey(@NotNull TextAttributesKey key, @NotNull IElementType tokenType) {
    return !key.getExternalName().startsWith("JS.") //NON-NLS
           ? key
           : ourTsKeyMap.computeIfAbsent(pair(key, tokenType), p -> {
             TextAttributesKey[] jsHighlights = ourJsHighlighter.getTokenHighlights(p.second);
             TextAttributesKey[] tsHighlights = ourTsHighlighter.getTokenHighlights(p.second);
             TextAttributesKey jsKey = ArrayUtil.getLastElement(jsHighlights);
             TextAttributesKey tsKey = ArrayUtil.getLastElement(tsHighlights);
             return jsKey == p.first && tsKey != null ? tsKey : p.first;
           });
  }
}
