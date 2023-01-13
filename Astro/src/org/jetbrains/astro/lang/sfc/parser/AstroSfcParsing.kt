// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptParser
import com.intellij.lang.javascript.parsing.JSXmlParser
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.containers.Stack
import com.intellij.xml.psi.XmlPsiBundle
import org.jetbrains.astro.lang.jsx.AstroJsxLanguage
import org.jetbrains.astro.lang.jsx.psi.AstroJsxStubElementTypes
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcTokenTypes

class AstroSfcParsing(builder: PsiBuilder) : HtmlParsing(builder), JSXmlParser {

  private val tsxParser = AstroJsxParser()

  override fun hasCustomTopLevelContent(): Boolean {
    return CUSTOM_CONTENT.contains(token())
  }

  override fun hasCustomTagContent(): Boolean {
    return CUSTOM_CONTENT.contains(token())
  }

  override fun isXmlTagStart(currentToken: IElementType?): Boolean =
    currentToken == XmlTokenType.XML_START_TAG_START

  override fun parseTag(names: Stack<String>): Boolean {
    parseTag()
    return true
  }

  override fun shouldContinueParsingTag(): Boolean {
    return !hasJSToken()
  }

  override fun parseOpenTagName(): String {
    val result: String
    if (token() == XmlTokenType.XML_NAME) {
      result = builder.tokenText!!
      advance()
    }
    else {
      result = ""
    }
    return result
  }

  override fun parseEndTagName(): String {
    val result: String
    if (token() == XmlTokenType.XML_NAME) {
      result = StringUtil.toLowerCase(builder.tokenText!!)
      advance()
    }
    else {
      // Astro does not care about closing tag names at all
      // Make an exception and allow empty closing tag </>
      // to close anything.
      result = peekTagName()
    }
    return result
  }

  private fun hasJSToken() =
    token()?.language?.isKindOf(JavascriptLanguage.INSTANCE) == true

  override fun parseProlog() {
    while (token().let {
        it == XmlTokenType.XML_COMMENT_CHARACTERS
        || it == AstroSfcTokenTypes.FRONTMATTER_SEPARATOR
        || it == AstroSfcTokenTypes.FRONTMATTER_SCRIPT
      })
      advance()
    super.parseProlog()
  }

  override fun parseCustomTagContent(xmlText: PsiBuilder.Marker?): PsiBuilder.Marker? {
    var result = xmlText
    when (token()) {
      JSTokenTypes.XML_LBRACE -> {
        result = terminateText(xmlText)
        tsxParser.parseJsxExpression()
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
    when (token()) {
      else -> {
        super.parseAttribute()
      }
    }
  }

  override fun parseAttributeValue() {
    when (token()) {
      else -> {
        super.parseAttributeValue()
      }
    }
  }

  inner class AstroJsxParser : TypeScriptParser(AstroJsxLanguage.INSTANCE, builder) {

    fun parseJsxExpression() {
      val exprStart = mark()
      pushTag(exprStart, "\$EXPR$", "\$EXPR$")
      assert(token() == JSTokenTypes.XML_LBRACE)
      advance()
      expressionParser.parseExpression()
      if (token() == JSTokenTypes.XML_RBRACE)
        advance()
      else
        error("Missing expression closing brace")
      while (peekTagName() != "\$EXPR$") {
        val tagName = peekTagName()
        if (isEndTagRequired(tagName)) {
          kotlin.error(XmlPsiBundle.message("xml.parsing.named.element.is.not.closed", peekTagName()))
        }
        doneTag()
      }
      closeTag()
      exprStart.done(AstroJsxStubElementTypes.EMBEDDED_EXPRESSION)
    }

    init {
      myXmlParser = this@AstroSfcParsing
    }
  }

  companion object {
    private val CUSTOM_CONTENT = TokenSet.create(JSTokenTypes.XML_LBRACE)
  }
}