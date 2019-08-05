// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.codeInsight.daemon.XmlErrorMessages
import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.lang.expr.parser.VueJSEmbeddedExprTokenType

class VueParsing(builder: PsiBuilder) : HtmlParsing(builder) {
  override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
    // There are heavily-used Vue components called like 'Col' or 'Input'. Unlike HTML tags <col> and <input> Vue components do have closing tags.
    // The following 'if' is a little bit hacky but it's rather tricky to solve the problem in a better way at parser level.
    if (tagName != originalTagName) {
      return false
    }
    return super.isSingleTag(tagName, originalTagName)
  }

  override fun parseAttribute() {
    assert(token() === XmlTokenType.XML_NAME)
    val attr = mark()
    val attributeInfo = VueAttributeNameParser.parse(builder.tokenText!!, null)
    advance()
    if (token() === XmlTokenType.XML_EQ) {
      advance()
      parseAttributeValue(attributeInfo)
    }
    attr.done(XmlElementType.XML_ATTRIBUTE)
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
            error.error(XmlErrorMessages.message("unescaped.ampersand.or.nonterminated.character.entity.reference"))
          }
          XmlTokenType.XML_ENTITY_REF_TOKEN -> parseReference()
          else -> advance()
        }
      }
      contentStart?.collapse(contentType!!)
      if (token() === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
        advance()
      }
      else {
        error(XmlErrorMessages.message("xml.parsing.unclosed.attribute.value"))
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
}
