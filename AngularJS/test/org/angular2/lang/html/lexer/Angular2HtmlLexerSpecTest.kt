// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer

import com.intellij.openapi.util.Pair
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import com.mscharhag.oleaster.matcher.Matchers
import com.mscharhag.oleaster.matcher.matchers.CollectionMatcher
import com.mscharhag.oleaster.matcher.util.Expectations
import com.mscharhag.oleaster.runner.OleasterRunner
import com.mscharhag.oleaster.runner.StaticRunnerSupport.describe
import com.mscharhag.oleaster.runner.StaticRunnerSupport.it
import org.angular2.lang.OleasterTestUtil.bootstrapLightPlatform
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType
import org.junit.runner.RunWith

@RunWith(OleasterRunner::class)
class Angular2HtmlLexerSpecTest {
  private val JS_EMBEDDED_CONTENT: Any = object : Any() {
    override fun equals(other: Any?): Boolean {
      return other != null && other.javaClass.getName().endsWith(".JSEmbeddedContentElementType")
    }
  }

  init {
    describe("HtmlLexer") {
      bootstrapLightPlatform()
      describe("line/column numbers") {
        it("should work without newlines") {
          Matchers.expect(tokenizeAndHumanizeLineColumn("<t>a</t>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "0:0"),
                   listOf(XmlTokenType.XML_NAME, "0:1"),
                   listOf(XmlTokenType.XML_TAG_END, "0:2"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "0:3"),
                   listOf(XmlTokenType.XML_END_TAG_START, "0:4"),
                   listOf(XmlTokenType.XML_NAME, "0:6"),
                   listOf(XmlTokenType.XML_TAG_END, "0:7")))
        }
        it("should work with one newline") {
          Matchers.expect(tokenizeAndHumanizeLineColumn("<t>\na</t>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "0:0"),
                   listOf(XmlTokenType.XML_NAME, "0:1"),
                   listOf(XmlTokenType.XML_TAG_END, "0:2"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "0:3"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "0:4"),
                   listOf(XmlTokenType.XML_END_TAG_START, "0:5"),
                   listOf(XmlTokenType.XML_NAME, "0:7"),
                   listOf(XmlTokenType.XML_TAG_END, "0:8")))
        }
        it("should work with multiple newlines") {
          Matchers.expect(tokenizeAndHumanizeLineColumn("<t\n>\na</t>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "0:0"),
                   listOf(XmlTokenType.XML_NAME, "0:1"),
                   listOf(TokenType.WHITE_SPACE, "0:2"),
                   listOf(XmlTokenType.XML_TAG_END, "0:3"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "0:4"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "0:5"),
                   listOf(XmlTokenType.XML_END_TAG_START, "0:6"),
                   listOf(XmlTokenType.XML_NAME, "0:8"),
                   listOf(XmlTokenType.XML_TAG_END, "0:9")))
        }
        it("should work with CR and LF") {
          Matchers.expect(tokenizeAndHumanizeLineColumn("<t\n>\r\na\r</t>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "0:0"),
                   listOf(XmlTokenType.XML_NAME, "0:1"),
                   listOf(TokenType.WHITE_SPACE, "0:2"),
                   listOf(XmlTokenType.XML_TAG_END, "0:3"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "0:4"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "0:6"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "0:7"),
                   listOf(XmlTokenType.XML_END_TAG_START, "0:8"),
                   listOf(XmlTokenType.XML_NAME, "0:10"),
                   listOf(XmlTokenType.XML_TAG_END, "0:11")))
        }
      }
      describe("comments") {
        it("should parse comments") {
          Matchers.expect(tokenizeAndHumanizeParts("<!--t\ne\rs\r\nt-->")).toEqual(
            listOf(listOf(XmlTokenType.XML_COMMENT_START, "<!--"),
                   listOf(XmlTokenType.XML_COMMENT_CHARACTERS, "t\ne\rs\r\nt"),
                   listOf(XmlTokenType.XML_COMMENT_END, "-->")))
        }

        //it("should report <!- without -", () -> {
        //  expect(tokenizeAndHumanizeErrors("<!-a")).toEqual(newArrayList(
        //    newArrayList(COMMENT_START, "Unexpected character \"a\"", "0:3")
        //  ));
        //});
        //
        //it("should report missing end comment", () -> {
        //  expect(tokenizeAndHumanizeErrors("<!--")).toEqual(newArrayList(
        //    newArrayList(XML_DATA_CHARACTERS, "Unexpected character \"EOF\"", "0:4")
        //  ));
        //});
        it("should accept comments finishing by too many dashes (even number)") {
          Matchers.expect(tokenizeAndHumanizeSourceSpans("<!-- test ---->")).toEqual(
            listOf(listOf(XmlTokenType.XML_COMMENT_START, "<!--"),
                   listOf(XmlTokenType.XML_COMMENT_CHARACTERS, " test --"),
                   listOf(XmlTokenType.XML_COMMENT_END, "-->")))
        }
        it("should accept comments finishing by too many dashes (odd number)") {
          Matchers.expect(tokenizeAndHumanizeSourceSpans("<!-- test --->")).toEqual(
            listOf(listOf(XmlTokenType.XML_COMMENT_START, "<!--"),
                   listOf(XmlTokenType.XML_COMMENT_CHARACTERS, " test -"),
                   listOf(XmlTokenType.XML_COMMENT_END, "-->")))
        }
        it("should not parse interpolation within comment") {
          Matchers.expect(tokenizeAndHumanizeParts("<!-- {{ v }} -->")).toEqual(
            listOf(listOf(XmlTokenType.XML_COMMENT_START, "<!--"),
                   listOf(XmlTokenType.XML_COMMENT_CHARACTERS, " {{ v }} "),
                   listOf(XmlTokenType.XML_COMMENT_END, "-->")))
        }
      }
      describe("doctype") {
        it("should parse doctypes") {
          Matchers.expect(tokenizeAndHumanizeParts("<!doctype html>")).toEqual(
            listOf(listOf(XmlTokenType.XML_DOCTYPE_START, "<!doctype"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "html"),
                   listOf(XmlTokenType.XML_DOCTYPE_END, ">")))
        }
      }
      describe("CDATA") {
        it("should parse CDATA") {
          Matchers.expect(tokenizeAndHumanizeParts("<![CDATA[t\ne\rs\r\nt]]>")).toEqual(
            listOf(listOf(XmlTokenType.XML_DATA_CHARACTERS, "<![CDATA[t"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "\n"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "e"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "\r"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "s"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "\r\n"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "t]]>")))
        }
      }
      describe("open tags") {
        it("should parse open tags without prefix") {
          Matchers.expect(tokenizeAndHumanizeParts("<test>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "test"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse namespace prefix") {
          Matchers.expect(tokenizeAndHumanizeParts("<ns1:test>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "ns1:test"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse void tags") {
          Matchers.expect(tokenizeAndHumanizeParts("<test/>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "test"),
                   listOf(XmlTokenType.XML_EMPTY_ELEMENT_END, "/>")))
        }
        it("should allow whitespace after the tag name") {
          Matchers.expect(tokenizeAndHumanizeParts("<test >")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "test"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
      }
      describe("attributes") {
        it("should parse attributes without prefix") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse attributes with interpolation") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a=\"{{v}}\" b=\"s{{m}}e\" c=\"s{{m//c}}e\">")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, "v"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "b"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "s"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, "m"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "e"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "c"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "s"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, "m//c"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "e"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should not parse interpolation within tag element") {
          Matchers.expect(tokenizeAndHumanizeParts("<t {{v}}=12>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "{{v}}"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "12"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse attributes with prefix") {
          Matchers.expect(tokenizeAndHumanizeParts("<t ns1:a>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "ns1:a"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse attributes whose prefix is not valid") {
          Matchers.expect(tokenizeAndHumanizeParts("<t (ns1:a)>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "(ns1:a)"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse attributes with single quote value") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a='b'>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "'"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "b"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "'"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse attributes with double quote value") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a=\"b\">")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "b"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse attributes with unquoted value") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a=b>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "b"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should allow whitespace") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a = b >")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "b"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse attributes with entities in values") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a=\"&#65;&#x41;\">")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_CHAR_ENTITY_REF, "&#65;"),
                   listOf(XmlTokenType.XML_CHAR_ENTITY_REF, "&#x41;"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should not decode entities without trailing \";\"") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a=\"&amp\" b=\"c&&d\">")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "&amp"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "b"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "c&&d"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse attributes with \"&\" in values") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a=\"b && c &\">")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "b && c &"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse values with CR and LF") {
          Matchers.expect(tokenizeAndHumanizeParts("<t a='t\ne\rs\r\nt'>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "t"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "a"),
                   listOf(XmlTokenType.XML_EQ, "="),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, "'"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, "t\ne\rs\r\nt"),
                   listOf(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, "'"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
      }
      describe("closing tags") {
        it("should parse closing tags without prefix") {
          Matchers.expect(tokenizeAndHumanizeParts("</test>")).toEqual(
            listOf(listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "test"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should parse closing tags with prefix") {
          Matchers.expect(tokenizeAndHumanizeParts("</ns1:test>")).toEqual(
            listOf(listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "ns1:test"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should allow whitespace") {
          Matchers.expect(tokenizeAndHumanizeParts("</test >")).toEqual(
            listOf(listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "test"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
      }
      describe("entities") {
        it("should parse named entities") {
          Matchers.expect(tokenizeAndHumanizeParts("a&amp;b")).toEqual(
            listOf(listOf(XmlTokenType.XML_DATA_CHARACTERS, "a"),
                   listOf(XmlTokenType.XML_CHAR_ENTITY_REF, "&amp;"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "b")))
        }
        it("should parse hexadecimal entities") {
          Matchers.expect(tokenizeAndHumanizeParts("&#x41;&#X41;")).toEqual(
            listOf(listOf(XmlTokenType.XML_CHAR_ENTITY_REF, "&#x41;"),
                   listOf(XmlTokenType.XML_CHAR_ENTITY_REF, "&#X41;")))
        }
        it("should parse decimal entities") {
          Matchers.expect(tokenizeAndHumanizeParts("&#65;")).toEqual(listOf(*arrayOf<Any>(
            listOf(XmlTokenType.XML_CHAR_ENTITY_REF, "&#65;")
          )))
        }
      }
      describe("regular text") {
        it("should parse text") {
          Matchers.expect(tokenizeAndHumanizeParts("a")).toEqual(listOf(*arrayOf<Any>(
            listOf(XmlTokenType.XML_DATA_CHARACTERS, "a")
          )))
        }
        it("should parse interpolation") {
          Matchers.expect(tokenizeAndHumanizeParts("{{ a }}b{{ c // comment }}")).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, " a "),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "b"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, " c // comment "),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))
          Matchers.expect(tokenizeAndHumanizeParts("{{a}}", false)).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, "a"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))
        }
        it("should parse empty interpolation") {
          Matchers.expect(tokenizeAndHumanizeParts("{{}}", false)).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))
          Matchers.expect(tokenizeAndHumanizeParts("{{ }}", false)).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, " "),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))
        }
        it("should parse interpolation with custom markers") {
          Matchers.expect(tokenizeAndHumanizeParts("{% a %}", false, Pair.pair("{%", "%}"))).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{%"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, " a "),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "%}")))
        }
        it("should parse empty interpolation with custom markers") {
          Matchers.expect(tokenizeAndHumanizeParts("{%%}", false, Pair.pair("{%", "%}"))).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{%"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "%}")))
          Matchers.expect(tokenizeAndHumanizeParts("{% %}", false, Pair.pair("{%", "%}"))).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{%"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, " "),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "%}")))
        }
        it("should parse interpolation with entities") {
          Matchers.expect(tokenizeAndHumanizeParts("{{&lt;}}", false)).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, "&lt;"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))
          Matchers.expect(tokenizeAndHumanizeParts("{{&xee12;}}", false)).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, "&xee12;"),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))
        }
        it("should handle CR & LF") {
          Matchers.expect(tokenizeAndHumanizeParts("t\ne\rs\r\nt")).toEqual(
            listOf(listOf(XmlTokenType.XML_DATA_CHARACTERS, "t"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "\n"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "e"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "\r"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "s"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, "\r\n"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "t")))
        }
        it("should parse entities") {
          Matchers.expect(tokenizeAndHumanizeParts("a&amp;b")).toEqual(
            listOf(listOf(XmlTokenType.XML_DATA_CHARACTERS, "a"),
                   listOf(XmlTokenType.XML_CHAR_ENTITY_REF, "&amp;"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "b")))
        }
        it("should parse text starting with \"&\"") {
          Matchers.expect(tokenizeAndHumanizeParts("a && b &")).toEqual(
            listOf(listOf(XmlTokenType.XML_DATA_CHARACTERS, "a"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "&&"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "b"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "&")))
        }
        it("should allow \"<\" in text nodes") {
          Matchers.expect(tokenizeAndHumanizeParts("{{ a < b ? c : d }}")).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, " a < b ? c : d "),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))

          //expect(tokenizeAndHumanizeSourceSpans("<p>a<b</p>")).toEqual(newArrayList(
          //  newArrayList(XML_START_TAG_START, "<"),
          //  newArrayList(XML_NAME, "p"),
          //  newArrayList(XML_TAG_END, ">"),
          //  newArrayList(XML_DATA_CHARACTERS, "a<b"),
          //  newArrayList(XML_END_TAG_START, "</"),
          //  newArrayList(XML_NAME, "p"),
          //  newArrayList(XML_TAG_END, ">")
          //));
          Matchers.expect(tokenizeAndHumanizeParts("< a>")).toEqual(
            listOf(listOf(XmlTokenType.XML_DATA_CHARACTERS, "<"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "a>")))
        }
        it("should parse valid start tag in interpolation") {
          Matchers.expect(tokenizeAndHumanizeParts("{{ a <b && c > d }}")).toEqual(
            listOf(listOf(XmlTokenType.XML_DATA_CHARACTERS, "{{"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "a"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "b"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "&&"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_NAME, "c"),
                   listOf(TokenType.WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "d"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "}}")))
          Matchers.expect(tokenizeAndHumanizeParts("{{<b>}}")).toEqual(
            listOf(listOf(XmlTokenType.XML_DATA_CHARACTERS, "{{"),
                   listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "b"),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "}}")))
        }
        it("should be able to escape {") {
          Matchers.expect(tokenizeAndHumanizeParts("{{ \"{\" }}")).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, " \"{\" "),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))
        }
        it("should be able to escape {{") {
          Matchers.expect(tokenizeAndHumanizeParts("{{ \"{{\" }}")).toEqual(
            listOf(listOf(Angular2HtmlTokenTypes.INTERPOLATION_START, "{{"),
                   listOf(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, " \"{{\" "),
                   listOf(Angular2HtmlTokenTypes.INTERPOLATION_END, "}}")))
        }
        it("should treat expansion form as text when they are not parsed") {
          Matchers.expect(tokenizeAndHumanizeParts("<span>{a, b, =4 {c}}</span>", false)).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "span"),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "{a,"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "b,"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "=4"),
                   listOf(XmlTokenType.XML_REAL_WHITE_SPACE, " "),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "{c}}"),
                   listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "span"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
      }
      describe("raw text") {
        it("should parse text") {
          expectReversed(tokenizeAndHumanizeParts("<script>t\ne\rs\r\nt</script>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(JS_EMBEDDED_CONTENT, "t\ne\rs\r\nt"),
                   listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should not detect entities") {
          expectReversed(tokenizeAndHumanizeParts("<script>&amp;</SCRIPT>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(JS_EMBEDDED_CONTENT, "&amp;"),
                   listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "SCRIPT"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should ignore other opening tags") {
          expectReversed(tokenizeAndHumanizeParts("<script>a<div></script>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(JS_EMBEDDED_CONTENT, "a<div>"),
                   listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should ignore other closing tags") {
          expectReversed(tokenizeAndHumanizeParts("<script>a</test></script>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(JS_EMBEDDED_CONTENT, "a</test>"),
                   listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
        it("should store the locations") {
          expectReversed(tokenizeAndHumanizeSourceSpans("<script>a</script>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(JS_EMBEDDED_CONTENT, "a"),
                   listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "script"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
      }

      //describe("escapable raw text", () -> {
      //  it("should parse text", () -> {
      //    expect(tokenizeAndHumanizeParts("<title>t\ne\rs\r\nt</title>")).toEqual(newArrayList(
      //      newArrayList(TAG_OPEN_START, null, "title"),
      //      newArrayList(TAG_OPEN_END),
      //      newArrayList(ESCAPABLE_RAW_TEXT, "t\ne\ns\nt"),
      //      newArrayList(TAG_CLOSE, null, "title"),
      //      newArrayList(EOF)
      //    ));
      //  });
      //
      //  it("should detect entities", () -> {
      //    expect(tokenizeAndHumanizeParts("<title>&amp;</title>")).toEqual(newArrayList(
      //      newArrayList(TAG_OPEN_START, null, "title"),
      //      newArrayList(TAG_OPEN_END),
      //      newArrayList(ESCAPABLE_RAW_TEXT, "&"),
      //      newArrayList(TAG_CLOSE, null, "title"),
      //      newArrayList(EOF)
      //    ));
      //  });
      //
      //  it("should ignore other opening tags", () -> {
      //    expect(tokenizeAndHumanizeParts("<title>a<div></title>")).toEqual(newArrayList(
      //      newArrayList(TAG_OPEN_START, null, "title"),
      //      newArrayList(TAG_OPEN_END),
      //      newArrayList(ESCAPABLE_RAW_TEXT, "a<div>"),
      //      newArrayList(TAG_CLOSE, null, "title"),
      //      newArrayList(EOF)
      //    ));
      //  });
      //
      //  it("should ignore other closing tags", () -> {
      //    expect(tokenizeAndHumanizeParts("<title>a</test></title>")).toEqual(newArrayList(
      //      newArrayList(TAG_OPEN_START, null, "title"),
      //      newArrayList(TAG_OPEN_END),
      //      newArrayList(ESCAPABLE_RAW_TEXT, "a</test>"),
      //      newArrayList(TAG_CLOSE, null, "title"),
      //      newArrayList(EOF)
      //    ));
      //  });
      //
      //  it("should store the locations", () -> {
      //    expect(tokenizeAndHumanizeSourceSpans("<title>a</title>")).toEqual(newArrayList(
      //      newArrayList(TAG_OPEN_START, "<title"),
      //      newArrayList(TAG_OPEN_END, ">"),
      //      newArrayList(ESCAPABLE_RAW_TEXT, "a"),
      //      newArrayList(TAG_CLOSE, "</title>"),
      //      newArrayList(EOF, "")
      //    ));
      //  });
      //});

      /* Expansion form tests are in Angular2HtmlLexerTest class */describe("errors") {}
      describe("unicode characters") {
        it("should support unicode characters") {
          Matchers.expect(tokenizeAndHumanizeSourceSpans("<p>İ</p>")).toEqual(
            listOf(listOf(XmlTokenType.XML_START_TAG_START, "<"),
                   listOf(XmlTokenType.XML_NAME, "p"),
                   listOf(XmlTokenType.XML_TAG_END, ">"),
                   listOf(XmlTokenType.XML_DATA_CHARACTERS, "İ"),
                   listOf(XmlTokenType.XML_END_TAG_START, "</"),
                   listOf(XmlTokenType.XML_NAME, "p"),
                   listOf(XmlTokenType.XML_TAG_END, ">")))
        }
      }
    }
  }

  private fun tokenizeWithoutErrors(
    input: String, tokenizeExpansionForms: Boolean = false,
    interpolationConfig: Pair<String?, String?>? = null): List<Token> {

    //
    //  const tokenizeResult = lex.tokenize(
    //    input, "someUrl", getHtmlTagDefinition, tokenizeExpansionForms, interpolationConfig);
    //
    //  if (tokenizeResult.errors.length > 0) {
    //const errorString = tokenizeResult.errors.join("\n");
    //    throw new Error("Unexpected parse errors:\n${errorString}");
    //  }
    //
    //  return tokenizeResult.tokens;
    return Token.create(input, tokenizeExpansionForms, interpolationConfig)
  }

  private fun tokenizeAndHumanizeParts(
    input: String, tokenizeExpansionForms: Boolean = false,
    interpolationConfig: Pair<String?, String?>? = null): List<List<*>?> {
    return tokenizeWithoutErrors(input, tokenizeExpansionForms, interpolationConfig)
      .map { (type, contents) -> listOf(type, contents) }
  }

  private fun tokenizeAndHumanizeSourceSpans(input: String): List<List<*>?> {
    return tokenizeAndHumanizeParts(input)
  }

  private fun tokenizeAndHumanizeLineColumn(input: String): List<List<*>?> {
    return tokenizeWithoutErrors(input).map { (type, _, start) ->
      listOf(type, "0:$start")
    }
  }

  private fun expectReversed(collection: Collection<*>): CollectionMatcher {
    return object : CollectionMatcher(collection) {
      override fun toEqual(other: Collection<*>?) {
        if (value == null && other == null) {
          return
        }
        Expectations.expectNotNull(value, "Expected null to be equal '%s'", other)
        Expectations.expectNotNull(other, "Expected '%s' to be equal null", value)
        Expectations.expectTrue(other == value, "Expected '%s' to be equal '%s'", value, other)
      }
    }
  }

  @JvmRecord
  private data class Token(val type: IElementType, val contents: String, val start: Int, val end: Int) {
    companion object {
      fun create(input: String, tokenizeExpansionForms: Boolean, interpolationConfig: Pair<String?, String?>?): List<Token> {
        val lexer = Angular2HtmlLexer(tokenizeExpansionForms, interpolationConfig)
        val result: MutableList<Token> = ArrayList()
        lexer.start(input, 0, input.length)
        var tokenType: IElementType = TokenType.WHITE_SPACE
        while (lexer.getTokenType()?.also { tokenType = it } != null) {
          result.add(Token(tokenType, lexer.tokenText, lexer.getTokenStart(), lexer.getTokenEnd()))
          lexer.advance()
        }
        return result
      }
    }
  }
}
