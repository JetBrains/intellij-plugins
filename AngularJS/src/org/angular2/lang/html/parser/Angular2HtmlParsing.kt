// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.tree.ICustomParsingType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILazyParseableElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.containers.Stack
import com.intellij.xml.psi.XmlPsiBundle
import com.intellij.xml.util.XmlUtil
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType.Companion.createTemplateBindings
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes
import org.angular2.web.Angular2WebSymbolsQueryConfigurator

class Angular2HtmlParsing(builder: PsiBuilder) : HtmlParsing(builder) {
  private val ngNonBindableTags = Stack<PsiBuilder.Marker>()
  fun parseExpansionFormContent() {
    val expansionFormContent = mark()
    var xmlText: PsiBuilder.Marker? = null
    while (!eof()) {
      when (token()) {
        XmlTokenType.XML_START_TAG_START -> {
          xmlText = terminateText(xmlText)
          parseTag()
        }
        XmlTokenType.XML_PI_START -> {
          xmlText = terminateText(xmlText)
          parseProcessingInstruction()
        }
        XmlTokenType.XML_CHAR_ENTITY_REF, XmlTokenType.XML_ENTITY_REF_TOKEN -> {
          xmlText = startText(xmlText)
          parseReference()
        }
        XmlTokenType.XML_CDATA_START -> {
          xmlText = startText(xmlText)
          parseCData()
        }
        XmlTokenType.XML_COMMENT_START -> {
          xmlText = startText(xmlText)
          parseComment()
        }
        XmlTokenType.XML_BAD_CHARACTER -> {
          xmlText = startText(xmlText)
          val error = mark()
          advance()
          error.error(XmlPsiBundle.message("xml.parsing.unescaped.ampersand.or.nonterminated.character.entity.reference"))
        }
        XmlTokenType.XML_END_TAG_START -> {
          val tagEndError = mark()
          advance()
          if (token() === XmlTokenType.XML_NAME) {
            advance()
            if (token() === XmlTokenType.XML_TAG_END) {
              advance()
            }
          }
          tagEndError.error(XmlPsiBundle.message("xml.parsing.closing.tag.matches.nothing"))
        }
        is ICustomParsingType, is ILazyParseableElementType -> {
          xmlText = terminateText(xmlText)
          advance()
        }
        else -> {
          if (hasCustomTagContent()) {
            xmlText = parseCustomTagContent(xmlText)
          }
          else {
            xmlText = startText(xmlText)
            advance()
          }
        }
      }
    }
    terminateText(xmlText)
    expansionFormContent.done(Angular2HtmlElementTypes.EXPANSION_FORM_CASE_CONTENT)
  }

  override fun hasCustomTopLevelContent(): Boolean {
    return CUSTOM_CONTENT.contains(token())
  }

  override fun hasCustomTagContent(): Boolean {
    return CUSTOM_CONTENT.contains(token())
  }

  override fun parseCustomTagContent(xmlText: PsiBuilder.Marker?): PsiBuilder.Marker? {
    var result = xmlText
    when (token()) {
      Angular2HtmlTokenTypes.INTERPOLATION_START -> {
        result = if (ngNonBindableTags.isEmpty()) {
          terminateText(result)
        }
        else {
          startText(result)
        }
        val interpolation = mark()
        advance()
        if (token() === Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR) {
          advance()
        }
        if (ngNonBindableTags.isEmpty()) {
          if (token() === Angular2HtmlTokenTypes.INTERPOLATION_END) {
            advance()
            interpolation.drop()
          }
          else {
            interpolation.error(Angular2Bundle.message("angular.parse.template.unterminated-interpolation"))
          }
        }
        else {
          if (token() === Angular2HtmlTokenTypes.INTERPOLATION_END) {
            advance()
          }
          interpolation.collapse(XmlTokenType.XML_DATA_CHARACTERS)
        }
      }
      Angular2HtmlTokenTypes.EXPANSION_FORM_START -> {
        result = terminateText(result)
        parseExpansionForm()
      }
      XmlTokenType.XML_COMMA -> {
        result = startText(result)
        builder.remapCurrentToken(XmlTokenType.XML_DATA_CHARACTERS)
        advance()
      }
      XmlTokenType.XML_DATA_CHARACTERS -> {
        result = startText(result)
        val dataStart = mark()
        while (DATA_TOKENS.contains(token())) {
          advance()
        }
        dataStart.collapse(XmlTokenType.XML_DATA_CHARACTERS)
      }
    }
    return result
  }

  override fun parseCustomTopLevelContent(error: PsiBuilder.Marker?): PsiBuilder.Marker? {
    val result = flushError(error)
    terminateText(parseCustomTagContent(null))
    return result
  }

  override fun closeTag(): PsiBuilder.Marker {
    if (!ngNonBindableTags.isEmpty()
        && ngNonBindableTags.peek() === peekTagMarker()) {
      ngNonBindableTags.pop()
    }
    return super.closeTag()
  }

  override fun parseAttribute() {
    assert(token() === XmlTokenType.XML_NAME)
    val att = mark()
    val tagName = XmlUtil.findLocalNameByQualifiedName(peekTagName())
    val attributeName = builder.tokenText
    if (Angular2WebSymbolsQueryConfigurator.ATTR_NG_NON_BINDABLE == attributeName) {
      if (ngNonBindableTags.isEmpty()
          || ngNonBindableTags.peek() !== peekTagMarker()) {
        ngNonBindableTags.push(peekTagMarker())
      }
    }
    val attributeInfo = Angular2AttributeNameParser.parse(attributeName!!, tagName!!)
    if (attributeInfo.error != null) {
      val attrName = mark()
      advance()
      attrName.error(attributeInfo.error)
    }
    else if (attributeInfo.type == Angular2AttributeType.REFERENCE) {
      val attrName = mark()
      advance()
      attrName.collapse(Angular2HtmlVarAttrTokenType.REFERENCE)
    }
    else if (attributeInfo.type == Angular2AttributeType.LET) {
      val attrName = mark()
      advance()
      attrName.collapse(Angular2HtmlVarAttrTokenType.LET)
    }
    else {
      advance()
    }
    var attributeElementType = attributeInfo.type.elementType
    if (token() === XmlTokenType.XML_EQ) {
      advance()
      attributeElementType = parseAttributeValue(attributeElementType, attributeInfo.name)
    }
    att.done(
      if (attributeElementType !== Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR)
        attributeElementType
      else
        XmlElementType.XML_ATTRIBUTE
    )
  }

  private fun parseAttributeValue(attributeElementType: IElementType, name: String): IElementType {
    var result = attributeElementType
    val attValue = mark()
    val contentType = getAttributeContentType(result, name)
    if (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER) {
      advance()
      val contentStart = if (contentType != null) mark() else null
      while (true) {
        val tt = token()
        if (tt == null
            || tt === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER
            || tt === XmlTokenType.XML_END_TAG_START
            || tt === XmlTokenType.XML_EMPTY_ELEMENT_END
            || tt === XmlTokenType.XML_START_TAG_START) {
          break
        }
        if (tt === Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR && result === XmlElementType.XML_ATTRIBUTE) {
          result = Angular2HtmlElementTypes.PROPERTY_BINDING
        }
        when (tt) {
          XmlTokenType.XML_BAD_CHARACTER -> {
            val error = mark()
            advance()
            error.error(XmlPsiBundle.message("xml.parsing.unescaped.ampersand.or.nonterminated.character.entity.reference"))
          }
          XmlTokenType.XML_ENTITY_REF_TOKEN -> {
            parseReference()
          }
          else -> {
            advance()
          }
        }
      }
      if (contentStart != null) {
        if (contentType === Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR) {
          contentStart.done(contentType)
        }
        else {
          contentStart.collapse(contentType!!)
        }
      }
      if (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
        advance()
      }
      else {
        error(XmlPsiBundle.message("xml.parsing.unclosed.attribute.value"))
      }
    }
    else {
      if (token().let { it !== XmlTokenType.XML_TAG_END && it !== XmlTokenType.XML_EMPTY_ELEMENT_END }) {
        if (contentType != null) {
          val contentStart = mark()
          advance()
          if (contentType === Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR) {
            contentStart.done(contentType)
          }
          else {
            contentStart.collapse(contentType)
          }
        }
        else {
          advance() // Single token att value
        }
      }
    }
    attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE)
    return result
  }

  private fun parseExpansionForm() {
    assert(token() === Angular2HtmlTokenTypes.EXPANSION_FORM_START)
    var expansionForm = mark()
    advance()
    if (!remapTokensUntilComma(Angular2EmbeddedExprTokenType.BINDING_EXPR) /*switch value*/
        || !remapTokensUntilComma(XmlTokenType.XML_DATA_CHARACTERS) /*type*/) {
      markCriticalExpansionFormProblem(expansionForm)
      return
    }
    skipRealWhiteSpaces()
    var first = true
    while (token().let { it === XmlTokenType.XML_DATA_CHARACTERS || it === Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_START }) {
      if (!parseExpansionFormCaseContent() && first) {
        markCriticalExpansionFormProblem(expansionForm)
        return
      }
      first = false
      skipRealWhiteSpaces()
    }
    if (token() !== Angular2HtmlTokenTypes.EXPANSION_FORM_END) {
      expansionForm
        .error(Angular2Bundle.message("angular.parse.template.unterminated-expansion-form"))
      expansionForm = expansionForm.precede()
    }
    else {
      advance()
    }
    expansionForm.done(Angular2HtmlElementTypes.EXPANSION_FORM)
  }

  private fun markCriticalExpansionFormProblem(expansionForm: PsiBuilder.Marker) {
    // critical problem, most likely not an expansion form at all
    expansionForm.rollbackTo()
    val errorMarker = mark()
    assert(token() === Angular2HtmlTokenTypes.EXPANSION_FORM_START)
    advance() //consume LBRACE
    errorMarker.error(Angular2Bundle.message("angular.parse.template.unterminated-expansion-form-critical"))
  }

  private fun remapTokensUntilComma(textType: IElementType): Boolean {
    val start = mark()
    while (!eof() && token() !== XmlTokenType.XML_COMMA) {
      advance()
    }
    start.collapse(textType)
    if (token() !== XmlTokenType.XML_COMMA) {
      start.precede().error(Angular2Bundle.message("angular.parse.template.invalid-icu-message-expected-comma"))
      return false
    }
    advance()
    return true
  }

  private fun parseExpansionFormCaseContent(): Boolean {
    var expansionFormCase = mark()
    if (token() === XmlTokenType.XML_DATA_CHARACTERS) {
      advance() // value
      skipRealWhiteSpaces()
      if (token() !== Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_START) {
        expansionFormCase.error(Angular2Bundle.message("angular.parse.template.invalid-icu-message-expected-left-brace"))
        expansionFormCase.precede().done(Angular2HtmlElementTypes.EXPANSION_FORM_CASE)
        return false
      }
    }
    else if (token() === Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_START) {
      advance()
      expansionFormCase.error(Angular2Bundle.message("angular.parse.template.invalid-icu-message-missing-case-value"))
      expansionFormCase = expansionFormCase.precede()
    }
    else {
      throw IllegalStateException()
    }
    advance()
    val content = mark()
    var level = 1
    var tt: IElementType?
    while (token().also { tt = it } !== Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_END || level > 1) {
      when (tt) {
        Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_START -> {
          level++
        }
        Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_END -> {
          level--
        }
        null -> {
          content.error(Angular2Bundle.message("angular.parse.template.invalid-icu-message-missing-right-brace"))
          expansionFormCase.done(Angular2HtmlElementTypes.EXPANSION_FORM_CASE)
          return false
        }
      }
      advance()
    }
    content.collapse(Angular2ExpansionFormCaseContentTokenType.INSTANCE)
    advance()
    expansionFormCase.done(Angular2HtmlElementTypes.EXPANSION_FORM_CASE)
    return true
  }

  private fun skipRealWhiteSpaces() {
    while (token() === XmlTokenType.XML_REAL_WHITE_SPACE) {
      advance()
    }
  }

  companion object {
    private val CUSTOM_CONTENT = TokenSet.create(Angular2HtmlTokenTypes.EXPANSION_FORM_START,
                                                 Angular2HtmlTokenTypes.INTERPOLATION_START,
                                                 XmlTokenType.XML_DATA_CHARACTERS, XmlTokenType.XML_COMMA)
    private val DATA_TOKENS = TokenSet.create(XmlTokenType.XML_COMMA, XmlTokenType.XML_DATA_CHARACTERS)
    private fun getAttributeContentType(type: IElementType, name: String): IElementType? =
      when (type) {
        Angular2HtmlElementTypes.PROPERTY_BINDING, Angular2HtmlElementTypes.BANANA_BOX_BINDING -> {
          Angular2EmbeddedExprTokenType.BINDING_EXPR
        }
        Angular2HtmlElementTypes.EVENT -> {
          Angular2EmbeddedExprTokenType.ACTION_EXPR
        }
        Angular2HtmlElementTypes.TEMPLATE_BINDINGS -> {
          createTemplateBindings(name)
        }
        Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR -> {
          Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR
        }
        Angular2HtmlElementTypes.REFERENCE, Angular2HtmlElementTypes.LET, XmlElementType.XML_ATTRIBUTE -> {
          null
        }
        else -> {
          throw IllegalStateException("Unsupported element type: $type")
        }
      }

  }
}