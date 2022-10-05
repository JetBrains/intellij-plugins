// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.html.HtmlParsing;
import com.intellij.psi.tree.ICustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.containers.Stack;
import com.intellij.xml.psi.XmlPsiBundle;
import com.intellij.xml.util.XmlUtil;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static org.angular2.web.Angular2WebSymbolsRegistryExtension.ATTR_NG_NON_BINDABLE;
import static org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType.*;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.*;

public class Angular2HtmlParsing extends HtmlParsing {

  private static final TokenSet CUSTOM_CONTENT = TokenSet.create(EXPANSION_FORM_START, INTERPOLATION_START,
                                                                 XML_DATA_CHARACTERS, XML_COMMA);

  private static final TokenSet DATA_TOKENS = TokenSet.create(XML_COMMA, XML_DATA_CHARACTERS);

  private final Stack<PsiBuilder.Marker> ngNonBindableTags = new Stack<>();

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
        error.error(XmlPsiBundle.message("xml.parsing.unescaped.ampersand.or.nonterminated.character.entity.reference"));
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

        tagEndError.error(XmlPsiBundle.message("xml.parsing.closing.tag.matches.nothing"));
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
      if (ngNonBindableTags.isEmpty()) {
        xmlText = terminateText(xmlText);
      }
      else {
        xmlText = startText(xmlText);
      }
      final PsiBuilder.Marker interpolation = mark();
      advance();
      if (token() == INTERPOLATION_EXPR) {
        advance();
      }

      if (ngNonBindableTags.isEmpty()) {
        if (token() == INTERPOLATION_END) {
          advance();
          interpolation.drop();
        }
        else {
          interpolation.error(Angular2Bundle.message("angular.parse.template.unterminated-interpolation"));
        }
      }
      else {
        if (token() == INTERPOLATION_END) {
          advance();
        }
        interpolation.collapse(XML_DATA_CHARACTERS);
      }
    }
    else if (tt == EXPANSION_FORM_START) {
      xmlText = terminateText(xmlText);
      parseExpansionForm();
    }
    else if (tt == XML_COMMA) {
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
  protected PsiBuilder.Marker closeTag() {
    if (!ngNonBindableTags.isEmpty()
        && ngNonBindableTags.peek() == peekTagMarker()) {
      ngNonBindableTags.pop();
    }
    return super.closeTag();
  }

  @Override
  protected void parseAttribute() {
    assert token() == XML_NAME;
    PsiBuilder.Marker att = mark();
    String tagName = XmlUtil.findLocalNameByQualifiedName(peekTagName());
    String attributeName = getBuilder().getTokenText();
    if (ATTR_NG_NON_BINDABLE.equals(attributeName)) {
      if (ngNonBindableTags.isEmpty()
          || ngNonBindableTags.peek() != peekTagMarker()) {
        ngNonBindableTags.push(peekTagMarker());
      }
    }
    final AttributeInfo attributeInfo = Angular2AttributeNameParser.parse(
      Objects.requireNonNull(attributeName), tagName);

    if (attributeInfo.error != null) {
      PsiBuilder.Marker attrName = mark();
      advance();
      attrName.error(attributeInfo.error);
    }
    else if (attributeInfo.type == Angular2AttributeType.REFERENCE) {
      PsiBuilder.Marker attrName = mark();
      advance();
      attrName.collapse(Angular2HtmlVarAttrTokenType.REFERENCE);
    }
    else if (attributeInfo.type == Angular2AttributeType.LET) {
      PsiBuilder.Marker attrName = mark();
      advance();
      attrName.collapse(Angular2HtmlVarAttrTokenType.LET);
    }
    else {
      advance();
    }
    IElementType attributeElementType = attributeInfo.type.getElementType();
    if (token() == XML_EQ) {
      advance();
      attributeElementType = parseAttributeValue(attributeElementType, attributeInfo.name);
    }
    att.done(attributeElementType != NG_CONTENT_SELECTOR ? attributeElementType : XML_ATTRIBUTE);
  }

  private IElementType parseAttributeValue(@NotNull IElementType attributeElementType, @NotNull String name) {
    final PsiBuilder.Marker attValue = mark();
    final IElementType contentType = getAttributeContentType(attributeElementType, name);
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

        if (tt == INTERPOLATION_EXPR && attributeElementType == XML_ATTRIBUTE) {
          attributeElementType = PROPERTY_BINDING;
        }

        if (tt == XmlTokenType.XML_BAD_CHARACTER) {
          final PsiBuilder.Marker error = mark();
          advance();
          error.error(XmlPsiBundle.message("xml.parsing.unescaped.ampersand.or.nonterminated.character.entity.reference"));
        }
        else if (tt == XmlTokenType.XML_ENTITY_REF_TOKEN) {
          parseReference();
        }
        else {
          advance();
        }
      }
      if (contentStart != null) {
        if (contentType == NG_CONTENT_SELECTOR) {
          contentStart.done(contentType);
        }
        else {
          contentStart.collapse(contentType);
        }
      }
      if (token() == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
        advance();
      }
      else {
        error(XmlPsiBundle.message("xml.parsing.unclosed.attribute.value"));
      }
    }
    else {
      if (token() != XmlTokenType.XML_TAG_END && token() != XmlTokenType.XML_EMPTY_ELEMENT_END) {
        if (contentType != null) {
          final PsiBuilder.Marker contentStart = mark();
          advance();
          if (contentType == NG_CONTENT_SELECTOR) {
            contentStart.done(contentType);
          }
          else {
            contentStart.collapse(contentType);
          }
        }
        else {
          advance(); // Single token att value
        }
      }
    }

    attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE);
    return attributeElementType;
  }

  private static IElementType getAttributeContentType(IElementType type, String name) {
    if (type == PROPERTY_BINDING || type == BANANA_BOX_BINDING) {
      return BINDING_EXPR;
    }
    if (type == EVENT) {
      return ACTION_EXPR;
    }
    if (type == TEMPLATE_BINDINGS) {
      return createTemplateBindings(name);
    }
    if (type == NG_CONTENT_SELECTOR) {
      return NG_CONTENT_SELECTOR;
    }
    if (type == REFERENCE || type == LET || type == XML_ATTRIBUTE) {
      return null;
    }
    throw new IllegalStateException("Unsupported element type: " + type);
  }

  private void parseExpansionForm() {
    assert token() == EXPANSION_FORM_START;
    PsiBuilder.Marker expansionForm = mark();

    advance();

    if (!remapTokensUntilComma(BINDING_EXPR)/*switch value*/
        || !remapTokensUntilComma(XML_DATA_CHARACTERS)/*type*/) {
      markCriticalExpansionFormProblem(expansionForm);
      return;
    }

    skipRealWhiteSpaces();
    boolean first = true;
    while (token() == XML_DATA_CHARACTERS || token() == EXPANSION_FORM_CASE_START) {
      if (!parseExpansionFormCaseContent() && first) {
        markCriticalExpansionFormProblem(expansionForm);
        return;
      }
      first = false;
      skipRealWhiteSpaces();
    }
    if (token() != EXPANSION_FORM_END) {
      expansionForm
        .error(Angular2Bundle.message("angular.parse.template.unterminated-expansion-form"));
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
    assert token() == EXPANSION_FORM_START;
    advance(); //consume LBRACE
    expansionForm.error(Angular2Bundle.message("angular.parse.template.unterminated-expansion-form-critical"));
  }

  private boolean remapTokensUntilComma(IElementType textType) {
    PsiBuilder.Marker start = mark();
    while (!eof() && token() != XML_COMMA) {
      advance();
    }
    start.collapse(textType);
    if (token() != XML_COMMA) {
      start.precede().error(Angular2Bundle.message("angular.parse.template.invalid-icu-message-expected-comma"));
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
      if (token() != EXPANSION_FORM_CASE_START) {
        expansionFormCase.error(Angular2Bundle.message("angular.parse.template.invalid-icu-message-expected-left-brace"));
        expansionFormCase.precede().done(EXPANSION_FORM_CASE);
        return false;
      }
    }
    else if (token() == EXPANSION_FORM_CASE_START) {
      advance();
      expansionFormCase.error(Angular2Bundle.message("angular.parse.template.invalid-icu-message-missing-case-value"));
      expansionFormCase = expansionFormCase.precede();
    }
    else {
      throw new IllegalStateException();
    }
    advance();
    PsiBuilder.Marker content = mark();
    int level = 1;
    IElementType tt;
    while ((tt = token()) != EXPANSION_FORM_CASE_END || level > 1) {
      if (tt == EXPANSION_FORM_CASE_START) {
        level++;
      }
      else if (tt == EXPANSION_FORM_CASE_END) {
        level--;
      }
      else if (tt == null) {
        content.error(Angular2Bundle.message("angular.parse.template.invalid-icu-message-missing-right-brace"));
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
