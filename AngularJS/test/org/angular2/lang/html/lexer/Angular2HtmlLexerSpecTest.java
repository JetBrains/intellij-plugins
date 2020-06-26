// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.mscharhag.oleaster.matcher.matchers.CollectionMatcher;
import com.mscharhag.oleaster.runner.OleasterRunner;
import org.angular2.lang.OleasterTestUtil;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.util.containers.ContainerUtil.newArrayList;
import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static com.mscharhag.oleaster.matcher.util.Expectations.expectNotNull;
import static com.mscharhag.oleaster.matcher.util.Expectations.expectTrue;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.describe;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.it;
import static java.util.stream.Collectors.toList;
import static org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR;
import static org.angular2.lang.html.lexer.Angular2HtmlTokenTypes.*;

@SuppressWarnings({"CodeBlock2Expr", "JUnitTestCaseWithNoTests"})
@RunWith(OleasterRunner.class)
public class Angular2HtmlLexerSpecTest {

  private static final Object JS_EMBEDDED_CONTENT = new Object() {
    @Override
    public boolean equals(Object obj) {
      return obj != null && obj.getClass().getName().endsWith(".JSEmbeddedContentElementType");
    }
  };

  static {
    describe("HtmlLexer", () -> {

      OleasterTestUtil.bootstrapLightPlatform();

      describe("line/column numbers", () -> {
        it("should work without newlines", () -> {
          expect(tokenizeAndHumanizeLineColumn("<t>a</t>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "0:0"),
            newArrayList(XML_NAME, "0:1"),
            newArrayList(XML_TAG_END, "0:2"),
            newArrayList(XML_DATA_CHARACTERS, "0:3"),
            newArrayList(XML_END_TAG_START, "0:4"),
            newArrayList(XML_NAME, "0:6"),
            newArrayList(XML_TAG_END, "0:7")
          ));
        });

        it("should work with one newline", () -> {
          expect(tokenizeAndHumanizeLineColumn("<t>\na</t>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "0:0"),
            newArrayList(XML_NAME, "0:1"),
            newArrayList(XML_TAG_END, "0:2"),
            newArrayList(XML_REAL_WHITE_SPACE, "0:3"),
            newArrayList(XML_DATA_CHARACTERS, "0:4"),
            newArrayList(XML_END_TAG_START, "0:5"),
            newArrayList(XML_NAME, "0:7"),
            newArrayList(XML_TAG_END, "0:8")
          ));
        });

        it("should work with multiple newlines", () -> {
          expect(tokenizeAndHumanizeLineColumn("<t\n>\na</t>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "0:0"),
            newArrayList(XML_NAME, "0:1"),
            newArrayList(WHITE_SPACE, "0:2"),
            newArrayList(XML_TAG_END, "0:3"),
            newArrayList(XML_REAL_WHITE_SPACE, "0:4"),
            newArrayList(XML_DATA_CHARACTERS, "0:5"),
            newArrayList(XML_END_TAG_START, "0:6"),
            newArrayList(XML_NAME, "0:8"),
            newArrayList(XML_TAG_END, "0:9")
          ));
        });

        it("should work with CR and LF", () -> {
          expect(tokenizeAndHumanizeLineColumn("<t\n>\r\na\r</t>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "0:0"),
            newArrayList(XML_NAME, "0:1"),
            newArrayList(WHITE_SPACE, "0:2"),
            newArrayList(XML_TAG_END, "0:3"),
            newArrayList(XML_REAL_WHITE_SPACE, "0:4"),
            newArrayList(XML_DATA_CHARACTERS, "0:6"),
            newArrayList(XML_REAL_WHITE_SPACE, "0:7"),
            newArrayList(XML_END_TAG_START, "0:8"),
            newArrayList(XML_NAME, "0:10"),
            newArrayList(XML_TAG_END, "0:11")
          ));
        });
      });

      describe("comments", () -> {
        it("should parse comments", () -> {
          expect(tokenizeAndHumanizeParts("<!--t\ne\rs\r\nt-->")).toEqual(newArrayList(
            newArrayList(XML_COMMENT_START, "<!--"),
            newArrayList(XML_COMMENT_CHARACTERS, "t\ne\rs\r\nt"),
            newArrayList(XML_COMMENT_END, "-->")
          ));
        });

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

        it("should accept comments finishing by too many dashes (even number)", () -> {
          expect(tokenizeAndHumanizeSourceSpans("<!-- test ---->")).toEqual(newArrayList(
            newArrayList(XML_COMMENT_START, "<!--"),
            newArrayList(XML_COMMENT_CHARACTERS, " test --"),
            newArrayList(XML_COMMENT_END, "-->")
          ));
        });

        it("should accept comments finishing by too many dashes (odd number)", () -> {
          expect(tokenizeAndHumanizeSourceSpans("<!-- test --->")).toEqual(newArrayList(
            newArrayList(XML_COMMENT_START, "<!--"),
            newArrayList(XML_COMMENT_CHARACTERS, " test -"),
            newArrayList(XML_COMMENT_END, "-->")
          ));
        });
        it("should not parse interpolation within comment", () -> {
          expect(tokenizeAndHumanizeParts("<!-- {{ v }} -->")).toEqual(newArrayList(
            newArrayList(XML_COMMENT_START, "<!--"),
            newArrayList(XML_COMMENT_CHARACTERS, " {{ v }} "),
            newArrayList(XML_COMMENT_END, "-->")
          ));
        });
      });

      describe("doctype", () -> {
        it("should parse doctypes", () -> {
          expect(tokenizeAndHumanizeParts("<!doctype html>")).toEqual(newArrayList(
            newArrayList(XML_DOCTYPE_START, "<!doctype"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "html"),
            newArrayList(XML_DOCTYPE_END, ">")
          ));
        });

        //it("should report missing end doctype", () -> {
        //  expect(tokenizeAndHumanizeErrors("<!")).toEqual(newArrayList(
        //    newArrayList(DOC_TYPE, "Unexpected character \"EOF\"", "0:2")
        //  ));
        //});
      });

      describe("CDATA", () -> {
        it("should parse CDATA", () -> {
          expect(tokenizeAndHumanizeParts("<![CDATA[t\ne\rs\r\nt]]>")).toEqual(newArrayList(
            newArrayList(XML_DATA_CHARACTERS, "<![CDATA[t"),
            newArrayList(XML_REAL_WHITE_SPACE, "\n"),
            newArrayList(XML_DATA_CHARACTERS, "e"),
            newArrayList(XML_REAL_WHITE_SPACE, "\r"),
            newArrayList(XML_DATA_CHARACTERS, "s"),
            newArrayList(XML_REAL_WHITE_SPACE, "\r\n"),
            newArrayList(XML_DATA_CHARACTERS, "t]]>")
          ));
        });

        //it("should report <![ without CDATA[", () -> {
        //  expect(tokenizeAndHumanizeErrors("<![a")).toEqual(newArrayList(
        //    newArrayList(CDATA_START, "Unexpected character \"a\"", "0:3")
        //  ));
        //});
        //
        //it("should report missing end cdata", () -> {
        //  expect(tokenizeAndHumanizeErrors("<![CDATA[")).toEqual(newArrayList(
        //    newArrayList(XML_DATA_CHARACTERS, "Unexpected character \"EOF\"", "0:9")
        //  ));
        //});
      });

      describe("open tags", () -> {
        it("should parse open tags without prefix", () -> {
          expect(tokenizeAndHumanizeParts("<test>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "test"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse namespace prefix", () -> {
          expect(tokenizeAndHumanizeParts("<ns1:test>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "ns1:test"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse void tags", () -> {
          expect(tokenizeAndHumanizeParts("<test/>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "test"),
            newArrayList(XML_EMPTY_ELEMENT_END, "/>")
          ));
        });

        it("should allow whitespace after the tag name", () -> {
          expect(tokenizeAndHumanizeParts("<test >")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "test"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_TAG_END, ">")
          ));
        });
      });

      describe("attributes", () -> {
        it("should parse attributes without prefix", () -> {
          expect(tokenizeAndHumanizeParts("<t a>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse attributes with interpolation", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"{{v}}\" b=\"s{{m}}e\" c=\"s{{m//c}}e\">")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, "v"),
            newArrayList(INTERPOLATION_END, "}}"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "b"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "s"),
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, "m"),
            newArrayList(INTERPOLATION_END, "}}"),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "e"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "c"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "s"),
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, "m//c"),
            newArrayList(INTERPOLATION_END, "}}"),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "e"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
            newArrayList(XML_TAG_END, ">")
          ));
        });
        it("should not parse interpolation within tag element", () -> {
          expect(tokenizeAndHumanizeParts("<t {{v}}=12>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "{{v}}"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "12"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse attributes with prefix", () -> {
          expect(tokenizeAndHumanizeParts("<t ns1:a>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "ns1:a"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse attributes whose prefix is not valid", () -> {
          expect(tokenizeAndHumanizeParts("<t (ns1:a)>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "(ns1:a)"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse attributes with single quote value", () -> {
          expect(tokenizeAndHumanizeParts("<t a='b'>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "'"),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "b"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "'"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse attributes with double quote value", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"b\">")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "b"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse attributes with unquoted value", () -> {
          expect(tokenizeAndHumanizeParts("<t a=b>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "b"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should allow whitespace", () -> {
          expect(tokenizeAndHumanizeParts("<t a = b >")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_EQ, "="),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "b"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse attributes with entities in values", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"&#65;&#x41;\">")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
            newArrayList(XML_CHAR_ENTITY_REF, "&#65;"),
            newArrayList(XML_CHAR_ENTITY_REF, "&#x41;"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should not decode entities without trailing \";\"", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"&amp\" b=\"c&&d\">")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "&amp"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "b"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "c&&d"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse attributes with \"&\" in values", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"b && c &\">")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "b && c &"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse values with CR and LF", () -> {
          expect(tokenizeAndHumanizeParts("<t a='t\ne\rs\r\nt'>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "t"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "a"),
            newArrayList(XML_EQ, "="),
            newArrayList(XML_ATTRIBUTE_VALUE_START_DELIMITER, "'"),
            newArrayList(XML_ATTRIBUTE_VALUE_TOKEN, "t\ne\rs\r\nt"),
            newArrayList(XML_ATTRIBUTE_VALUE_END_DELIMITER, "'"),
            newArrayList(XML_TAG_END, ">")
          ));
        });
      });

      describe("closing tags", () -> {
        it("should parse closing tags without prefix", () -> {
          expect(tokenizeAndHumanizeParts("</test>")).toEqual(newArrayList(
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "test"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should parse closing tags with prefix", () -> {
          expect(tokenizeAndHumanizeParts("</ns1:test>")).toEqual(newArrayList(
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "ns1:test"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should allow whitespace", () -> {
          expect(tokenizeAndHumanizeParts("</test >")).toEqual(newArrayList(
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "test"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        //it("should report missing name after </", () -> {
        //  expect(tokenizeAndHumanizeErrors("</")).toEqual(newArrayList(
        //    newArrayList(TAG_CLOSE, "Unexpected character \"EOF\"", "0:2")
        //  ));
        //});
        //
        //it("should report missing >", () -> {
        //  expect(tokenizeAndHumanizeErrors("</test")).toEqual(newArrayList(
        //    newArrayList(TAG_CLOSE, "Unexpected character \"EOF\"", "0:6")
        //  ));
        //});
      });

      describe("entities", () -> {
        it("should parse named entities", () -> {
          expect(tokenizeAndHumanizeParts("a&amp;b")).toEqual(newArrayList(
            newArrayList(XML_DATA_CHARACTERS, "a"),
            newArrayList(XML_CHAR_ENTITY_REF, "&amp;"),
            newArrayList(XML_DATA_CHARACTERS, "b")
          ));
        });

        it("should parse hexadecimal entities", () -> {
          expect(tokenizeAndHumanizeParts("&#x41;&#X41;")).toEqual(newArrayList(
            newArrayList(XML_CHAR_ENTITY_REF, "&#x41;"),
            newArrayList(XML_CHAR_ENTITY_REF, "&#X41;")
          ));
        });

        it("should parse decimal entities", () -> {
          expect(tokenizeAndHumanizeParts("&#65;")).toEqual(newArrayList(
            new Object[]{
              newArrayList(XML_CHAR_ENTITY_REF, "&#65;")
            }
          ));
        });

        //it("should report malformed/unknown entities", () -> {
        //  expect(tokenizeAndHumanizeErrors("&tbo;")).toEqual(newArrayList(newArrayList(
        //    NG_TEXT,
        //    "Unknown entity \"tbo\" - use the \"&#<decimal>;\" or  \"&#x<hex>;\" syntax", "0:0"
        //  )));
        //  expect(tokenizeAndHumanizeErrors("&#asdf;")).toEqual(newArrayList(
        //    newArrayList(NG_TEXT, "Unexpected character \"s\"", "0:3")
        //  ));
        //  expect(tokenizeAndHumanizeErrors("&#xasdf;")).toEqual(newArrayList(
        //    newArrayList(NG_TEXT, "Unexpected character \"s\"", "0:4")
        //  ));
        //
        //  expect(tokenizeAndHumanizeErrors("&#xABC")).toEqual(newArrayList(
        //    newArrayList(NG_TEXT, "Unexpected character \"EOF\"", "0:6")
        //  ));
        //});
      });

      describe("regular text", () -> {
        it("should parse text", () -> {
          expect(tokenizeAndHumanizeParts("a")).toEqual(newArrayList(
            new Object[]{
              newArrayList(XML_DATA_CHARACTERS, "a")
            }
          ));
        });

        it("should parse interpolation", () -> {
          expect(tokenizeAndHumanizeParts("{{ a }}b{{ c // comment }}")).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, " a "),
            newArrayList(INTERPOLATION_END, "}}"),
            newArrayList(XML_DATA_CHARACTERS, "b"),
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, " c // comment "),
            newArrayList(INTERPOLATION_END, "}}")
          ));
          expect(tokenizeAndHumanizeParts("{{a}}", false)).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, "a"),
            newArrayList(INTERPOLATION_END, "}}")
          ));
        });
        it("should parse empty interpolation", () -> {
          expect(tokenizeAndHumanizeParts("{{}}", false)).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_END, "}}")
          ));
          expect(tokenizeAndHumanizeParts("{{ }}", false)).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, " "),
            newArrayList(INTERPOLATION_END, "}}")
          ));
        });

        it("should parse interpolation with custom markers", () -> {
          expect(tokenizeAndHumanizeParts("{% a %}", false, pair("{%", "%}"))).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{%"),
            newArrayList(INTERPOLATION_EXPR, " a "),
            newArrayList(INTERPOLATION_END, "%}")
          ));
        });

        it("should parse empty interpolation with custom markers", () -> {
          expect(tokenizeAndHumanizeParts("{%%}", false, pair("{%", "%}"))).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{%"),
            newArrayList(INTERPOLATION_END, "%}")
          ));
          expect(tokenizeAndHumanizeParts("{% %}", false, pair("{%", "%}"))).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{%"),
            newArrayList(INTERPOLATION_EXPR, " "),
            newArrayList(INTERPOLATION_END, "%}")
          ));
        });

        it("should parse interpolation with entities", () -> {
          expect(tokenizeAndHumanizeParts("{{&lt;}}", false)).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, "&lt;"),
            newArrayList(INTERPOLATION_END, "}}")
          ));
          expect(tokenizeAndHumanizeParts("{{&xee12;}}", false)).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, "&xee12;"),
            newArrayList(INTERPOLATION_END, "}}")
          ));
        });

        it("should handle CR & LF", () -> {
          expect(tokenizeAndHumanizeParts("t\ne\rs\r\nt")).toEqual(newArrayList(
            newArrayList(XML_DATA_CHARACTERS, "t"),
            newArrayList(XML_REAL_WHITE_SPACE, "\n"),
            newArrayList(XML_DATA_CHARACTERS, "e"),
            newArrayList(XML_REAL_WHITE_SPACE, "\r"),
            newArrayList(XML_DATA_CHARACTERS, "s"),
            newArrayList(XML_REAL_WHITE_SPACE, "\r\n"),
            newArrayList(XML_DATA_CHARACTERS, "t")
          ));
        });

        it("should parse entities", () -> {
          expect(tokenizeAndHumanizeParts("a&amp;b")).toEqual(newArrayList(
            newArrayList(XML_DATA_CHARACTERS, "a"),
            newArrayList(XML_CHAR_ENTITY_REF, "&amp;"),
            newArrayList(XML_DATA_CHARACTERS, "b")
          ));
        });

        it("should parse text starting with \"&\"", () -> {
          expect(tokenizeAndHumanizeParts("a && b &")).toEqual(newArrayList(
            newArrayList(XML_DATA_CHARACTERS, "a"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "&&"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "b"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "&")
          ));
        });

        it("should allow \"<\" in text nodes", () -> {
          expect(tokenizeAndHumanizeParts("{{ a < b ? c : d }}")).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, " a < b ? c : d "),
            newArrayList(INTERPOLATION_END, "}}")
          ));

          //expect(tokenizeAndHumanizeSourceSpans("<p>a<b</p>")).toEqual(newArrayList(
          //  newArrayList(XML_START_TAG_START, "<"),
          //  newArrayList(XML_NAME, "p"),
          //  newArrayList(XML_TAG_END, ">"),
          //  newArrayList(XML_DATA_CHARACTERS, "a<b"),
          //  newArrayList(XML_END_TAG_START, "</"),
          //  newArrayList(XML_NAME, "p"),
          //  newArrayList(XML_TAG_END, ">")
          //));

          expect(tokenizeAndHumanizeParts("< a>")).toEqual(newArrayList(
            newArrayList(XML_DATA_CHARACTERS, "<"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "a>")
          ));
        });

        it("should parse valid start tag in interpolation", () -> {
          expect(tokenizeAndHumanizeParts("{{ a <b && c > d }}")).toEqual(newArrayList(
            newArrayList(XML_DATA_CHARACTERS, "{{"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "a"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "b"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "&&"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_NAME, "c"),
            newArrayList(WHITE_SPACE, " "),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "d"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "}}")
          ));
          expect(tokenizeAndHumanizeParts("{{<b>}}")).toEqual(newArrayList(
            newArrayList(XML_DATA_CHARACTERS, "{{"),
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "b"),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(XML_DATA_CHARACTERS, "}}")
          ));
        });

        it("should be able to escape {", () -> {
          expect(tokenizeAndHumanizeParts("{{ \"{\" }}")).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, " \"{\" "),
            newArrayList(INTERPOLATION_END, "}}")
          ));
        });

        it("should be able to escape {{", () -> {
          expect(tokenizeAndHumanizeParts("{{ \"{{\" }}")).toEqual(newArrayList(
            newArrayList(INTERPOLATION_START, "{{"),
            newArrayList(INTERPOLATION_EXPR, " \"{{\" "),
            newArrayList(INTERPOLATION_END, "}}")
          ));
        });

        it("should treat expansion form as text when they are not parsed", () -> {
          expect(tokenizeAndHumanizeParts("<span>{a, b, =4 {c}}</span>", false)).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "span"),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(XML_DATA_CHARACTERS, "{a,"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "b,"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "=4"),
            newArrayList(XML_REAL_WHITE_SPACE, " "),
            newArrayList(XML_DATA_CHARACTERS, "{c}}"),
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "span"),
            newArrayList(XML_TAG_END, ">")
          ));
        });
      });

      describe("raw text", () -> {
        it("should parse text", () -> {
          expectReversed(tokenizeAndHumanizeParts("<script>t\ne\rs\r\nt</script>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(JS_EMBEDDED_CONTENT, "t\ne\rs\r\nt"),
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should not detect entities", () -> {
          expectReversed(tokenizeAndHumanizeParts("<script>&amp;</SCRIPT>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(JS_EMBEDDED_CONTENT, "&amp;"),
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "SCRIPT"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should ignore other opening tags", () -> {
          expectReversed(tokenizeAndHumanizeParts("<script>a<div></script>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(JS_EMBEDDED_CONTENT, "a<div>"),
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should ignore other closing tags", () -> {
          expectReversed(tokenizeAndHumanizeParts("<script>a</test></script>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(JS_EMBEDDED_CONTENT, "a</test>"),
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">")
          ));
        });

        it("should store the locations", () -> {
          expectReversed(tokenizeAndHumanizeSourceSpans("<script>a</script>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(JS_EMBEDDED_CONTENT, "a"),
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "script"),
            newArrayList(XML_TAG_END, ">")
          ));
        });
      });

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

      /* Expansion form tests are in Angular2HtmlLexerTest class */

      describe("errors", () -> {
        //it("should report unescaped \"{\" on error", () -> {
        //  expect(tokenizeAndHumanizeErrors("<p>before { after</p>", true)).toEqual(newArrayList(newArrayList(
        //    XML_DATA_CHARACTERS,
        //    "Unexpected character \"EOF\" (Do you have an unescaped \"{\" in your template? Use \"{{ '{' }}\") to escape it.)",
        //    "0:21"
        //  )));
        //});
      });

      describe("unicode characters", () -> {
        it("should support unicode characters", () -> {
          expect(tokenizeAndHumanizeSourceSpans("<p>İ</p>")).toEqual(newArrayList(
            newArrayList(XML_START_TAG_START, "<"),
            newArrayList(XML_NAME, "p"),
            newArrayList(XML_TAG_END, ">"),
            newArrayList(XML_DATA_CHARACTERS, "İ"),
            newArrayList(XML_END_TAG_START, "</"),
            newArrayList(XML_NAME, "p"),
            newArrayList(XML_TAG_END, ">")
          ));
        });
      });
    });
  }

  private static List<Token> tokenizeWithoutErrors(String input) {
    return tokenizeWithoutErrors(input, false, null);
  }

  private static List<Token> tokenizeWithoutErrors(
    String input, boolean tokenizeExpansionForms,
    Pair<String, String> interpolationConfig) {

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
    return Token.create(input, tokenizeExpansionForms, interpolationConfig);
  }

  private static List<List<?>> tokenizeAndHumanizeParts(String input) {
    return tokenizeAndHumanizeParts(input, false, null);
  }

  private static List<List<?>> tokenizeAndHumanizeParts(
    String input, boolean tokenizeExpansionForms) {
    return tokenizeAndHumanizeParts(input, tokenizeExpansionForms, null);
  }

  private static List<List<?>> tokenizeAndHumanizeParts(
    String input, boolean tokenizeExpansionForms,
    Pair<String, String> interpolationConfig) {
    return tokenizeWithoutErrors(input, tokenizeExpansionForms, interpolationConfig)
      .stream()
      .map(token -> newArrayList(token.type, token.contents))
      .collect(toList());
  }

  private static List<List<?>> tokenizeAndHumanizeSourceSpans(String input) {
    return tokenizeAndHumanizeParts(input);
  }

  private static List<List<?>> tokenizeAndHumanizeLineColumn(String input) {
    return tokenizeWithoutErrors(input)
      .stream()
      .map(token -> newArrayList(token.type, "0:" + token.start))
      .collect(toList());
  }

  private static List<List<?>> tokenizeAndHumanizeErrors(String input) {
    return tokenizeAndHumanizeErrors(input, false);
  }

  private static List<List<?>> tokenizeAndHumanizeErrors(String input, boolean tokenizeExpansionForms) {
    //return lex.tokenize(input, "someUrl", getHtmlTagDefinition, tokenizeExpansionForms)
    //         .errors.map(e = > [<any > e.tokenType, e.msg, humanizeLineColumn(e.span.start)));
    return Collections.emptyList();
  }

  private static CollectionMatcher expectReversed(Collection<?> collection) {
    return new CollectionMatcher(collection) {
      @Override
      public void toEqual(Collection other) {
        if (getValue() == null && other == null) {
          return;
        }
        expectNotNull(getValue(), "Expected null to be equal '%s'", other);
        expectNotNull(other, "Expected '%s' to be equal null", getValue());
        expectTrue(other.equals(this.getValue()), "Expected '%s' to be equal '%s'", getValue(), other);
      }
    };
  }

  @SuppressWarnings("NewClassNamingConvention")
  private static final class Token {

    public final IElementType type;
    public final String contents;
    public final int start;
    public final int end;

    private Token(IElementType type, String contents, int start, int end) {
      this.type = type;
      this.contents = contents;
      this.start = start;
      this.end = end;
    }

    public static List<Token> create(String input, boolean tokenizeExpansionForms,
                                     Pair<String, String> interpolationConfig) {
      Angular2HtmlLexer lexer = new Angular2HtmlLexer(tokenizeExpansionForms, interpolationConfig);
      List<Token> result = new ArrayList<>();
      lexer.start(input, 0, input.length());
      IElementType tokenType;
      while ((tokenType = lexer.getTokenType()) != null) {
        result.add(new Token(tokenType, lexer.getTokenText(), lexer.getTokenStart(), lexer.getTokenEnd()));
        lexer.advance();
      }
      return result;
    }
  }
}
