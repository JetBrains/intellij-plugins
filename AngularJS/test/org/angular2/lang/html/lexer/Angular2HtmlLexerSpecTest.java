// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
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
import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static com.mscharhag.oleaster.matcher.util.Expectations.expectNotNull;
import static com.mscharhag.oleaster.matcher.util.Expectations.expectTrue;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.describe;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.it;
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
          expect(tokenizeAndHumanizeLineColumn("<t>a</t>")).toEqual(List.of(List.of(XML_START_TAG_START, "0:0"),
                                                                            List.of(XML_NAME, "0:1"),
                                                                            List.of(XML_TAG_END, "0:2"),
                                                                            List.of(XML_DATA_CHARACTERS, "0:3"),
                                                                            List.of(XML_END_TAG_START, "0:4"),
                                                                            List.of(XML_NAME, "0:6"),
                                                                            List.of(XML_TAG_END, "0:7")));
        });

        it("should work with one newline", () -> {
          expect(tokenizeAndHumanizeLineColumn("<t>\na</t>")).toEqual(List.of(List.of(XML_START_TAG_START, "0:0"),
                                                                              List.of(XML_NAME, "0:1"),
                                                                              List.of(XML_TAG_END, "0:2"),
                                                                              List.of(XML_REAL_WHITE_SPACE, "0:3"),
                                                                              List.of(XML_DATA_CHARACTERS, "0:4"),
                                                                              List.of(XML_END_TAG_START, "0:5"),
                                                                              List.of(XML_NAME, "0:7"),
                                                                              List.of(XML_TAG_END, "0:8")));
        });

        it("should work with multiple newlines", () -> {
          expect(tokenizeAndHumanizeLineColumn("<t\n>\na</t>")).toEqual(List.of(List.of(XML_START_TAG_START, "0:0"),
                                                                                List.of(XML_NAME, "0:1"),
                                                                                List.of(WHITE_SPACE, "0:2"),
                                                                                List.of(XML_TAG_END, "0:3"),
                                                                                List.of(XML_REAL_WHITE_SPACE, "0:4"),
                                                                                List.of(XML_DATA_CHARACTERS, "0:5"),
                                                                                List.of(XML_END_TAG_START, "0:6"),
                                                                                List.of(XML_NAME, "0:8"),
                                                                                List.of(XML_TAG_END, "0:9")));
        });

        it("should work with CR and LF", () -> {
          expect(tokenizeAndHumanizeLineColumn("<t\n>\r\na\r</t>")).toEqual(List.of(List.of(XML_START_TAG_START, "0:0"),
                                                                                    List.of(XML_NAME, "0:1"),
                                                                                    List.of(WHITE_SPACE, "0:2"),
                                                                                    List.of(XML_TAG_END, "0:3"),
                                                                                    List.of(XML_REAL_WHITE_SPACE, "0:4"),
                                                                                    List.of(XML_DATA_CHARACTERS, "0:6"),
                                                                                    List.of(XML_REAL_WHITE_SPACE, "0:7"),
                                                                                    List.of(XML_END_TAG_START, "0:8"),
                                                                                    List.of(XML_NAME, "0:10"),
                                                                                    List.of(XML_TAG_END, "0:11")));
        });
      });

      describe("comments", () -> {
        it("should parse comments", () -> {
          expect(tokenizeAndHumanizeParts("<!--t\ne\rs\r\nt-->")).toEqual(List.of(List.of(XML_COMMENT_START, "<!--"),
                                                                                  List.of(XML_COMMENT_CHARACTERS,
                                                                                          "t\ne\rs\r\nt"),
                                                                                  List.of(XML_COMMENT_END, "-->")));
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
          expect(tokenizeAndHumanizeSourceSpans("<!-- test ---->")).toEqual(List.of(List.of(XML_COMMENT_START, "<!--"),
                                                                                    List.of(XML_COMMENT_CHARACTERS,
                                                                                            " test --"),
                                                                                    List.of(XML_COMMENT_END, "-->")));
        });

        it("should accept comments finishing by too many dashes (odd number)", () -> {
          expect(tokenizeAndHumanizeSourceSpans("<!-- test --->")).toEqual(List.of(List.of(XML_COMMENT_START, "<!--"),
                                                                                   List.of(XML_COMMENT_CHARACTERS,
                                                                                           " test -"),
                                                                                   List.of(XML_COMMENT_END, "-->")));
        });
        it("should not parse interpolation within comment", () -> {
          expect(tokenizeAndHumanizeParts("<!-- {{ v }} -->")).toEqual(List.of(List.of(XML_COMMENT_START, "<!--"),
                                                                               List.of(XML_COMMENT_CHARACTERS,
                                                                                       " {{ v }} "),
                                                                               List.of(XML_COMMENT_END, "-->")));
        });
      });

      describe("doctype", () -> {
        it("should parse doctypes", () -> {
          expect(tokenizeAndHumanizeParts("<!doctype html>")).toEqual(List.of(List.of(XML_DOCTYPE_START, "<!doctype"),
                                                                              List.of(WHITE_SPACE, " "),
                                                                              List.of(XML_NAME, "html"),
                                                                              List.of(XML_DOCTYPE_END, ">")));
        });

        //it("should report missing end doctype", () -> {
        //  expect(tokenizeAndHumanizeErrors("<!")).toEqual(newArrayList(
        //    newArrayList(DOC_TYPE, "Unexpected character \"EOF\"", "0:2")
        //  ));
        //});
      });

      describe("CDATA", () -> {
        it("should parse CDATA", () -> {
          expect(tokenizeAndHumanizeParts("<![CDATA[t\ne\rs\r\nt]]>")).toEqual(
            List.of(List.of(XML_DATA_CHARACTERS, "<![CDATA[t"),
                    List.of(XML_REAL_WHITE_SPACE, "\n"),
                    List.of(XML_DATA_CHARACTERS, "e"),
                    List.of(XML_REAL_WHITE_SPACE, "\r"),
                    List.of(XML_DATA_CHARACTERS, "s"),
                    List.of(XML_REAL_WHITE_SPACE, "\r\n"),
                    List.of(XML_DATA_CHARACTERS, "t]]>")));
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
          expect(tokenizeAndHumanizeParts("<test>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                     List.of(XML_NAME, "test"),
                                                                     List.of(XML_TAG_END, ">")));
        });

        it("should parse namespace prefix", () -> {
          expect(tokenizeAndHumanizeParts("<ns1:test>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                         List.of(XML_NAME, "ns1:test"),
                                                                         List.of(XML_TAG_END, ">")));
        });

        it("should parse void tags", () -> {
          expect(tokenizeAndHumanizeParts("<test/>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                      List.of(XML_NAME, "test"),
                                                                      List.of(XML_EMPTY_ELEMENT_END, "/>")));
        });

        it("should allow whitespace after the tag name", () -> {
          expect(tokenizeAndHumanizeParts("<test >")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                      List.of(XML_NAME, "test"),
                                                                      List.of(WHITE_SPACE, " "),
                                                                      List.of(XML_TAG_END, ">")));
        });
      });

      describe("attributes", () -> {
        it("should parse attributes without prefix", () -> {
          expect(tokenizeAndHumanizeParts("<t a>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                    List.of(XML_NAME, "t"),
                                                                    List.of(WHITE_SPACE, " "),
                                                                    List.of(XML_NAME, "a"),
                                                                    List.of(XML_TAG_END, ">")));
        });

        it("should parse attributes with interpolation", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"{{v}}\" b=\"s{{m}}e\" c=\"s{{m//c}}e\">")).toEqual(
            List.of(List.of(XML_START_TAG_START, "<"),
                    List.of(XML_NAME, "t"),
                    List.of(WHITE_SPACE, " "),
                    List.of(XML_NAME, "a"),
                    List.of(XML_EQ, "="),
                    List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                    List.of(INTERPOLATION_START, "{{"),
                    List.of(INTERPOLATION_EXPR, "v"),
                    List.of(INTERPOLATION_END, "}}"),
                    List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                    List.of(WHITE_SPACE, " "),
                    List.of(XML_NAME, "b"),
                    List.of(XML_EQ, "="),
                    List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                    List.of(XML_ATTRIBUTE_VALUE_TOKEN, "s"),
                    List.of(INTERPOLATION_START, "{{"),
                    List.of(INTERPOLATION_EXPR, "m"),
                    List.of(INTERPOLATION_END, "}}"),
                    List.of(XML_ATTRIBUTE_VALUE_TOKEN, "e"),
                    List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                    List.of(WHITE_SPACE, " "),
                    List.of(XML_NAME, "c"),
                    List.of(XML_EQ, "="),
                    List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                    List.of(XML_ATTRIBUTE_VALUE_TOKEN, "s"),
                    List.of(INTERPOLATION_START, "{{"),
                    List.of(INTERPOLATION_EXPR, "m//c"),
                    List.of(INTERPOLATION_END, "}}"),
                    List.of(XML_ATTRIBUTE_VALUE_TOKEN, "e"),
                    List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                    List.of(XML_TAG_END, ">")));
        });
        it("should not parse interpolation within tag element", () -> {
          expect(tokenizeAndHumanizeParts("<t {{v}}=12>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                           List.of(XML_NAME, "t"),
                                                                           List.of(WHITE_SPACE, " "),
                                                                           List.of(XML_NAME, "{{v}}"),
                                                                           List.of(XML_EQ, "="),
                                                                           List.of(XML_ATTRIBUTE_VALUE_TOKEN, "12"),
                                                                           List.of(XML_TAG_END, ">")));
        });

        it("should parse attributes with prefix", () -> {
          expect(tokenizeAndHumanizeParts("<t ns1:a>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                        List.of(XML_NAME, "t"),
                                                                        List.of(WHITE_SPACE, " "),
                                                                        List.of(XML_NAME, "ns1:a"),
                                                                        List.of(XML_TAG_END, ">")));
        });

        it("should parse attributes whose prefix is not valid", () -> {
          expect(tokenizeAndHumanizeParts("<t (ns1:a)>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                          List.of(XML_NAME, "t"),
                                                                          List.of(WHITE_SPACE, " "),
                                                                          List.of(XML_NAME, "(ns1:a)"),
                                                                          List.of(XML_TAG_END, ">")));
        });

        it("should parse attributes with single quote value", () -> {
          expect(tokenizeAndHumanizeParts("<t a='b'>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                        List.of(XML_NAME, "t"),
                                                                        List.of(WHITE_SPACE, " "),
                                                                        List.of(XML_NAME, "a"),
                                                                        List.of(XML_EQ, "="),
                                                                        List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER,
                                                                                "'"),
                                                                        List.of(XML_ATTRIBUTE_VALUE_TOKEN, "b"),
                                                                        List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "'"),
                                                                        List.of(XML_TAG_END, ">")));
        });

        it("should parse attributes with double quote value", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"b\">")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                          List.of(XML_NAME, "t"),
                                                                          List.of(WHITE_SPACE, " "),
                                                                          List.of(XML_NAME, "a"),
                                                                          List.of(XML_EQ, "="),
                                                                          List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER,
                                                                                  "\""),
                                                                          List.of(XML_ATTRIBUTE_VALUE_TOKEN, "b"),
                                                                          List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER,
                                                                                  "\""),
                                                                          List.of(XML_TAG_END, ">")));
        });

        it("should parse attributes with unquoted value", () -> {
          expect(tokenizeAndHumanizeParts("<t a=b>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                      List.of(XML_NAME, "t"),
                                                                      List.of(WHITE_SPACE, " "),
                                                                      List.of(XML_NAME, "a"),
                                                                      List.of(XML_EQ, "="),
                                                                      List.of(XML_ATTRIBUTE_VALUE_TOKEN, "b"),
                                                                      List.of(XML_TAG_END, ">")));
        });

        it("should allow whitespace", () -> {
          expect(tokenizeAndHumanizeParts("<t a = b >")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                         List.of(XML_NAME, "t"),
                                                                         List.of(WHITE_SPACE, " "),
                                                                         List.of(XML_NAME, "a"),
                                                                         List.of(WHITE_SPACE, " "),
                                                                         List.of(XML_EQ, "="),
                                                                         List.of(WHITE_SPACE, " "),
                                                                         List.of(XML_ATTRIBUTE_VALUE_TOKEN, "b"),
                                                                         List.of(WHITE_SPACE, " "),
                                                                         List.of(XML_TAG_END, ">")));
        });

        it("should parse attributes with entities in values", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"&#65;&#x41;\">")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                                    List.of(XML_NAME, "t"),
                                                                                    List.of(WHITE_SPACE, " "),
                                                                                    List.of(XML_NAME, "a"),
                                                                                    List.of(XML_EQ, "="),
                                                                                    List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                                                                                    List.of(XML_CHAR_ENTITY_REF,
                                                                                            "&#65;"),
                                                                                    List.of(XML_CHAR_ENTITY_REF,
                                                                                            "&#x41;"),
                                                                                    List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                                                                                    List.of(XML_TAG_END, ">")));
        });

        it("should not decode entities without trailing \";\"", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"&amp\" b=\"c&&d\">")).toEqual(
            List.of(List.of(XML_START_TAG_START, "<"),
                    List.of(XML_NAME, "t"),
                    List.of(WHITE_SPACE, " "),
                    List.of(XML_NAME, "a"),
                    List.of(XML_EQ, "="),
                    List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                    List.of(XML_ATTRIBUTE_VALUE_TOKEN, "&amp"),
                    List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                    List.of(WHITE_SPACE, " "),
                    List.of(XML_NAME, "b"),
                    List.of(XML_EQ, "="),
                    List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                    List.of(XML_ATTRIBUTE_VALUE_TOKEN, "c&&d"),
                    List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                    List.of(XML_TAG_END, ">")));
        });

        it("should parse attributes with \"&\" in values", () -> {
          expect(tokenizeAndHumanizeParts("<t a=\"b && c &\">")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                                 List.of(XML_NAME, "t"),
                                                                                 List.of(WHITE_SPACE, " "),
                                                                                 List.of(XML_NAME, "a"),
                                                                                 List.of(XML_EQ, "="),
                                                                                 List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER, "\""),
                                                                                 List.of(XML_ATTRIBUTE_VALUE_TOKEN,
                                                                                         "b && c &"),
                                                                                 List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "\""),
                                                                                 List.of(XML_TAG_END, ">")));
        });

        it("should parse values with CR and LF", () -> {
          expect(tokenizeAndHumanizeParts("<t a='t\ne\rs\r\nt'>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                                   List.of(XML_NAME, "t"),
                                                                                   List.of(WHITE_SPACE, " "),
                                                                                   List.of(XML_NAME, "a"),
                                                                                   List.of(XML_EQ, "="),
                                                                                   List.of(XML_ATTRIBUTE_VALUE_START_DELIMITER, "'"),
                                                                                   List.of(XML_ATTRIBUTE_VALUE_TOKEN,
                                                                                           "t\ne\rs\r\nt"),
                                                                                   List.of(XML_ATTRIBUTE_VALUE_END_DELIMITER, "'"),
                                                                                   List.of(XML_TAG_END, ">")));
        });
      });

      describe("closing tags", () -> {
        it("should parse closing tags without prefix", () -> {
          expect(tokenizeAndHumanizeParts("</test>")).toEqual(List.of(List.of(XML_END_TAG_START, "</"),
                                                                      List.of(XML_NAME, "test"),
                                                                      List.of(XML_TAG_END, ">")));
        });

        it("should parse closing tags with prefix", () -> {
          expect(tokenizeAndHumanizeParts("</ns1:test>")).toEqual(List.of(List.of(XML_END_TAG_START, "</"),
                                                                          List.of(XML_NAME, "ns1:test"),
                                                                          List.of(XML_TAG_END, ">")));
        });

        it("should allow whitespace", () -> {
          expect(tokenizeAndHumanizeParts("</test >")).toEqual(List.of(List.of(XML_END_TAG_START, "</"),
                                                                       List.of(XML_NAME, "test"),
                                                                       List.of(WHITE_SPACE, " "),
                                                                       List.of(XML_TAG_END, ">")));
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
          expect(tokenizeAndHumanizeParts("a&amp;b")).toEqual(List.of(List.of(XML_DATA_CHARACTERS, "a"),
                                                                      List.of(XML_CHAR_ENTITY_REF, "&amp;"),
                                                                      List.of(XML_DATA_CHARACTERS, "b")));
        });

        it("should parse hexadecimal entities", () -> {
          expect(tokenizeAndHumanizeParts("&#x41;&#X41;")).toEqual(List.of(List.of(XML_CHAR_ENTITY_REF, "&#x41;"),
                                                                           List.of(XML_CHAR_ENTITY_REF, "&#X41;")));
        });

        it("should parse decimal entities", () -> {
          expect(tokenizeAndHumanizeParts("&#65;")).toEqual(List.of(new Object[]{
            List.of(XML_CHAR_ENTITY_REF, "&#65;")
          }));
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
          expect(tokenizeAndHumanizeParts("a")).toEqual(List.of(new Object[]{
            List.of(XML_DATA_CHARACTERS, "a")
          }));
        });

        it("should parse interpolation", () -> {
          expect(tokenizeAndHumanizeParts("{{ a }}b{{ c // comment }}")).toEqual(
            List.of(List.of(INTERPOLATION_START, "{{"),
                    List.of(INTERPOLATION_EXPR, " a "),
                    List.of(INTERPOLATION_END, "}}"),
                    List.of(XML_DATA_CHARACTERS, "b"),
                    List.of(INTERPOLATION_START, "{{"),
                    List.of(INTERPOLATION_EXPR, " c // comment "),
                    List.of(INTERPOLATION_END, "}}")));
          expect(tokenizeAndHumanizeParts("{{a}}", false)).toEqual(List.of(List.of(INTERPOLATION_START, "{{"),
                                                                           List.of(INTERPOLATION_EXPR, "a"),
                                                                           List.of(INTERPOLATION_END, "}}")));
        });
        it("should parse empty interpolation", () -> {
          expect(tokenizeAndHumanizeParts("{{}}", false)).toEqual(List.of(List.of(INTERPOLATION_START, "{{"),
                                                                          List.of(INTERPOLATION_END, "}}")));
          expect(tokenizeAndHumanizeParts("{{ }}", false)).toEqual(List.of(List.of(INTERPOLATION_START, "{{"),
                                                                           List.of(INTERPOLATION_EXPR, " "),
                                                                           List.of(INTERPOLATION_END, "}}")));
        });

        it("should parse interpolation with custom markers", () -> {
          expect(tokenizeAndHumanizeParts("{% a %}", false, pair("{%", "%}"))).toEqual(
            List.of(List.of(INTERPOLATION_START, "{%"),
                    List.of(INTERPOLATION_EXPR, " a "),
                    List.of(INTERPOLATION_END, "%}")));
        });

        it("should parse empty interpolation with custom markers", () -> {
          expect(tokenizeAndHumanizeParts("{%%}", false, pair("{%", "%}"))).toEqual(
            List.of(List.of(INTERPOLATION_START, "{%"),
                    List.of(INTERPOLATION_END, "%}")));
          expect(tokenizeAndHumanizeParts("{% %}", false, pair("{%", "%}"))).toEqual(
            List.of(List.of(INTERPOLATION_START, "{%"),
                    List.of(INTERPOLATION_EXPR, " "),
                    List.of(INTERPOLATION_END, "%}")));
        });

        it("should parse interpolation with entities", () -> {
          expect(tokenizeAndHumanizeParts("{{&lt;}}", false)).toEqual(List.of(List.of(INTERPOLATION_START, "{{"),
                                                                              List.of(INTERPOLATION_EXPR, "&lt;"),
                                                                              List.of(INTERPOLATION_END, "}}")));
          expect(tokenizeAndHumanizeParts("{{&xee12;}}", false)).toEqual(List.of(List.of(INTERPOLATION_START, "{{"),
                                                                                 List.of(INTERPOLATION_EXPR, "&xee12;"),
                                                                                 List.of(INTERPOLATION_END, "}}")));
        });

        it("should handle CR & LF", () -> {
          expect(tokenizeAndHumanizeParts("t\ne\rs\r\nt")).toEqual(List.of(List.of(XML_DATA_CHARACTERS, "t"),
                                                                           List.of(XML_REAL_WHITE_SPACE, "\n"),
                                                                           List.of(XML_DATA_CHARACTERS, "e"),
                                                                           List.of(XML_REAL_WHITE_SPACE, "\r"),
                                                                           List.of(XML_DATA_CHARACTERS, "s"),
                                                                           List.of(XML_REAL_WHITE_SPACE, "\r\n"),
                                                                           List.of(XML_DATA_CHARACTERS, "t")));
        });

        it("should parse entities", () -> {
          expect(tokenizeAndHumanizeParts("a&amp;b")).toEqual(List.of(List.of(XML_DATA_CHARACTERS, "a"),
                                                                      List.of(XML_CHAR_ENTITY_REF, "&amp;"),
                                                                      List.of(XML_DATA_CHARACTERS, "b")));
        });

        it("should parse text starting with \"&\"", () -> {
          expect(tokenizeAndHumanizeParts("a && b &")).toEqual(List.of(List.of(XML_DATA_CHARACTERS, "a"),
                                                                       List.of(XML_REAL_WHITE_SPACE, " "),
                                                                       List.of(XML_DATA_CHARACTERS, "&&"),
                                                                       List.of(XML_REAL_WHITE_SPACE, " "),
                                                                       List.of(XML_DATA_CHARACTERS, "b"),
                                                                       List.of(XML_REAL_WHITE_SPACE, " "),
                                                                       List.of(XML_DATA_CHARACTERS, "&")));
        });

        it("should allow \"<\" in text nodes", () -> {
          expect(tokenizeAndHumanizeParts("{{ a < b ? c : d }}")).toEqual(List.of(List.of(INTERPOLATION_START, "{{"),
                                                                                  List.of(INTERPOLATION_EXPR,
                                                                                          " a < b ? c : d "),
                                                                                  List.of(INTERPOLATION_END, "}}")));

          //expect(tokenizeAndHumanizeSourceSpans("<p>a<b</p>")).toEqual(newArrayList(
          //  newArrayList(XML_START_TAG_START, "<"),
          //  newArrayList(XML_NAME, "p"),
          //  newArrayList(XML_TAG_END, ">"),
          //  newArrayList(XML_DATA_CHARACTERS, "a<b"),
          //  newArrayList(XML_END_TAG_START, "</"),
          //  newArrayList(XML_NAME, "p"),
          //  newArrayList(XML_TAG_END, ">")
          //));

          expect(tokenizeAndHumanizeParts("< a>")).toEqual(List.of(List.of(XML_DATA_CHARACTERS, "<"),
                                                                   List.of(XML_REAL_WHITE_SPACE, " "),
                                                                   List.of(XML_DATA_CHARACTERS, "a>")));
        });

        it("should parse valid start tag in interpolation", () -> {
          expect(tokenizeAndHumanizeParts("{{ a <b && c > d }}")).toEqual(List.of(List.of(XML_DATA_CHARACTERS, "{{"),
                                                                                  List.of(XML_REAL_WHITE_SPACE, " "),
                                                                                  List.of(XML_DATA_CHARACTERS, "a"),
                                                                                  List.of(XML_REAL_WHITE_SPACE, " "),
                                                                                  List.of(XML_START_TAG_START, "<"),
                                                                                  List.of(XML_NAME, "b"),
                                                                                  List.of(WHITE_SPACE, " "),
                                                                                  List.of(XML_NAME, "&&"),
                                                                                  List.of(WHITE_SPACE, " "),
                                                                                  List.of(XML_NAME, "c"),
                                                                                  List.of(WHITE_SPACE, " "),
                                                                                  List.of(XML_TAG_END, ">"),
                                                                                  List.of(XML_REAL_WHITE_SPACE, " "),
                                                                                  List.of(XML_DATA_CHARACTERS, "d"),
                                                                                  List.of(XML_REAL_WHITE_SPACE, " "),
                                                                                  List.of(XML_DATA_CHARACTERS, "}}")));
          expect(tokenizeAndHumanizeParts("{{<b>}}")).toEqual(List.of(List.of(XML_DATA_CHARACTERS, "{{"),
                                                                      List.of(XML_START_TAG_START, "<"),
                                                                      List.of(XML_NAME, "b"),
                                                                      List.of(XML_TAG_END, ">"),
                                                                      List.of(XML_DATA_CHARACTERS, "}}")));
        });

        it("should be able to escape {", () -> {
          expect(tokenizeAndHumanizeParts("{{ \"{\" }}")).toEqual(List.of(List.of(INTERPOLATION_START, "{{"),
                                                                          List.of(INTERPOLATION_EXPR, " \"{\" "),
                                                                          List.of(INTERPOLATION_END, "}}")));
        });

        it("should be able to escape {{", () -> {
          expect(tokenizeAndHumanizeParts("{{ \"{{\" }}")).toEqual(List.of(List.of(INTERPOLATION_START, "{{"),
                                                                           List.of(INTERPOLATION_EXPR, " \"{{\" "),
                                                                           List.of(INTERPOLATION_END, "}}")));
        });

        it("should treat expansion form as text when they are not parsed", () -> {
          expect(tokenizeAndHumanizeParts("<span>{a, b, =4 {c}}</span>", false)).toEqual(
            List.of(List.of(XML_START_TAG_START, "<"),
                    List.of(XML_NAME, "span"),
                    List.of(XML_TAG_END, ">"),
                    List.of(XML_DATA_CHARACTERS, "{a,"),
                    List.of(XML_REAL_WHITE_SPACE, " "),
                    List.of(XML_DATA_CHARACTERS, "b,"),
                    List.of(XML_REAL_WHITE_SPACE, " "),
                    List.of(XML_DATA_CHARACTERS, "=4"),
                    List.of(XML_REAL_WHITE_SPACE, " "),
                    List.of(XML_DATA_CHARACTERS, "{c}}"),
                    List.of(XML_END_TAG_START, "</"),
                    List.of(XML_NAME, "span"),
                    List.of(XML_TAG_END, ">")));
        });
      });

      describe("raw text", () -> {
        it("should parse text", () -> {
          expectReversed(tokenizeAndHumanizeParts("<script>t\ne\rs\r\nt</script>")).toEqual(
            List.of(List.of(XML_START_TAG_START, "<"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">"),
                    List.of(JS_EMBEDDED_CONTENT, "t\ne\rs\r\nt"),
                    List.of(XML_END_TAG_START, "</"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">")));
        });

        it("should not detect entities", () -> {
          expectReversed(tokenizeAndHumanizeParts("<script>&amp;</SCRIPT>")).toEqual(
            List.of(List.of(XML_START_TAG_START, "<"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">"),
                    List.of(JS_EMBEDDED_CONTENT, "&amp;"),
                    List.of(XML_END_TAG_START, "</"),
                    List.of(XML_NAME, "SCRIPT"),
                    List.of(XML_TAG_END, ">")));
        });

        it("should ignore other opening tags", () -> {
          expectReversed(tokenizeAndHumanizeParts("<script>a<div></script>")).toEqual(
            List.of(List.of(XML_START_TAG_START, "<"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">"),
                    List.of(JS_EMBEDDED_CONTENT, "a<div>"),
                    List.of(XML_END_TAG_START, "</"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">")));
        });

        it("should ignore other closing tags", () -> {
          expectReversed(tokenizeAndHumanizeParts("<script>a</test></script>")).toEqual(
            List.of(List.of(XML_START_TAG_START, "<"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">"),
                    List.of(JS_EMBEDDED_CONTENT, "a</test>"),
                    List.of(XML_END_TAG_START, "</"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">")));
        });

        it("should store the locations", () -> {
          expectReversed(tokenizeAndHumanizeSourceSpans("<script>a</script>")).toEqual(
            List.of(List.of(XML_START_TAG_START, "<"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">"),
                    List.of(JS_EMBEDDED_CONTENT, "a"),
                    List.of(XML_END_TAG_START, "</"),
                    List.of(XML_NAME, "script"),
                    List.of(XML_TAG_END, ">")));
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
          expect(tokenizeAndHumanizeSourceSpans("<p>İ</p>")).toEqual(List.of(List.of(XML_START_TAG_START, "<"),
                                                                             List.of(XML_NAME, "p"),
                                                                             List.of(XML_TAG_END, ">"),
                                                                             List.of(XML_DATA_CHARACTERS, "İ"),
                                                                             List.of(XML_END_TAG_START, "</"),
                                                                             List.of(XML_NAME, "p"),
                                                                             List.of(XML_TAG_END, ">")));
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
    return ContainerUtil.map(tokenizeWithoutErrors(input, tokenizeExpansionForms, interpolationConfig),
                             token -> List.of(token.type, token.contents));
  }

  private static List<List<?>> tokenizeAndHumanizeSourceSpans(String input) {
    return tokenizeAndHumanizeParts(input);
  }

  private static List<List<?>> tokenizeAndHumanizeLineColumn(String input) {
    return ContainerUtil.map(tokenizeWithoutErrors(input),
                             token -> List.of(token.type, "0:" + token.start));
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

  @SuppressWarnings({"NewClassNamingConvention"})
  private record Token(IElementType type, String contents, int start, int end) {
    public static List<Token> create(String input, boolean tokenizeExpansionForms, Pair<String, String> interpolationConfig) {
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
