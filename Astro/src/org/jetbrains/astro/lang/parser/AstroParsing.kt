// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.html.HtmlParsing
import com.intellij.lang.javascript.*
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptExpressionParser
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptParser
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptStatementParser
import com.intellij.lang.javascript.parsing.JSParsingContextUtil
import com.intellij.lang.javascript.parsing.JSXmlParser
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.containers.Stack
import com.intellij.xml.parsing.XmlParserBundle
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.lexer.AstroLexer
import org.jetbrains.astro.lang.lexer.AstroTokenTypes


class AstroParsing(builder: PsiBuilder) : HtmlParsing(builder), JSXmlParser {

  val tsxParser = AstroJsxParser()

  private val typeScriptParser = TypeScriptParser(builder)

  override fun isXmlTagStart(currentToken: IElementType?): Boolean =
    currentToken === XmlTokenType.XML_START_TAG_START

  override fun parseDocument() {
    builder.enforceCommentTokens(JSTokenTypes.COMMENTS)
    val embeddedContent = builder.mark()

    while (token().let { it === XmlTokenType.XML_REAL_WHITE_SPACE || it === XmlTokenType.XML_COMMENT_CHARACTERS })
      advance()

    if (token() === AstroTokenTypes.FRONTMATTER_SEPARATOR) {
      parseFrontmatter()
    }

    while (token() === XmlTokenType.XML_COMMENT_START) {
      parseComment()
    }

    parseProlog()

    var error: Marker? = null
    while (!eof()) {
      val tt = token()
      if (tt === XmlTokenType.XML_START_TAG_START) {
        error = flushError(error)
        parseTag()
      }
      else if (tt === XmlTokenType.XML_COMMENT_START) {
        error = flushError(error)
        parseComment()
      }
      else if (tt === XmlTokenType.XML_PI_START) {
        error = flushError(error)
        parseProcessingInstruction()
      }
      else if (tt === XmlTokenType.XML_CHAR_ENTITY_REF || tt === XmlTokenType.XML_ENTITY_REF_TOKEN) {
        parseReference()
      }
      else if (tt === XmlTokenType.XML_REAL_WHITE_SPACE || tt === XmlTokenType.XML_DATA_CHARACTERS) {
        error = flushError(error)
        advance()
      }
      else if (tt === XmlTokenType.XML_END_TAG_START) {
        val tagEndError = builder.mark()
        advance()
        if (token() === XmlTokenType.XML_NAME) {
          advance()
          if (token() === XmlTokenType.XML_TAG_END) {
            advance()
          }
        }
        tagEndError.error(XmlParserBundle.message("xml.parsing.closing.tag.matches.nothing"))
      }
      else if (hasCustomTopLevelContent()) {
        error = parseCustomTopLevelContent(error)
      }
      else {
        if (error == null) error = mark()
        advance()
      }
    }
    flushIncompleteStackItemsWhile { it is HtmlTagInfo }
    error?.error(XmlParserBundle.message("xml.parsing.top.level.element.is.not.completed"))
    embeddedContent.done(AstroStubElementTypes.CONTENT_ROOT)
  }

  private fun parseFrontmatter() {
    advance()
    val frontmatterScript = builder.mark()
    // parse frontmatter
    builder.putUserData(JSParsingContextUtil.ASYNC_METHOD_KEY, true)
    while (builder.tokenType.let { it != null && it != AstroTokenTypes.FRONTMATTER_SEPARATOR }) {
      typeScriptParser.statementParser.parseStatement()
    }
    frontmatterScript.done(AstroStubElementTypes.FRONTMATTER_SCRIPT)
    if (token() === AstroTokenTypes.FRONTMATTER_SEPARATOR) {
      advance()
    }
  }

  override fun parseTag(names: Stack<String>): Boolean {
    parseTag()
    while (hasTags()) {
      val tag = peekTagInfo()
      if (isEndTagRequired(tag)) {
        error(XmlParserBundle.message("xml.parsing.named.element.is.not.closed", tag.originalName))
      }
      doneTag()
    }
    return true
  }

  override fun shouldContinueParsingTag(): Boolean {
    val token = token()
    if (token === JSTokenTypes.XML_LBRACE || token is JSEmbeddedContentElementType) return true
    if (builder.hasJSToken()) return false
    if (token === XmlTokenType.XML_START_TAG_START) return true
    return stackSize() == 0 || hasTags()
  }

  override fun parseOpenTagName(): String {
    val result: String
    if (token() === XmlTokenType.XML_NAME) {
      result = builder.tokenText!!
      advance()
    }
    else {
      result = ""
    }
    return result
  }

  override fun parseEndTagName(): String? {
    if (token() === XmlTokenType.XML_NAME) {
      val result = StringUtil.toLowerCase(builder.tokenText!!)
      advance()
      return result
    }
    else if (hasTags()) {
      // Astro does not care about closing tag names at all
      // Make an exception and allow empty closing tag </>
      // to close anything.
      return peekTagInfo().normalizedName
    }
    return null
  }

  override fun hasCustomTagContent(): Boolean {
    return token() === JSTokenTypes.XML_LBRACE
  }

  override fun parseCustomTagContent(xmlText: Marker?): Marker? {
    var result = xmlText
    when (token()) {
      JSTokenTypes.XML_LBRACE -> {
        result = terminateText(xmlText)
        parseJsxExpression(false, true)
      }
    }
    return result
  }

  override fun hasCustomTopLevelContent(): Boolean {
    return hasCustomTagContent()
  }

  override fun parseCustomTopLevelContent(error: Marker?): Marker? {
    val result = flushError(error)
    terminateText(parseCustomTagContent(null))
    return result
  }

  override fun hasCustomAttributeValue(): Boolean {
    return token().let {
      it === JSTokenTypes.XML_LBRACE
      || it === JSTokenTypes.BACKQUOTE
    }
  }

  override fun parseCustomAttributeValue() {
    if (token() === JSTokenTypes.BACKQUOTE) {
      parseAttributeTemplateLiteralExpression()
    }
    else {
      parseJsxExpression(true, false)
    }
  }

  override fun hasCustomTagHeaderContent(): Boolean {
    return token() === JSTokenTypes.XML_LBRACE
  }

  override fun parseCustomTagHeaderContent() {
    when (token()) {
      JSTokenTypes.XML_LBRACE -> {
        val attributeName = builder.mark()
        parseJsxExpression(true, false)
        // Consume possible bad characters
        while (token() == XmlTokenType.XML_BAD_CHARACTER) {
          builder.advanceLexer()
        }
        // Expression attributes, which are followed by `=`,
        // are not expression attributes as far as Astro lexer is concerned
        if (token() == XmlTokenType.XML_EQ) {
          attributeName.collapse(XmlTokenType.XML_NAME)
          advance()
          parseAttributeValue()
          attributeName.precede().done(JSStubElementTypes.XML_ATTRIBUTE)
        }
        else {
          attributeName.done(JSStubElementTypes.XML_ATTRIBUTE)
        }
      }
    }
  }

  override fun getHtmlTagElementType(info: HtmlTagInfo, tagLevel: Int): IElementType {
    // AstroTag:script is considered to have language Astro and not JS causing issues with formatting unlike HtmlTag:script
    return if (info.normalizedName == "script") XmlElementType.HTML_TAG
    else JSElementTypes.JSX_XML_LITERAL_EXPRESSION
  }

  override fun getHtmlAttributeElementType(): IElementType {
    return AstroStubElementTypes.HTML_ATTRIBUTE
  }

  override fun getHtmlAttributeValueElementType(): IElementType {
    return JSElementTypes.XML_ATTRIBUTE_VALUE
  }

  override fun isSingleTag(tagInfo: HtmlTagInfo): Boolean {
    return !AstroLexer.isPossiblyComponentTag(tagInfo.originalName) && super.isSingleTag(tagInfo)
  }

  private fun parseJsxExpression(supportsNestedTemplateLiterals: Boolean, supportsEmptyExpression: Boolean) {
    parseExpressionWithTagsHandled {
      (tsxParser.expressionParser as AstroTypeScriptExpressionParser)
        .parseExpression(supportsNestedTemplateLiterals, supportsEmptyExpression)
    }
  }

  private fun parseAttributeTemplateLiteralExpression() {
    parseExpressionWithTagsHandled {
      (tsxParser.expressionParser as AstroTypeScriptExpressionParser).parseAttributeTemplateLiteralExpression()
    }
  }

  private fun parseExpressionWithTagsHandled(parse: () -> Unit) {
    pushItemToStack(AstroExpressionItem(mark()))
    parse()
    flushIncompleteStackItemsWhile { it !is AstroExpressionItem }
    completeTopStackItem()
  }

  private class AstroExpressionItem(private val expressionStart: Marker) : HtmlParserStackItem {
    override fun done(
      builder: PsiBuilder,
      beforeMarker: Marker?,
      incomplete: Boolean,
    ) {
      if (beforeMarker == null) {
        expressionStart.done(JSStubElementTypes.EMBEDDED_EXPRESSION)
      }
      else {
        expressionStart.doneBefore(JSStubElementTypes.EMBEDDED_EXPRESSION, beforeMarker)
      }
    }
  }

  inner class AstroJsxParser internal constructor() : TypeScriptParser(AstroLanguage.INSTANCE, builder) {
    override val expressionParser: TypeScriptExpressionParser =
      AstroTypeScriptExpressionParser(this)

    override val statementParser: TypeScriptStatementParser =
      object : TypeScriptStatementParser(this@AstroJsxParser) {
        override fun parseBlock(): Boolean {
          val mark = builder.mark()
          parseBlockAndAttachStatementsDirectly()
          mark.done(JSElementTypes.BLOCK_STATEMENT_EAGER)
          return true
        }
      }

    override val xmlParser: JSXmlParser =
      this@AstroParsing
  }

  private inner class AstroTypeScriptExpressionParser(parser: TypeScriptParser) : TypeScriptExpressionParser(parser) {

    private var supportNestedTemplateLiterals: Boolean = true
    private var topLevelTemplateLiteralParse: Boolean = false

    fun parseExpression(supportsNestedTemplateLiterals: Boolean, supportsEmptyExpression: Boolean) {
      withNestedTemplateLiteralsSupport(supportsNestedTemplateLiterals) {
        checkMatches(builder, JSTokenTypes.XML_LBRACE, "javascript.parser.message.expected.lbrace")
        if (builder.tokenType === JSTokenTypes.XML_RBRACE) {
          if (!supportsEmptyExpression) {
            builder.error(AstroBundle.message("astro.parsing.error.empty.expression"))
          }
        }
        else if (!parseArgument()) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
        }

        if (!checkMatches(builder, JSTokenTypes.XML_RBRACE, "javascript.parser.message.expected.rbrace")) {
          while (builder.tokenType !== JSTokenTypes.XML_RBRACE && !builder.eof()) {
            if (builder.tokenType === JSTokenTypes.XML_END_TAG_START) {
              val footer = builder.mark()
              builder.advanceLexer()
              parseEndTagName()
              if (token() === XmlTokenType.XML_TAG_END) builder.advanceLexer()
              footer.error(XmlParserBundle.message("xml.parsing.closing.tag.matches.nothing"))
            }
            else if (!parseArgument()) {
              builder.advanceLexer()
            }
          }
          if (builder.tokenType === JSTokenTypes.XML_RBRACE) {
            builder.advanceLexer()
          }
        }
      }
    }

    fun parseAttributeTemplateLiteralExpression() {
      withNestedTemplateLiteralsSupport(false) {
        parseStringTemplate()
      }
    }

    private fun withNestedTemplateLiteralsSupport(enabled: Boolean, action: () -> Unit) {
      val prev = supportNestedTemplateLiterals
      supportNestedTemplateLiterals = enabled
      try {
        action()
      }
      finally {
        supportNestedTemplateLiterals = prev
      }
    }

    override fun parsePrimaryExpression(): Boolean {
      if (builder.tokenType === JSTokenTypes.BACKQUOTE && topLevelTemplateLiteralParse) {
        builder.error(AstroBundle.message("astro.parsing.error.nested.template.literals.not.supported"))
        return false
      }
      return super.parsePrimaryExpression()
    }

    override fun parseStringTemplate(): Boolean {
      if (!supportNestedTemplateLiterals) {
        if (topLevelTemplateLiteralParse) {
          builder.error(AstroBundle.message("astro.parsing.error.nested.template.literals.not.supported"))
          builder.advanceLexer()
          return true
        }
        else {
          topLevelTemplateLiteralParse = true
        }
      }
      return super.parseStringTemplate().also {
        if (!supportNestedTemplateLiterals) {
          topLevelTemplateLiteralParse = false
        }
      }
    }
  }

  companion object {
    private fun PsiBuilder.hasJSToken() =
      tokenType?.language?.isKindOf(JavascriptLanguage) == true

  }
}