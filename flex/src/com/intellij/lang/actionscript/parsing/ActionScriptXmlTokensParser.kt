// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesAndCommentsBinder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle
import com.intellij.lang.javascript.parsing.JSXmlParser
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.containers.Stack
import com.intellij.util.text.CharSequenceSubSequence
import com.intellij.xml.parsing.XmlParserBundle

internal class ActionScriptXmlTokensParser(private val parser: ActionScriptParser) : JSXmlParser {
  private val builder: PsiBuilder = parser.builder

  override fun isXmlTagStart(currentToken: IElementType?): Boolean {
    return currentToken === JSTokenTypes.XML_START_TAG_START || currentToken === JSTokenTypes.XML_START_TAG_LIST
  }

  override fun parseTag(names: Stack<String>): Boolean {
    val tokenType = builder.tokenType
    assert(JSTokenTypes.XML_START_TAG_START === tokenType || JSTokenTypes.XML_START_TAG_LIST === tokenType)

    val marker = builder.mark()
    builder.advanceLexer()
    val name: String?
    if (builder.tokenType === JSTokenTypes.XML_LBRACE) {
      val nameBuilder = StringBuilder()
      parseXmlJsScript(nameBuilder)
      name = nameBuilder.toString()
    }
    else {
      name = builder.tokenText
    }
    var seenEnd = JSTokenTypes.XML_START_TAG_LIST === tokenType
    names.push(name)
    var hasErrors = false
    try {
      var currentTokenType = builder.tokenType
      while (currentTokenType != null
      ) {
        if (!JSElementTypes.XML_TOKENS.contains(currentTokenType)
            && currentTokenType !== JSTokenTypes.XML_LBRACE
        ) {
          if (!seenEnd) {
            builder.error(XmlParserBundle.message("xml.parsing.tag.start.is.not.closed"))
            return false
          }
          val errorMarker = builder.mark()
          builder.advanceLexer()
          errorMarker.error(JavaScriptParserBundle.message("javascript.parser.message.expected.xml.element"))
          currentTokenType = builder.tokenType
          continue
        }

        if (currentTokenType === JSTokenTypes.XML_START_TAG_START ||
            currentTokenType === JSTokenTypes.XML_START_TAG_LIST
        ) {
          parseTag(names)
          currentTokenType = builder.tokenType
          continue
        }
        else if (currentTokenType === JSTokenTypes.XML_EMPTY_TAG_END ||
                 currentTokenType === JSTokenTypes.XML_END_TAG_LIST
        ) {
          builder.advanceLexer()
          return !hasErrors
        }
        else if (currentTokenType === JSTokenTypes.XML_END_TAG_START) {
          if (!seenEnd) {
            builder.error(XmlParserBundle.message("xml.parsing.tag.start.is.not.closed"))
            return false
          }

          val endTagStart = builder.mark()
          builder.advanceLexer()
          val type = builder.tokenType
          if (type === XmlTokenType.XML_TAG_NAME
              || type === JSTokenTypes.XML_LBRACE) {
            val endName: String?
            if (type === JSTokenTypes.XML_LBRACE) {
              val nameBuilder = StringBuilder()
              parseXmlJsScript(nameBuilder)
              endName = nameBuilder.toString()
            }
            else {
              endName = builder.tokenText
            }
            if (!StringUtil.equals(name, endName)
                && (endName == null || !endName.endsWith(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED))
            ) {
              if (names.contains(endName)) {
                endTagStart.rollbackTo()
                builder.error(XmlParserBundle.message("xml.parsing.named.element.is.not.closed", name))
                return !hasErrors
              }
              else {
                builder.error(XmlParserBundle.message("xml.parsing.closing.tag.matches.nothing"))
                hasErrors = true
              }
            }
            if (type !== JSTokenTypes.XML_LBRACE) {
              builder.advanceLexer()
            }
          }
          else if (JSTokenTypes.XML_START_TAG_LIST !== tokenType) {
            builder.error(XmlParserBundle.message("xml.parsing.closing.tag.name.missing"))
            hasErrors = true
          }
          if (builder.tokenType !== XmlTokenType.XML_TAG_END) {
            builder.error(XmlParserBundle.message("xml.parsing.closing.tag.is.not.done"))
            hasErrors = true
          }
          else {
            builder.advanceLexer()
          }

          endTagStart.drop()
          return !hasErrors
        }
        else if (currentTokenType === JSTokenTypes.XML_NAME) {
          parseAttribute()
          currentTokenType = builder.tokenType
          continue
        }
        else if (currentTokenType === JSTokenTypes.XML_TAG_CONTENT) {
          val xmlTextMarker = builder.mark()
          builder.advanceLexer()
          xmlTextMarker.done(JSElementTypes.XML_TEXT)

          currentTokenType = builder.tokenType
          continue
        }

        if (currentTokenType === JSTokenTypes.XML_TAG_END) {
          seenEnd = true
        }

        if (currentTokenType === JSTokenTypes.XML_LBRACE) {
          parseXmlJsScript(null)
        }
        else {
          if (currentTokenType === JSTokenTypes.XML_RBRACE) {
            builder.error(JavaScriptParserBundle.message("javascript.parser.message.unexpected.token", builder.tokenText))
          }
          builder.advanceLexer()
        }

        if (currentTokenType !== JSTokenTypes.XML_LBRACE
            && builder.tokenType === JSTokenTypes.XML_ATTR_EQUAL) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.missing.attribute.name"))
          hasErrors = true
        }
        currentTokenType = builder.tokenType
      }
      return !hasErrors
    }
    finally {
      names.pop()
      marker.done(xmlLiteralExpression)
      marker.setCustomEdgeTokenBinders(null, INCOMPLETE_TAG_WHITESPACE_BINDER)
    }
  }

  private fun parseXmlJsScript(text: StringBuilder?) {
    val textStart = builder.currentOffset
    parser.expressionParser.parseScriptExpression(false)
    text?.append(StringUtil.trim(CharSequenceSubSequence(builder.originalText, textStart, builder.currentOffset)))
  }

  private val xmlLiteralExpression: IElementType
    get() = JSElementTypes.XML_LITERAL_EXPRESSION


  private fun parseAttribute() {
    assert(builder.tokenType === XmlTokenType.XML_NAME)
    val att = builder.mark()
    builder.advanceLexer()
    if (builder.tokenType === XmlTokenType.XML_EQ) {
      builder.advanceLexer()
      parseAttributeValue()
    }
    att.done(JSElementTypes.XML_ATTRIBUTE)
  }

  private fun parseAttributeValue() {
    val attValue = builder.mark()
    if (builder.tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER) {
      while (true) {
        val tt = builder.tokenType
        if (tt == null || tt === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER || tt === XmlTokenType.XML_END_TAG_START || tt === XmlTokenType.XML_EMPTY_ELEMENT_END || tt === XmlTokenType.XML_START_TAG_START) {
          break
        }

        builder.advanceLexer()
      }

      if (builder.tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
        builder.advanceLexer()
      }
      else {
        builder.error(XmlParserBundle.message("xml.parsing.unclosed.attribute.value"))
      }
    }
    else {
      if (builder.tokenType !== XmlTokenType.XML_TAG_END && builder.tokenType !== XmlTokenType.XML_EMPTY_ELEMENT_END) {
        if (builder.tokenType === JSTokenTypes.XML_LBRACE) {
          parseXmlJsScript(null)
        }
        else {
          builder.advanceLexer() // Single token att value
        }
      }
    }

    attValue.done(JSElementTypes.XML_ATTRIBUTE_VALUE)
  }

  companion object {
    private val INCOMPLETE_TAG_WHITESPACE_BINDER = WhitespacesAndCommentsBinder { tokens, _, _ ->
      val last = tokens[tokens.size - 1]
      val prev = tokens[tokens.size - 2]
      if (last === JSTokenTypes.WHITE_SPACE
          && prev !== JSTokenTypes.XML_TAG_END
          && prev !== JSTokenTypes.XML_END_TAG_LIST
          && prev !== JSTokenTypes.XML_EMPTY_TAG_END
          && !JSElementTypes.COMMENTS.contains(prev)
      ) 1
      else 0
    }
  }
}
