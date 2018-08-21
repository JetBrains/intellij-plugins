// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.codeInsight.daemon.XmlErrorMessages;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.html.HtmlParsing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.ICustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType.*;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.*;

public class Angular2HtmlParsing extends HtmlParsing {

  private static final TokenSet CUSTOM_CONTENT = TokenSet.create(LBRACE, RBRACE, INTERPOLATION_START, XML_COMMA, XML_DATA_CHARACTERS);
  private static final TokenSet DATA_TOKENS = TokenSet.create(RBRACE, XML_COMMA, XML_DATA_CHARACTERS);

  public Angular2HtmlParsing(@NotNull PsiBuilder builder) {
    super(builder);
  }

  public void parseExpansionFormContent() {
    PsiBuilder.Marker expansionFormContent = mark();
    PsiBuilder.Marker xmlText = null;
    while (!eof()) {
      final IElementType tt = token();
      if (tt == XmlTokenType.XML_START_TAG_START) {
        xmlText = terminateText(xmlText);
        parseTag();
      }
      else if (tt == XmlTokenType.XML_PI_START) {
        xmlText = terminateText(xmlText);
        parseProcessingInstruction();
      }
      else if (tt == XmlTokenType.XML_CHAR_ENTITY_REF || tt == XmlTokenType.XML_ENTITY_REF_TOKEN) {
        xmlText = startText(xmlText);
        parseReference();
      }
      else if (tt == XmlTokenType.XML_CDATA_START) {
        xmlText = startText(xmlText);
        parseCData();
      }
      else if (tt == XmlTokenType.XML_COMMENT_START) {
        xmlText = startText(xmlText);
        parseComment();
      }
      else if (tt == XmlTokenType.XML_BAD_CHARACTER) {
        xmlText = startText(xmlText);
        final PsiBuilder.Marker error = mark();
        advance();
        error.error(XmlErrorMessages.message("unescaped.ampersand.or.nonterminated.character.entity.reference"));
      }
      else if (tt == XmlTokenType.XML_END_TAG_START) {
        final PsiBuilder.Marker tagEndError = mark();

        advance();
        if (token() == XmlTokenType.XML_NAME) {
          advance();
          if (token() == XmlTokenType.XML_TAG_END) {
            advance();
          }
        }

        tagEndError.error(XmlErrorMessages.message("xml.parsing.closing.tag.matches.nothing"));
      }
      else if (tt instanceof ICustomParsingType || tt instanceof ILazyParseableElementType) {
        xmlText = terminateText(xmlText);
        advance();
      }
      else if (hasCustomTagContent()) {
        xmlText = parseCustomTagContent(xmlText);
      }
      else {
        xmlText = startText(xmlText);
        advance();
      }
    }
    terminateText(xmlText);
    expansionFormContent.done(EXPANSION_FORM_CASE_CONTENT);
  }

  @Override
  protected boolean hasCustomTopLevelContent() {
    return CUSTOM_CONTENT.contains(token());
  }

  @Override
  protected boolean hasCustomTagContent() {
    return CUSTOM_CONTENT.contains(token());
  }

  @Override
  protected PsiBuilder.Marker parseCustomTagContent(PsiBuilder.Marker xmlText) {
    final IElementType tt = token();
    if (tt == INTERPOLATION_START) {
      xmlText = terminateText(xmlText);
      final PsiBuilder.Marker interpolation = mark();
      advance();
      if (token() == INTERPOLATION_EXPR) {
        advance();
      }
      if (token() == INTERPOLATION_END) {
        advance();
        interpolation.drop();
      }
      else {
        interpolation.error("Unterminated interpolation");
      }
    }
    else if (tt == LBRACE) {
      xmlText = terminateText(xmlText);
      parseExpansionForm();
    }
    else if (tt == RBRACE || tt == XML_COMMA) {
      xmlText = startText(xmlText);
      getBuilder().remapCurrentToken(XML_DATA_CHARACTERS);
      advance();
    }
    else if (tt == XML_DATA_CHARACTERS) {
      xmlText = startText(xmlText);
      PsiBuilder.Marker dataStart = mark();
      while (DATA_TOKENS.contains(token())) {
        advance();
      }
      dataStart.collapse(XML_DATA_CHARACTERS);
    }
    return xmlText;
  }

  @Override
  protected PsiBuilder.Marker parseCustomTopLevelContent(PsiBuilder.Marker error) {
    error = flushError(error);
    terminateText(parseCustomTagContent(null));
    return error;
  }

  @Override
  protected void parseAttribute() {
    assert token() == XML_NAME;
    PsiBuilder.Marker att = mark();
    final String attributeName = normalizeAttributeName(
      Objects.requireNonNull(getBuilder().getTokenText()));
    final Pair<IElementType, String> attributeElementTypeAndError = parseAttributeName(attributeName);

    if (attributeElementTypeAndError.second != null) {
      PsiBuilder.Marker attrName = mark();
      advance();
      attrName.error(attributeElementTypeAndError.second);
    }
    else {
      advance();
    }
    IElementType attributeElementType = attributeElementTypeAndError.first;
    if (token() == XML_EQ) {
      advance();
      attributeElementType = parseAttributeValue(attributeElementType);
    }
    att.done(attributeElementType);
  }

  @NotNull
  public static String normalizeAttributeName(@NotNull String name) {
    return StringUtil.trimStart(name, "data-");
  }

  private Pair<IElementType, String> parseAttributeName(@NotNull String name) {
    return parseAttributeName(name, "ng-template".contentEquals(XmlUtil.getLocalName(peekTagName())));
  }

  @NotNull
  public static Pair<IElementType, String> parseAttributeName(@NotNull String name, boolean isInTemplateTag) {
    if (name.startsWith("bindon-")) {
      return parseBananaBoxBinding(name.substring(7));
    }
    else if (name.startsWith("[(") && name.endsWith(")]")) {
      return parseBananaBoxBinding(name.substring(2, name.length() - 2));
    }
    else if (name.startsWith("bind-")) {
      return parsePropertyBinding(name.substring(5));
    }
    else if (name.startsWith("[") && name.endsWith("]")) {
      return parsePropertyBinding(name.substring(1, name.length() - 1));
    }
    else if (name.startsWith("on-")) {
      return parseEvent(name.substring(3));
    }
    else if (name.startsWith("(") && name.endsWith(")")) {
      return parseEvent(name.substring(1, name.length() - 1));
    }
    else if (name.startsWith("*")) {
      return parseTemplateBindings(name.substring(1));
    }
    else if (name.startsWith("let-")) {
      return parseVariable(name.substring(4), isInTemplateTag);
    }
    else if (name.startsWith("#")) {
      return parseReference(name.substring(1));
    }
    else if (name.startsWith("ref-")) {
      return parseReference(name.substring(4));
    }
    else if (name.startsWith("@")) {
      return parsePropertyBinding(name.substring(1));
    }
    return pair(XML_ATTRIBUTE, null);
  }

  @NotNull
  private static Pair<IElementType, String> parseBananaBoxBinding(@NotNull String name) {
    return pair(BANANA_BOX_BINDING, null);
  }

  @NotNull
  private static Pair<IElementType, String> parsePropertyBinding(@NotNull String name) {
    return pair(PROPERTY_BINDING, null);
  }

  @NotNull
  private static Pair<IElementType, String> parseEvent(@NotNull String name) {
    if (isAnimationLabel(name)) {
      return parseAnimationEvent(name.substring(1));
    }
    return pair(EVENT, null);
  }

  @NotNull
  private static Pair<IElementType, String> parseTemplateBindings(@NotNull String name) {
    return pair(TEMPLATE_BINDINGS, null);
  }

  @NotNull
  private static Pair<IElementType, String> parseAnimationEvent(@NotNull String name) {
    int dot = name.indexOf('.');
    if (dot < 0) {
      return pair(ANIMATION_EVENT, "The animation trigger output event (@" + name +
                                   ") is missing its phase value name (start or done are currently supported)");
    }
    else {
      @SuppressWarnings("StringToUpperCaseOrToLowerCaseWithoutLocale")
      String phase = name.substring(dot + 1).toLowerCase();
      if (!"start".equals(phase) && !"done".equals(phase)) {
        return pair(ANIMATION_EVENT, "The provided animation output phase value '" + phase +
                                     "' for '@" + name.substring(0, dot) +
                                     "' is not supported (use start or done))");
      }
    }
    return pair(ANIMATION_EVENT, null);
  }

  @NotNull
  private static Pair<IElementType, String> parseVariable(@NotNull String varName, boolean isInTemplateTag) {
    if (!isInTemplateTag) {
      return pair(XML_ATTRIBUTE, "\"let-\" is only supported on ng-template elements.");
    }
    else if (varName.contains("-")) {
      return pair(XML_ATTRIBUTE, "\"-\" is not allowed in variable names");
    }
    return pair(VARIABLE, null);
  }

  @NotNull
  private static Pair<IElementType, String> parseReference(@NotNull String refName) {
    if (refName.contains("-")) {
      return pair(XML_ATTRIBUTE, "\"-\" is not allowed in reference names");
    }
    return pair(REFERENCE, null);
  }

  private static boolean isAnimationLabel(@NotNull String name) {
    return name.startsWith("@");
  }

  private IElementType parseAttributeValue(@NotNull IElementType attributeElementType) {
    final PsiBuilder.Marker attValue = mark();
    final IElementType contentType = getAttributeContentType(attributeElementType);
    if (token() == XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER) {
      advance();
      final PsiBuilder.Marker contentStart = contentType != null ? mark() : null;
      while (true) {
        final IElementType tt = token();
        if (tt == null || tt == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER || tt == XmlTokenType.XML_END_TAG_START || tt == XmlTokenType
          .XML_EMPTY_ELEMENT_END ||
            tt == XmlTokenType.XML_START_TAG_START) {
          break;
        }

        if (tt == INTERPOLATION_EXPR) {
          attributeElementType = PROPERTY_BINDING;
        }

        if (tt == XmlTokenType.XML_BAD_CHARACTER) {
          final PsiBuilder.Marker error = mark();
          advance();
          error.error(XmlErrorMessages.message("unescaped.ampersand.or.nonterminated.character.entity.reference"));
        }
        else if (tt == XmlTokenType.XML_ENTITY_REF_TOKEN) {
          parseReference();
        }
        else {
          advance();
        }
      }
      if (contentStart != null) {
        contentStart.collapse(contentType);
      }
      if (token() == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
        advance();
      }
      else {
        error(XmlErrorMessages.message("xml.parsing.unclosed.attribute.value"));
      }
    }
    else {
      if (token() != XmlTokenType.XML_TAG_END && token() != XmlTokenType.XML_EMPTY_ELEMENT_END) {
        if (contentType != null) {
          final PsiBuilder.Marker contentStart = mark();
          advance();
          contentStart.collapse(contentType);
        }
        else {
          advance(); // Single token att value
        }
      }
    }

    attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE);
    return attributeElementType;
  }

  private static IElementType getAttributeContentType(IElementType type) {
    if (type == PROPERTY_BINDING) {
      return BINDING_EXPR;
    }
    if (type == EVENT || type == ANIMATION_EVENT) {
      return ACTION_EXPR;
    }
    if (type == TEMPLATE_BINDINGS) {
      return createTemplateBindings("$templateKey"); //TODO use real key here
    }
    if (type == REFERENCE || type == VARIABLE || type == XML_ATTRIBUTE) {
      return null;
    }
    throw new IllegalStateException("Unsupported element type: " + type);
  }

  private void parseExpansionForm() {
    assert token() == LBRACE;
    PsiBuilder.Marker expansionForm = mark();

    advance();

    if (!remapTokensUntilComma(BINDING_EXPR)/*switch value*/
        || !remapTokensUntilComma(XML_DATA_CHARACTERS)/*type*/) {
      markCriticalExpansionFormProblem(expansionForm);
      return;
    }

    skipRealWhiteSpaces();
    boolean first = true;
    while (token() == XML_DATA_CHARACTERS || token() == LBRACE) {
      if (!parseExpansionFormCaseContent() && first) {
        markCriticalExpansionFormProblem(expansionForm);
        return;
      }
      first = false;
      skipRealWhiteSpaces();
    }
    if (token() != RBRACE) {
      expansionForm
        .error("Unterminated expansion form.");
      expansionForm = expansionForm.precede();
    }
    else {
      advance();
    }
    expansionForm.done(EXPANSION_FORM);
  }

  private void markCriticalExpansionFormProblem(PsiBuilder.Marker expansionForm) {
    // critical problem, most likely not an expansion form at all
    expansionForm.rollbackTo();
    expansionForm = mark();
    assert token() == LBRACE;
    advance(); //consume LBRACE
    expansionForm
      .error("Unterminated expansion form. Do you have an unescaped \"{\" in your template? Use \"{{ '{' }}\") to escape it.");
  }

  private boolean remapTokensUntilComma(IElementType textType) {
    PsiBuilder.Marker start = mark();
    while (!eof() && token() != XML_COMMA) {
      advance();
    }
    start.collapse(textType);
    if (token() != XML_COMMA) {
      start.precede().error("Invalid ICU message. Expected ','.");
      return false;
    }
    advance();
    return true;
  }

  private boolean parseExpansionFormCaseContent() {
    PsiBuilder.Marker expansionFormCase = mark();
    if (token() == XML_DATA_CHARACTERS) {
      advance(); // value
      skipRealWhiteSpaces();
      if (token() != LBRACE) {
        expansionFormCase.error("Invalid ICU message. Missing '{'.");
        expansionFormCase.precede().done(EXPANSION_FORM_CASE);
        return false;
      }
    }
    else if (token() == LBRACE) {
      advance();
      expansionFormCase.error("Invalid ICU message. Missing case value.");
      expansionFormCase = expansionFormCase.precede();
    }
    else {
      throw new IllegalStateException();
    }
    advance();
    PsiBuilder.Marker content = mark();
    int level = 1;
    IElementType tt;
    while ((tt = token()) != RBRACE || level > 1) {
      if (tt == LBRACE) {
        level++;
      }
      else if (tt == RBRACE) {
        level--;
      }
      else if (tt == null) {
        content.error("Invalid ICU message. Missing '}'.");
        expansionFormCase.done(EXPANSION_FORM_CASE);
        return false;
      }
      advance();
    }
    content.collapse(Angular2ExpansionFormCaseContentTokenType.INSTANCE);
    advance();
    expansionFormCase.done(EXPANSION_FORM_CASE);
    return true;
  }

  private void skipRealWhiteSpaces() {
    while (token() == XML_REAL_WHITE_SPACE) {
      advance();
    }
  }
}
