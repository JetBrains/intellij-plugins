package com.dmarcotte.handlebars.parsing;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.*;

/**
 * Java representation of the validations in the tokenizer_spec.rb revision which corresponds
 * to the revision of handlesbars.l that our lexer is based on
 * (https://github.com/wycats/handlebars.js/blob/932e2970ad29b16d6d6874ad0bfb44b07b4cd765/spec/tokenizer_spec.rb)
 * <p/>
 * All the tests should be nearly identical except that we generate whitespace tokens to give IDEA a better picture
 * of the text, vs. the actual Handlebars lexer which can just toss whitespace out
 */
public class HbTokenizerSpecTest extends HbLexerTest {

  /**
   * tokenizes a simple mustache as 'OPEN ID CLOSE'
   */
  public void testSimpleMustache() {
    TokenizerResult result = tokenize("{{foo}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE);
    result.shouldBeToken(1, ID, "foo");
  }

  /**
   * supports escaping delimiters
   */
  public void testEscapingDelimiters() {
    TokenizerResult result = tokenize("{{foo}} \\{{bar}} {{baz}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE, WHITE_SPACE, ESCAPE_CHAR, CONTENT, OPEN, ID, CLOSE);

    result.shouldBeToken(5, CONTENT, "{{bar}} ");
  }

  /**
   * supports escaping multiple delimiters
   */
  public void testEscapingMultipleDelimiters() {
    TokenizerResult result = tokenize("{{foo}}    \\{{bar}} \\{{baz}}");

    result.shouldMatchTokenTypes(OPEN, ID, CLOSE, WHITE_SPACE, ESCAPE_CHAR, CONTENT, ESCAPE_CHAR, CONTENT);

    result.shouldBeToken(4, ESCAPE_CHAR, "\\");
    result.shouldBeToken(5, CONTENT, "{{bar}} ");
    result.shouldBeToken(6, ESCAPE_CHAR, "\\");
    result.shouldBeToken(7, CONTENT, "{{baz}}");
  }

  /**
   * supports escaping a triple stash
   */
  public void testEscapingTripleStash() {
    TokenizerResult result = tokenize("{{foo}} \\{{{bar}}} {{baz}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE, WHITE_SPACE, ESCAPE_CHAR, CONTENT, OPEN, ID, CLOSE);

    result.shouldBeToken(5, CONTENT, "{{{bar}}} ");
  }

  /**
   * tokenizes a simple path
   */
  public void testSimplePath() {
    TokenizerResult result = tokenize("{{foo/bar}}");
    result.shouldMatchTokenTypes(OPEN, ID, SEP, ID, CLOSE);
  }

  /**
   * allows dot notation
   */
  public void testAllowDotNotation() {
    TokenizerResult result = tokenize("{{foo.bar}}");
    result.shouldMatchTokenTypes(OPEN, ID, SEP, ID, CLOSE);
  }

  /**
   * allows path literals with []
   */
  public void testAllowPathLiteralsWithSquareBrackets() {
    TokenizerResult result = tokenize("{{foo.[bar]}}");
    result.shouldMatchTokenTypes(OPEN, ID, SEP, ID, CLOSE);
  }

  /**
   * allows multiple path literals on a line with []
   */
  public void testAllowsPathLiteralsOnLineWithSquareBrackets() {
    TokenizerResult result = tokenize("{{foo.[bar]}}{{foo.[baz]}}");
    result.shouldMatchTokenTypes(OPEN, ID, SEP, ID, CLOSE, OPEN, ID, SEP, ID, CLOSE);
  }

  /**
   * tokenizes {{.}} as OPEN ID CLOSE
   */
  public void testTokenizesOpenDotClose() {
    TokenizerResult result = tokenize("{{.}}");
    result.shouldMatchTokenTypes(OPEN, ID, CLOSE);
  }

  /**
   * tokenizes a path as 'OPEN (ID SEP)* ID CLOSE'
   */
  public void testTokenizeDoubleDotPath() {
    TokenizerResult result = tokenize("{{../foo/bar}}");
    result.shouldMatchTokenTypes(OPEN, ID, SEP, ID, SEP, ID, CLOSE);
    result.shouldBeToken(1, ID, "..");
  }

  /**
   * tokenizes a path with .. as a parent path
   */
  public void testTokenizeDoubleDotPathAsParent() {
    TokenizerResult result = tokenize("{{../foo.bar}}");
    result.shouldMatchTokenTypes(OPEN, ID, SEP, ID, SEP, ID, CLOSE);
    result.shouldBeToken(1, ID, "..");
  }

  /**
   * tokenizes a path with this/foo as OPEN ID SEP ID CLOSE
   */
  public void testTokenizesSlashAsSep() {
    TokenizerResult result = tokenize("{{this/foo}}");
    result.shouldMatchTokenTypes(OPEN, ID, SEP, ID, CLOSE);
    result.shouldBeToken(1, ID, "this");
    result.shouldBeToken(3, ID, "foo");
  }

  /**
   * tokenizes a simple mustache with spaces as 'OPEN ID CLOSE'
   */
  public void testTokenizeSimpleMustacheWithSpaces() {
    TokenizerResult result = tokenize("{{  foo  }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    result.shouldBeToken(2, ID, "foo");
  }

  /**
   * tokenizes a simple mustache with line breaks as 'OPEN ID ID CLOSE'
   */
  public void testTokenizeSimpleMustacheWithLineBreaks() {
    TokenizerResult result = tokenize("{{  foo  \n   bar }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    result.shouldBeToken(2, ID, "foo");
  }

  /**
   * tokenizes raw content as 'CONTENT'
   */
  public void testTokenizeRawContent() {
    TokenizerResult result = tokenize("foo {{ bar }} baz");
    result.shouldMatchTokenTypes(CONTENT, OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE, CONTENT);
    result.shouldBeToken(0, CONTENT, "foo ");
    result.shouldBeToken(6, CONTENT, " baz");
  }

  /**
   * tokenizes a partial as 'OPEN_PARTIAL ID CLOSE'
   */
  public void testTokenizePartial() {
    TokenizerResult result = tokenize("{{> foo}}");
    result.shouldMatchTokenTypes(OPEN_PARTIAL, WHITE_SPACE, PARTIAL_NAME, CLOSE);
  }

  /**
   * tokenizes a partial with context as 'OPEN_PARTIAL ID ID CLOSE'
   */
  public void testTokenizePartialWithMultipleIds() {
    TokenizerResult result = tokenize("{{> foo bar }}");
    result.shouldMatchTokenTypes(OPEN_PARTIAL, WHITE_SPACE, PARTIAL_NAME, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
  }

  /**
   * tokenizes a partial without spaces as 'OPEN_PARTIAL ID CLOSE'
   */
  public void testTokenizePartialWithoutSpaces() {
    TokenizerResult result = tokenize("{{>foo}}");
    result.shouldMatchTokenTypes(OPEN_PARTIAL, PARTIAL_NAME, CLOSE);
  }

  /**
   * tokenizes a partial space at the end as 'OPEN_PARTIAL ID CLOSE'
   */
  public void testTokenizePartialWithTrailingSpaces() {
    TokenizerResult result = tokenize("{{>foo  }}");
    result.shouldMatchTokenTypes(OPEN_PARTIAL, PARTIAL_NAME, WHITE_SPACE, CLOSE);
  }

  /**
   * tokenizes a comment as 'COMMENT'
   */
  public void testTokenizeComment() {
    TokenizerResult result = tokenize("foo {{! this is a comment }} bar {{ baz }}");
    result.shouldMatchTokenTypes(CONTENT, COMMENT, CONTENT, OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    // Note that we differ from tokenizer_spec.rb since it is convenient to include the comment mustaches into the comment token for the IDE
    result.shouldBeToken(1, COMMENT, "{{! this is a comment }}");
  }

  /**
   * tokenizes a block comment as 'COMMENT'
   */
  public void testTokenizeBlockComment() {
    TokenizerResult result = tokenize("foo {{!-- this is a {{comment}} --}} bar {{ baz }}");
    result.shouldMatchTokenTypes(CONTENT, COMMENT, CONTENT, OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    result.shouldBeToken(1, COMMENT, "{{!-- this is a {{comment}} --}}");
  }

  /**
   * tokenizes a block comment with whitespace as 'COMMENT'
   */
  public void testTokenizeBlockCommentWithWhitespace() {
    TokenizerResult result = tokenize("foo {{!-- this is a\n{{comment}}\n--}} bar {{ baz }}");
    result.shouldMatchTokenTypes(CONTENT, COMMENT, CONTENT, OPEN, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    result.shouldBeToken(1, COMMENT, "{{!-- this is a\n{{comment}}\n--}}");
  }

  /**
   * tokenizes open and closing blocks as 'OPEN_BLOCK ID CLOSE ... OPEN_ENDBLOCK ID CLOSE'
   */
  public void testTokenizeOpenAndCloseBlock() {
    TokenizerResult result = tokenize("{{#foo}}content{{/foo}}");
    result.shouldMatchTokenTypes(OPEN_BLOCK, ID, CLOSE, CONTENT, OPEN_ENDBLOCK, ID, CLOSE);
  }

  /**
   * tokenizes inverse sections as 'OPEN_INVERSE CLOSE'
   */
  public void testTokenizeInverseSection() {
    tokenize("{{^}}").shouldMatchTokenTypes(OPEN_INVERSE, CLOSE);

    // NOTE: we lex "else"-type inverses in a non-standard was to allow us to highlight it properly
    tokenize("{{else}}").shouldMatchTokenTypes(OPEN, ELSE, CLOSE);
    tokenize("{{ else }}").shouldMatchTokenTypes(OPEN, WHITE_SPACE, ELSE, WHITE_SPACE, CLOSE);
  }

  /**
   * tokenizes inverse sections with ID as 'OPEN_INVERSE ID CLOSE'
   */
  public void testTokenizeInverseSectionWithId() {
    TokenizerResult result = tokenize("{{^foo}}");
    result.shouldMatchTokenTypes(OPEN_INVERSE, ID, CLOSE);
    result.shouldBeToken(1, ID, "foo");
  }

  /**
   * tokenizes inverse sections with ID and spaces as 'OPEN_INVERSE ID CLOSE'
   */
  public void testTokenizeInverseSectionWithWhitespace() {
    TokenizerResult result = tokenize("{{^ foo  }}");
    result.shouldMatchTokenTypes(OPEN_INVERSE, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    result.shouldBeToken(2, ID, "foo");
  }

  /**
   * tokenizes mustaches with params as 'OPEN ID ID ID CLOSE'
   */
  public void testTokenizeMustacheWithParams() {
    TokenizerResult result = tokenize("{{ foo bar baz }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, CLOSE);
    result.shouldBeToken(2, ID, "foo");
    result.shouldBeToken(4, ID, "bar");
    result.shouldBeToken(6, ID, "baz");
  }

  /**
   * tokenizes mustaches with String params as 'OPEN ID ID STRING CLOSE'
   */
  public void testTokenizeMustacheWithStringParams() {
    TokenizerResult result = tokenize("{{ foo bar \"baz\" }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, STRING, WHITE_SPACE, CLOSE);
    result.shouldBeToken(6, STRING, "\"baz\"");
  }

  /**
   * tokenizes mustaches with String params using single quotes as 'OPEN ID ID STRING CLOSE'
   */
  public void testTokenizeMustachesWithStringParamsUsingSingleQuotes() {
    TokenizerResult result = tokenize("{{ foo bar 'baz' }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, STRING, WHITE_SPACE, CLOSE);
    result.shouldBeToken(6, STRING, "'baz'");
  }

  /**
   * tokenizes String params with spaces inside as 'STRING'
   */
  public void testTokenizeMustacheWithStringParamsWithSpaces() {
    TokenizerResult result = tokenize("{{ foo bar \"baz bat\" }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, STRING, WHITE_SPACE, CLOSE);
    result.shouldBeToken(6, STRING, "\"baz bat\"");
  }

  /**
   * tokenizes String params using single quotes with escapes quotes as 'STRING'
   */
  public void testTokenizeStringParamsUsingSingleQuotesWithEscapedQuotes() {
    TokenizerResult result = tokenize("{{ foo 'bar\\'baz' }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, STRING, WHITE_SPACE, CLOSE);
    result.shouldBeToken(4, STRING, "'bar\\'baz'");
  }

  /**
   * tokenizes String params with escapes quotes as 'STRING'
   * {{ foo "bar\"baz" }}
   */
  public void testTokenizeMustacheWithStringParamWithEscapeQuotes() {
    TokenizerResult result = tokenize("{{ foo \"bar\\\"baz\" }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, STRING, WHITE_SPACE, CLOSE);
    result.shouldBeToken(4, STRING, "\"bar\\\"baz\"");
  }

  /**
   * tokenizes numbers
   */
  public void testTokenizesNumbers() {
    TokenizerResult result = tokenize("{{ foo 1 }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, INTEGER, WHITE_SPACE, CLOSE);
    result.shouldBeToken(4, INTEGER, "1");

    result = tokenize("{{ foo -1 }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, INTEGER, WHITE_SPACE, CLOSE);
    result.shouldBeToken(4, INTEGER, "-1");
  }

  /**
   * tokenizes booleans
   */
  public void testTokenizeBooleans() {
    TokenizerResult result = tokenize("{{ foo true }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, BOOLEAN, WHITE_SPACE, CLOSE);
    result.shouldBeToken(4, BOOLEAN, "true");

    result = tokenize("{{ foo false }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, BOOLEAN, WHITE_SPACE, CLOSE);
    result.shouldBeToken(4, BOOLEAN, "false");
  }

  /**
   * tokenizes hash arguments
   */
  public void testTokenizeHashArguments() {
    TokenizerResult result = tokenize("{{ foo bar=baz }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, ID, WHITE_SPACE, CLOSE);

    result = tokenize("{{ foo bar baz=bat }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, ID, WHITE_SPACE, CLOSE);

    result = tokenize("{{ foo bar baz=1 }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, INTEGER, WHITE_SPACE, CLOSE);

    result = tokenize("{{ foo bar baz=true }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, BOOLEAN, WHITE_SPACE, CLOSE);

    result = tokenize("{{ foo bar baz=false }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, BOOLEAN, WHITE_SPACE, CLOSE);

    result = tokenize("{{ foo bar\n baz=bat }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, ID, WHITE_SPACE, CLOSE);

    result = tokenize("{{ foo bar baz=\"bat\" }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, STRING, WHITE_SPACE, CLOSE);

    result = tokenize("{{ foo bar baz=\"bat\" bam=wot }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, STRING, WHITE_SPACE, ID, EQUALS, ID,
                                 WHITE_SPACE, CLOSE);

    result = tokenize("{{ foo omg bar=\"baz\" bat=\"bam\" }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, STRING, WHITE_SPACE, ID, EQUALS, STRING,
                                 WHITE_SPACE, CLOSE);
    result.shouldBeToken(4, ID, "omg");
  }

  /**
   * tokenizes special @ identifiers
   */
  public void testSpecialDataIdentifiers() {
    TokenizerResult result = tokenize("{{ @foo }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, DATA_PREFIX, DATA, WHITE_SPACE, CLOSE);
    result.shouldBeToken(3, DATA, "foo");

    result = tokenize("{{ foo @bar }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, DATA_PREFIX, DATA, WHITE_SPACE, CLOSE);
    result.shouldBeToken(5, DATA, "bar");

    result = tokenize("{{ foo bar=@baz }}");
    result.shouldMatchTokenTypes(OPEN, WHITE_SPACE, ID, WHITE_SPACE, ID, EQUALS, DATA_PREFIX, DATA, WHITE_SPACE, CLOSE);
    result.shouldBeToken(7, DATA, "baz");
  }
}
