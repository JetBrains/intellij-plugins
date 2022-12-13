// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.xml.util.HtmlUtil.*
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.*
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.VueScriptLangs
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes
import org.jetbrains.vuejs.lang.expr.parser.VueJSEmbeddedExprTokenType
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START
import org.jetbrains.vuejs.model.SLOT_TAG_NAME
import java.util.*

class VueParsing(builder: PsiBuilder) : HtmlParsing(builder) {
  private val langMode: LangMode get() = builder.getUserData(VueScriptLangs.LANG_MODE)!!

  override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
    // There are heavily-used Vue components called like 'Col' or 'Input'. Unlike HTML tags <col> and <input> Vue components do have closing tags.
    // The following 'if' is a little bit hacky but it's rather tricky to solve the problem in a better way at parser level.
    if (tagName.length >= 3
        && tagName != originalTagName
        && !originalTagName.all { it.isUpperCase() }) {
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
      if (token() is VueJSEmbeddedExprTokenType) {
        maybeRemapCurrentToken(token())
        advance()
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

  override fun maybeRemapCurrentToken(tokenType: IElementType) {
    if (tokenType is VueJSEmbeddedExprTokenType) {
      builder.remapCurrentToken(tokenType.copyWithLanguage(langMode))
    }
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
      parseAttributeValue()
    }
    if (peekTagName().lowercase(Locale.US) == SLOT_TAG_NAME) {
      attr.done(VueStubElementTypes.STUBBED_ATTRIBUTE)
    }
    else
      when (attributeInfo.kind) {
        TEMPLATE_SRC, SCRIPT_SRC, STYLE_SRC -> attr.done(VueStubElementTypes.SRC_ATTRIBUTE)
        SCRIPT_ID -> attr.done(VueStubElementTypes.SCRIPT_ID_ATTRIBUTE)
        SCRIPT_SETUP, STYLE_MODULE -> attr.done(VueStubElementTypes.STUBBED_ATTRIBUTE)
        REF -> attr.done(VueStubElementTypes.REF_ATTRIBUTE)
        else -> attr.done(XmlElementType.XML_ATTRIBUTE)
      }
  }

  override fun getHtmlTagElementType(): IElementType {
    val tagName = peekTagName().lowercase(Locale.US)
    if (tagName in ALWAYS_STUBBED_TAGS
        || (tagLevel() == 1 && tagName in TOP_LEVEL_TAGS)) {
      return if (tagName == TEMPLATE_TAG_NAME) VueStubElementTypes.TEMPLATE_TAG else VueStubElementTypes.STUBBED_TAG
    }
    return super.getHtmlTagElementType()
  }

  companion object {
    val ALWAYS_STUBBED_TAGS: List<String> = listOf(SCRIPT_TAG_NAME, SLOT_TAG_NAME)
    val TOP_LEVEL_TAGS: List<String> = listOf(TEMPLATE_TAG_NAME, STYLE_TAG_NAME)
  }
}
