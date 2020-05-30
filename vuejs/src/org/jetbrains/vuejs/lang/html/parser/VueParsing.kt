// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.codeInsight.daemon.XmlErrorBundle
import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.xml.psi.XmlPsiBundle
import com.intellij.xml.util.HtmlUtil.*
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.*
import org.jetbrains.vuejs.lang.expr.parser.VueJSEmbeddedExprTokenType
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_EXPR
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START
import org.jetbrains.vuejs.model.SLOT_TAG_NAME
import java.util.*

class VueParsing(builder: PsiBuilder) : HtmlParsing(builder) {

  override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
    // There are heavily-used Vue components called like 'Col' or 'Input'. Unlike HTML tags <col> and <input> Vue components do have closing tags.
    // The following 'if' is a little bit hacky but it's rather tricky to solve the problem in a better way at parser level.
    if (tagName != originalTagName) {
      return false
    }
    return super.isSingleTag(tagName, originalTagName)
  }

  override fun hasCustomTagContent(): Boolean {
    return token() === INTERPOLATION_START
  }

  override fun hasCustomTopLevelContent(): Boolean {
    return token() === INTERPOLATION_START
  }

  override fun parseCustomTagContent(xmlText: PsiBuilder.Marker?): PsiBuilder.Marker? {
    var result = xmlText
    val tt = token()
    if (tt === INTERPOLATION_START) {
      result = terminateText(result)
      val interpolation = mark()
      advance()
      if (token() === INTERPOLATION_EXPR) {
        parseInterpolationExpr()
      }
      if (token() === INTERPOLATION_END) {
        advance()
        interpolation.drop()
      }
      else {
        interpolation.error(VueBundle.message("vue.parser.message.unterminated.interpolation"))
      }
    }
    return result
  }

  override fun parseCustomTopLevelContent(error: PsiBuilder.Marker?): PsiBuilder.Marker? {
    val result = flushError(error)
    terminateText(parseCustomTagContent(null))
    return result
  }

  override fun parseAttribute() {
    assert(token() === XmlTokenType.XML_NAME)
    val attr = mark()
    val attributeInfo = VueAttributeNameParser.parse(builder.tokenText!!, peekTagName(), tagLevel() == 1)
    advance()
    if (token() === XmlTokenType.XML_EQ) {
      advance()
      parseAttributeValue(attributeInfo)
    }
    if (peekTagName().toLowerCase(Locale.US) == SLOT_TAG_NAME) {
      attr.done(VueStubElementTypes.SLOT_TAG_ATTRIBUTE)
    }
    else
      when (attributeInfo.kind) {
        TEMPLATE_SRC, SCRIPT_SRC, STYLE_SRC -> attr.done(VueStubElementTypes.SRC_ATTRIBUTE)
        SCRIPT_ID -> attr.done(VueStubElementTypes.SCRIPT_ID_ATTRIBUTE)
        else -> attr.done(XmlElementType.XML_ATTRIBUTE)

      }
  }

  private fun parseAttributeValue(attributeInfo: VueAttributeNameParser.VueAttributeInfo) {
    val attValue = mark()
    val contentType = if (attributeInfo.injectJS) VueJSEmbeddedExprTokenType.createEmbeddedExpression(attributeInfo, builder.project)
    else null
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

        when (tt) {
          XmlTokenType.XML_BAD_CHARACTER -> {
            val error = mark()
            advance()
            error.error(XmlPsiBundle.message("unescaped.ampersand.or.nonterminated.character.entity.reference"))
          }
          XmlTokenType.XML_ENTITY_REF_TOKEN -> parseReference()
          INTERPOLATION_EXPR -> parseInterpolationExpr()
          else -> advance()
        }
      }
      contentStart?.collapse(contentType!!)
      if (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
        advance()
      }
      else {
        error(XmlPsiBundle.message("xml.parsing.unclosed.attribute.value"))
      }
    }
    else {
      if (token() !== XmlTokenType.XML_TAG_END && token() !== XmlTokenType.XML_EMPTY_ELEMENT_END) {
        if (contentType != null) {
          val contentStart = mark()
          advance()
          contentStart.collapse(contentType)
        }
        else {
          advance() // Single token att value
        }
      }
    }
    attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE)
  }

  private fun parseInterpolationExpr() {
    assert(token() === INTERPOLATION_EXPR)
    val marker = mark()
    advance()
    marker.collapse(VueJSEmbeddedExprTokenType.createInterpolationExpression(builder.project))
  }

  override fun getHtmlTagElementType(): IElementType {
    val tagName = peekTagName().toLowerCase(Locale.US)
    if (tagName in STUBBED_TAGS
        || (tagLevel() == 1 && tagName in TOP_LEVEL_TAGS)) {
      return VueStubElementTypes.STUBBED_TAG
    }
    return super.getHtmlTagElementType()
  }

  companion object {
    val STUBBED_TAGS: List<String> = listOf(SCRIPT_TAG_NAME, SLOT_TAG_NAME)
    val TOP_LEVEL_TAGS: List<String> = listOf(TEMPLATE_TAG_NAME, STYLE_TAG_NAME)
  }
}
