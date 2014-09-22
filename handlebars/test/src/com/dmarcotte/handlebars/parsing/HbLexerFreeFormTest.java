package com.dmarcotte.handlebars.parsing;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.*;

/**
 * Free form lexer tests to help develop the lexer, pin down regressions, etc.
 * <p/>
 * See {@link HbTokenizerSpecTest} for the tests based on the formal Handlebars description in its tokenizer_spec.rb
 */
public class HbLexerFreeFormTest extends HbLexerTest {
  public void testPlainMustache1() {
    TokenizerResult result = tokenize("{{mustacheContent}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE);
    result.shouldMatchTokenContent("{{", "mustacheContent", "}}");
  }

  public void testPlainMustacheWithContentPreamble() {
    TokenizerResult result = tokenize("Some content y'all {{mustacheContent}}");
    result.shouldMatchTokenTypes(CONTENT, OPEN, ID, CLOSE);
    result.shouldMatchTokenContent("Some content y'all ", "{{", "mustacheContent", "}}");
  }

  public void testNoMustaches() {
    TokenizerResult result = tokenize("Some content y'all ");
    result.shouldMatchTokenTypes(CONTENT);
    result.shouldMatchTokenContent("Some content y'all ");
  }

  public void testPlainMustacheWithWhitespace() {
    TokenizerResult result = tokenize("{{\tmustacheContent }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    result.shouldMatchTokenContent("{{", "\t", "mustacheContent", " ", "}}");
  }

  public void testComment() {
    TokenizerResult result = tokenize("{{! this is a comment=true }}");
    result.shouldMatchTokenTypes(COMMENT);
    result.shouldMatchTokenContent("{{! this is a comment=true }}");
  }

  public void testContentAfterComment() {
    TokenizerResult result = tokenize("{{! this is a comment=true }}This here be non-Hb content!");
    result.shouldMatchTokenTypes(COMMENT, CONTENT);
    result.shouldMatchTokenContent("{{! this is a comment=true }}", "This here be non-Hb content!");
  }

  public void testSquareBracketStuff() {
    TokenizerResult result = tokenize("{{test\t[what] }}");
    result.shouldMatchTokenTypes(OPEN, ID, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    result.shouldMatchTokenContent("{{", "test", "\t", "[what]", " ", "}}");
  }

  public void testSeparator() {
    TokenizerResult result = tokenize("{{dis/connected}}");
    result.shouldMatchTokenTypes(OPEN, ID, SEP, ID, CLOSE);
    result.shouldMatchTokenContent("{{", "dis", "/", "connected", "}}");
  }

  public void testSimplePartial() {
    TokenizerResult result = tokenize("{{>partialId}}");
    result.shouldMatchTokenTypes(OPEN_PARTIAL, ID, CLOSE);
    result.shouldMatchTokenContent("{{>", "partialId", "}}");
  }

  public void testSimpleUnescaped() {
    TokenizerResult result = tokenize("{{{partialId}}}");
    result.shouldMatchTokenTypes(OPEN_UNESCAPED, ID, CLOSE_UNESCAPED);
    result.shouldMatchTokenContent("{{{", "partialId", "}}}");
  }

  /**
   * Lexer was not interpreting mustaches ("{{") properly, resulting in bad behavior when perfectly
   * reasonable CONTENT contained a single "{" (i.e. when writing js in a script tag for instance),
   * the "{" was being lexed as INVALID.
   * <p/>
   * See https://github.com/dmarcotte/idea-handlebars/issues/4
   */
  public void testContentWithSingleBrace() {
    TokenizerResult result = tokenize("{");
    result.shouldMatchTokenTypes(CONTENT);
    result.shouldMatchTokenContent("{");

    // also check an example with more context just to be sure
    result = tokenize("{{stache}}\n<script type=\"text/javascript\">function test() { alert('hotttness') }</script>{{afterStache}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE, CONTENT, OPEN, ID, CLOSE);
  }

  public void testRegularMustacheFollowedByUnescaped() {
    TokenizerResult result = tokenize("{{reg}}{{{unesc}}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE, OPEN_UNESCAPED, ID, CLOSE_UNESCAPED);
    result.shouldMatchTokenContent("{{", "reg", "}}", "{{{", "unesc", "}}}");
  }

  public void testTooManyMustaches() {
    TokenizerResult result = tokenize("{{{{");
    result.shouldMatchTokenTypes(OPEN_UNESCAPED, INVALID);
    result.shouldMatchTokenContent("{{{", "{");
  }

  public void testTooManyCommentCloseStaches() {
    TokenizerResult result = tokenize("{{! ZOMG! A comment!!! }}}");
    result.shouldMatchTokenTypes(COMMENT, CONTENT);
    result.shouldMatchTokenContent("{{! ZOMG! A comment!!! }}", "}");

    result = tokenize("{{! ZOMG! A comment!!! }}}}");
    result.shouldMatchTokenTypes(COMMENT, CONTENT);
    result.shouldMatchTokenContent("{{! ZOMG! A comment!!! }}", "}}");
  }

  public void testUnclosedSimpleComment() {
    TokenizerResult result = tokenize("{{! unclosed comment");

    result.shouldMatchTokenTypes(UNCLOSED_COMMENT);
    result.shouldBeToken(0, UNCLOSED_COMMENT, "{{! unclosed comment");
  }

  public void testUnclosedBlockComment() {
    TokenizerResult result = tokenize("{{!-- unclosed comment {{foo}}");

    result.shouldMatchTokenTypes(UNCLOSED_COMMENT);
    result.shouldBeToken(0, UNCLOSED_COMMENT, "{{!-- unclosed comment {{foo}}");
  }

  public void testEmptyComment() {
    TokenizerResult result = tokenize("{{!}}");

    result.shouldMatchTokenTypes(COMMENT);
    result.shouldBeToken(0, COMMENT, "{{!}}");
  }

  public void testEscapedMustacheAtEOF() {
    TokenizerResult result = tokenize("\\{{escaped}}");

    result.shouldMatchTokenTypes(ESCAPE_CHAR, CONTENT);
    result.shouldMatchTokenContent("\\", "{{escaped}}");
  }


  public void testEscapedMustacheWithNoFollowingStache() {
    TokenizerResult result = tokenize("\\{{escaped}} <div/>");

    result.shouldMatchTokenTypes(ESCAPE_CHAR, CONTENT);
    result.shouldMatchTokenContent("\\", "{{escaped}} <div/>");
  }

  public void testDataWithInvalidIdentifier() {
    TokenizerResult result = tokenize("{{ @  }}");

    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, DATA_PREFIX, WHITE_SPACE, CLOSE);

    result = tokenize("{{ @%foo  }}");

    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, DATA_PREFIX, INVALID, ID, WHITE_SPACE, CLOSE);
  }

  public void testOpenWhitespaceStrip() {
    TokenizerResult result = tokenize("{{~foo}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE);

    result = tokenize("{{~ foo }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
  }

  public void testOpenPartialWhitespaceStrip() {
    TokenizerResult result = tokenize("{{~>foo}}");
    result.shouldMatchTokenTypes(OPEN_PARTIAL, ID, CLOSE);

    result = tokenize("{{~> foo }}");
    result.shouldMatchTokenTypes(OPEN_PARTIAL, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
  }

  public void testOpenBlockWhitespaceStrip() {
    TokenizerResult result = tokenize("{{~#foo}}");
    result.shouldMatchTokenTypes(OPEN_BLOCK, ID, CLOSE);

    result = tokenize("{{~# foo }}");
    result.shouldMatchTokenTypes(OPEN_BLOCK, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
  }

  public void testOpenEndblockWhitespaceStrip() {
    TokenizerResult result = tokenize("{{~/foo}}");
    result.shouldMatchTokenTypes(OPEN_ENDBLOCK, ID, CLOSE);

    result = tokenize("{{~/ foo }}");
    result.shouldMatchTokenTypes(OPEN_ENDBLOCK, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
  }

  public void testOpenInverseWhitespaceStrip() {
    TokenizerResult result = tokenize("{{~^foo}}");
    result.shouldMatchTokenTypes(OPEN_INVERSE, ID, CLOSE);

    result = tokenize("{{~^ foo }}");
    result.shouldMatchTokenTypes(OPEN_INVERSE, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);

    result = tokenize("{{~else foo}}");
    result.shouldMatchTokenTypes(OPEN, ELSE, WHITE_SPACE, ID, CLOSE);

    result = tokenize("{{~else foo }}");
    result.shouldMatchTokenTypes(OPEN, ELSE, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
  }

  public void testOpenUnescapedWhitespaceStrip() {
    TokenizerResult result = tokenize("{{~{foo}}}");
    result.shouldMatchTokenTypes(OPEN_UNESCAPED, ID, CLOSE_UNESCAPED);

    result = tokenize("{{~{ foo }}}");
    result.shouldMatchTokenTypes(OPEN_UNESCAPED, WHITE_SPACE, ID, WHITE_SPACE, CLOSE_UNESCAPED);
  }

  public void testCloseWhitespaceStrip() {
    TokenizerResult result = tokenize("{{foo~}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE);

    result = tokenize("{{ foo ~}}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
  }

  public void testOpenCloseWhitespaceStrip() {
    TokenizerResult result = tokenize("{{~foo~}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE);

    result = tokenize("{{~ foo ~}}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
  }

  public void testCloseUnescapedWhitespaceStrip() {
    TokenizerResult result = tokenize("{{{foo}~}}");
    result.shouldMatchTokenTypes(OPEN_UNESCAPED, ID, CLOSE_UNESCAPED);

    result = tokenize("{{{ foo }~}}");
    result.shouldMatchTokenTypes(OPEN_UNESCAPED, WHITE_SPACE, ID, WHITE_SPACE, CLOSE_UNESCAPED);
  }

  public void testOpenCloseUnescapedWhitespaceStrip() {
    TokenizerResult result = tokenize("{{~{foo}~}}");
    result.shouldMatchTokenTypes(OPEN_UNESCAPED, ID, CLOSE_UNESCAPED);

    result = tokenize("{{~{ foo }~}}");
    result.shouldMatchTokenTypes(OPEN_UNESCAPED, WHITE_SPACE, ID, WHITE_SPACE, CLOSE_UNESCAPED);
  }

  public void testDecimalNumberAsMustacheParam() {
    TokenizerResult result = tokenize("{{name 10.123}}");
    result.shouldMatchTokenTypes(OPEN, ID, WHITE_SPACE, NUMBER, CLOSE);
    result.shouldMatchTokenContent("{{", "name", " ", "10.123", "}}");
  }

  public void testThreeDecimalNumberAsMustacheParam() {
    TokenizerResult result = tokenize("{{name 42 10.1 42}}");
    result.shouldMatchTokenTypes(OPEN, ID, WHITE_SPACE, NUMBER, WHITE_SPACE, NUMBER, WHITE_SPACE, NUMBER, CLOSE);
    result.shouldMatchTokenContent("{{", "name", " ", "42", " ", "10.1", " ", "42", "}}");
  }

  public void testDecimalNumberAsMustacheHashParam() {
    TokenizerResult result = tokenize("{{name paramValue=10.1}}");
    result.shouldMatchTokenTypes(OPEN, ID, WHITE_SPACE, ID, EQUALS, NUMBER, CLOSE);
    result.shouldMatchTokenContent("{{", "name", " ", "paramValue", "=", "10.1", "}}");
  }
}
