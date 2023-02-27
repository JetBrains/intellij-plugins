// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.google.common.base.Strings;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import org.intellij.terraform.BaseLexerTestCase;
import org.intellij.terraform.hcl.HCLElementTypes;
import org.intellij.terraform.hcl.HCLParserDefinition;
import org.intellij.terraform.hcl.refactoring.HCLElementRenameValidator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class HCLLexerTest extends BaseLexerTestCase {
  @Override
  protected Lexer createLexer() {
    return HCLParserDefinition.createLexer();
  }

  @Override
  protected String getDirPath() {
    return "data/hcl/lexer";
  }

  public void testSimple() throws Exception {
    doTest("a=1", """
      ID ('a')
      = ('=')
      NUMBER ('1')""");
  }

  public void testNumberWithSuffix() throws Exception {
    doTest("a=[1k, 1Kb]", """
      ID ('a')
      = ('=')
      [ ('[')
      NUMBER ('1')
      ID ('k')
      , (',')
      WHITE_SPACE (' ')
      NUMBER ('1')
      ID ('Kb')
      ] (']')""");
  }

  public void testStringWithCurves() throws Exception {
    doTest("a=\"{}\"", """
      ID ('a')
      = ('=')
      DOUBLE_QUOTED_STRING ('"{}"')""");
  }

  public void testStringWith$() throws Exception {
    doTest("dollar=\"$\"", """
      ID ('dollar')
      = ('=')
      DOUBLE_QUOTED_STRING ('"$"')""");
  }

  public void testQuotes1() throws Exception {
    doTest("a='\"1\"'", """
      ID ('a')
      = ('=')
      SINGLE_QUOTED_STRING (''"1"'')""");
  }

  public void testQuotes2() throws Exception {
    doTest("a=\"'1'\"", """
      ID ('a')
      = ('=')
      DOUBLE_QUOTED_STRING ('"'1'"')""");
  }

  public void testTerraformIL() throws Exception {
    doTest("count = \"${count()}\"", """
      ID ('count')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${count()}"')""");
  }

  public void testTerraformILInception() throws Exception {
    doTest("count = \"${foo(${bar()})}\"", """
      ID ('count')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${foo(${bar()})}"')""");
  }

  public void testTerraformILInception2() throws Exception {
    doTest("count = \"${${}}\"", """
      ID ('count')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${${}}"')""");
  }

  public void testTerraformILWithString() throws Exception {
    doTest("count = \"${call(\"count\")}\"", """
      ID ('count')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${call("count")}"')""");
  }

  public void testTerraformILWithString2() throws Exception {
    doTest("count = '${call(\"count\")}'", """
      ID ('count')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      SINGLE_QUOTED_STRING (''${call("count")}'')""");
  }

  public void testComplicatedTerraformConfigWithILStings() throws Exception {
    doTest("container_definitions = \"${file(\"ecs-container-definitions.json\")}\"", """
      ID ('container_definitions')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${file("ecs-container-definitions.json")}"')""");
  }

  public void testUnfinishedString() throws Exception {
    doTest("a=\"x\"\"\n", """
      ID ('a')
      = ('=')
      DOUBLE_QUOTED_STRING ('"x"')
      DOUBLE_QUOTED_STRING ('"')
      WHITE_SPACE ('\\n')""");
  }

  public void testUnfinishedString2() throws Exception {
    doTest("a=\r\n\"x\"\"\r\n", """
      ID ('a')
      = ('=')
      WHITE_SPACE ('
      \\n')
      DOUBLE_QUOTED_STRING ('"x"')
      DOUBLE_QUOTED_STRING ('"')
      WHITE_SPACE ('
      \\n')""");
  }

  public void testUnfinishedStringInObjectSingleLine() throws Exception {
    doTest("a={y = \"x\"\"}", """
      ID ('a')
      = ('=')
      { ('{')
      ID ('y')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"x"')
      DOUBLE_QUOTED_STRING ('"}')""");
  }

  public void testUnfinishedStringInObjectMultiLine() throws Exception {
    doTest("a={\ny = \"x\"\"\n}", """
      ID ('a')
      = ('=')
      { ('{')
      WHITE_SPACE ('\\n')
      ID ('y')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"x"')
      DOUBLE_QUOTED_STRING ('"')
      WHITE_SPACE ('\\n')
      } ('}')""");
  }

  public void testUnfinishedStringWithBackslash() throws Exception {
    doTest("a=\"x\\\ny\"", """
      ID ('a')
      = ('=')
      DOUBLE_QUOTED_STRING ('"x\\')
      WHITE_SPACE ('\\n')
      ID ('y')
      DOUBLE_QUOTED_STRING ('"')""");
  }

  public void testUnfinishedInterpolation() throws Exception {
    doTest("a = \"${b(\"c\")}${{}}\"", """
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${b("c")}${{}}"')""");
  }

  public void testUnfinishedInterpolation2() throws Exception {
    doTest("a = \"${b(\"c\")}${{}}\"\nx=y", """
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${b("c")}${{}}"')
      WHITE_SPACE ('\\n')
      ID ('x')
      = ('=')
      ID ('y')""");
  }

  public void testMultilineString() throws Exception {
    doTest("ml=\"hello\n  world\"", """
      ID ('ml')
      = ('=')
      DOUBLE_QUOTED_STRING ('"hello')
      WHITE_SPACE ('\\n  ')
      ID ('world')
      DOUBLE_QUOTED_STRING ('"')
      """);
  }

  public void testHereDoc() throws Exception {
    doTest("""
             foo = <<EOF
             bar
             baz
             EOF""",
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')
             HD_LINE ('bar')
             HD_EOL ('\\n')
             HD_LINE ('baz')
             HD_EOL ('\\n')
             HD_MARKER ('EOF')
             """);
  }

  public void testHereDoc2() throws Exception {
    doTest("""
             foo = <<EOF
             bar
             baz
             EOF
             """,
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')
             HD_LINE ('bar')
             HD_EOL ('\\n')
             HD_LINE ('baz')
             HD_EOL ('\\n')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')""");
  }

  public void testHereDoc_Indented() throws Exception {
    doTest("""
             foo = <<-EOF
               bar
               baz
               EOF
             """,
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('-EOF')
             WHITE_SPACE ('\\n')
             HD_LINE ('  bar')
             HD_EOL ('\\n')
             HD_LINE ('  baz')
             HD_EOL ('\\n')
             HD_MARKER ('  EOF')
             WHITE_SPACE ('\\n')""");
  }

  public void testHereDoc_Indented_End() throws Exception {
    doTest("""
             foo = <<EOF
               bar
               baz
               EOF
             """,
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')
             HD_LINE ('  bar')
             HD_EOL ('\\n')
             HD_LINE ('  baz')
             HD_EOL ('\\n')
             HD_MARKER ('  EOF')
             WHITE_SPACE ('\\n')""");
  }

  public void testHereDoc_Empty() throws Exception {
    doTest("foo = <<EOF\n" +
            "EOF",
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')
             HD_MARKER ('EOF')
             """);
  }

  public void testHereDoc_EmptyLines() throws Exception {
    doTest("""
             foo = <<EOF


             EOF""",
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')
             HD_LINE ('')
             HD_EOL ('\\n')
             HD_LINE ('')
             HD_EOL ('\\n')
             HD_MARKER ('EOF')
             """);
  }

  public void testHereDoc_SingleLineEmpty() throws Exception {
    doTest("""
             foo = <<EOF

             EOF""",
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')
             HD_LINE ('')
             HD_EOL ('\\n')
             HD_MARKER ('EOF')
             """);
  }

  public void testHereDoc_Incomplete() throws Exception {
    doTest("""
             foo = <<EOF
             bar
             """,
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')
             HD_LINE ('bar')
             HD_EOL ('\\n')
             BAD_CHARACTER ('')""");
  }

  public void testHereDoc_IncompleteStart() throws Exception {
    doTest("""
             foo = <<
             bar
             """,
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             BAD_CHARACTER ('\\n')
             ID ('bar')
             WHITE_SPACE ('\\n')""");
  }

  public void testHereDoc_BackSlash_Start() throws Exception {
    doTest("""
             foo = <<EOF\\
             EOF
             """,
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             BAD_CHARACTER ('\\')
             HD_LINE ('')
             HD_EOL ('\\n')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')""");
  }

  public void testHereDoc_BackSlash_Line() throws Exception {
    doTest("""
             foo = <<EOF
             a\\
             EOF
             """,
           """
             ID ('foo')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             HD_START ('<<')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')
             HD_LINE ('a\\')
             HD_EOL ('\\n')
             HD_MARKER ('EOF')
             WHITE_SPACE ('\\n')""");
  }

  protected final static String f100 = Strings.repeat("f", 100);

  protected void doSimpleTokenTest(@NotNull IElementType expected, @NotNull String text) {
    final Lexer lexer = createLexer();
    lexer.start(text, 0, text.length());
    final IElementType first = lexer.getTokenType();
    assertNotNull(first);
    assertEquals(0, lexer.getState());

    lexer.advance();
    assertNull("Should be only one token in: " + text + "\nSecond is " + lexer.getTokenType() + "(" + lexer.getTokenText() + ")", lexer.getTokenType());

    assertEquals(0, lexer.getState());
    assertEquals(expected, first);
  }

  // testSimpleTokens_* methods uses inputs from hcl/scanner/scanner_test.go#tokenLists

  public void testSimpleTokens_Comment() throws Exception {
    List<String> line_c_comments = Arrays.asList(
        "//",
        "////",
        "// comment",
        "// /* comment */",
        "// // comment //",
        "//" + f100
    );
    List<String> line_hash_comments = Arrays.asList(
        "#",
        "##",
        "# comment",
        "# /* comment */",
        "# # comment #",
        "#" + f100
    );
    List<String> block_comments = Arrays.asList(
        "/**/",
        "/***/",
        "/****/",
        "/**\n**/",
        "/* **/",
        "/* comment */",
        "/* // comment */",
        "/* /* comment */",
        "/*\n comment\n*/",
        "/*" + f100 + "*/"
    );
    for (String comment : line_c_comments) {
      doSimpleTokenTest(HCLElementTypes.LINE_C_COMMENT, comment);
    }
    for (String comment : line_hash_comments) {
      doSimpleTokenTest(HCLElementTypes.LINE_HASH_COMMENT, comment);
    }
    for (String comment : block_comments) {
      doSimpleTokenTest(HCLElementTypes.BLOCK_COMMENT, comment);
    }
    for (String comment : block_comments) {
      doTest("foo\n" + comment + "\nbar", "ID ('foo')\n" +
          "WHITE_SPACE ('\\n')\n" +
          "block_comment ('" + comment.replace("\n", "\\n") + "')\n" +
          "WHITE_SPACE ('\\n')\n" +
          "ID ('bar')");
    }
  }

  public void testSimpleTokens_Boolean() throws Exception {
    doSimpleTokenTest(HCLElementTypes.TRUE, "true");
    doSimpleTokenTest(HCLElementTypes.FALSE, "false");
  }

  public void testSimpleTokens_Identifier() throws Exception {
    List<String> identifiers = Arrays.asList(
        "a",
        "a0",
        "foobar",
        "foo-bar",
        "abc123",
        "LGTM",
        "_",
        "_abc123",
        "abc123_",
        "_abc_123",
        "_äöü",
        "_本",
        "äöü",
        "本",
        "a۰۱۸",
        "foo६४",
        "bar９８７６",
        "_0_"
    );
    HCLElementRenameValidator validator = new HCLElementRenameValidator();
    for (String input : identifiers) {
      doSimpleTokenTest(HCLElementTypes.ID, input);
      assertTrue(input, validator.isInputValid(input, false));
      assertTrue(input, validator.isInputValid(input, true));
    }
  }

  public void testSimpleTokens_String() throws Exception {
    List<String> strings = Arrays.asList(
        "\" \"",
        "\"a\"",
        "\"本\"",
        "\"\\a\"",
        "\"\\b\"",
        "\"\\f\"",
        "\"\\n\"",
        "\"\\r\"",
        "\"\\t\"",
        "\"\\v\"",
        "\"\\\"\"",
        "\"\\000\"",
        "\"\\777\"",
        "\"\\x00\"",
        "\"\\xff\"",
        "\"\\u0000\"",
        "\"\\ufA16\"",
        "\"\\U00000000\"",
        "\"\\U0000ffAB\"",
        "\"" + f100 + "\""
    );
    for (String input : strings) {
      doSimpleTokenTest(HCLElementTypes.DOUBLE_QUOTED_STRING, input);
    }
  }

  public void testSimpleTokens_Number() throws Exception {
    List<String> numbers = Arrays.asList(
        "0",
        "1",
        "9",
        "42",
        "1234567890",
        "00",
        "01",
        "07",
        "042",
        "01234567",
        "0x0",
        "0x1",
        "0xf",
        "0x42",
        "0x123456789abcDEF",
        "0x" + f100,
        "0X0",
        "0X1",
        "0XF",
        "0X42",
        "0X123456789abcDEF",
        "0X" + f100,
        "-0",
        "-1",
        "-9",
        "-42",
        "-1234567890",
        "-00",
        "-01",
        "-07",
        "-29",
        "-042",
        "-01234567",
        "-0x0",
        "-0x1",
        "-0xf",
        "-0x42",
        "-0x123456789abcDEF",
        "-0x" + f100,
        "-0X0",
        "-0X1",
        "-0XF",
        "-0X42",
        "-0X123456789abcDEF",
        "-0X" + f100,
        "0"
    );
    for (String input : numbers) {
      doSimpleTokenTest(HCLElementTypes.NUMBER, input);
    }
  }

  public void testSimpleTokens_Float() throws Exception {
    List<String> floats = Arrays.asList(
        "0.0",
        "1.0",
        "42.0",
        "01234567890.0",
        "0e0",
        "1e0",
        "42e0",
        "01234567890e0",
        "0E0",
        "1E0",
        "42E0",
        "01234567890E0",
        "0e+10",
        "1e-10",
        "42e+10",
        "01234567890e-10",
        "0E+10",
        "1E-10",
        "42E+10",
        "01234567890E-10",
        "01.8e0",
        "1.4e0",
        "42.2e0",
        "01234567890.12e0",
        "0.E0",
        "1.12E0",
        "42.123E0",
        "01234567890.213E0",
        "0.2e+10",
        "1.2e-10",
        "42.54e+10",
        "01234567890.98e-10",
        "0.1E+10",
        "1.1E-10",
        "42.1E+10",
        "01234567890.1E-10",
        "-0.0",
        "-1.0",
        "-42.0",
        "-01234567890.0",
        "-0e0",
        "-1e0",
        "-42e0",
        "-01234567890e0",
        "-0E0",
        "-1E0",
        "-42E0",
        "-01234567890E0",
        "-0e+10",
        "-1e-10",
        "-42e+10",
        "-01234567890e-10",
        "-0E+10",
        "-1E-10",
        "-42E+10",
        "-01234567890E-10",
        "-01.8e0",
        "-1.4e0",
        "-42.2e0",
        "-01234567890.12e0",
        "-0.E0",
        "-1.12E0",
        "-42.123E0",
        "-01234567890.213E0",
        "-0.2e+10",
        "-1.2e-10",
        "-42.54e+10",
        "-01234567890.98e-10",
        "-0.1E+10",
        "-1.1E-10",
        "-42.1E+10",
        "-01234567890.1E-10"
    );
    for (String input : floats) {
      doSimpleTokenTest(HCLElementTypes.NUMBER, input);
    }
  }

  public void testNonEscapedQuote() throws Exception {
    doTest("""
             x=[
              "a",
              "b\\\\",
             ]""", """
             ID ('x')
             = ('=')
             [ ('[')
             WHITE_SPACE ('\\n ')
             DOUBLE_QUOTED_STRING ('"a"')
             , (',')
             WHITE_SPACE ('\\n ')
             DOUBLE_QUOTED_STRING ('"b\\\\"')
             , (',')
             WHITE_SPACE ('\\n')
             ] (']')""");
  }

  // From several 'panic' issues in HCL itself
  public void testBrokenInput() throws Exception {
    doTestNoException("{\"\\0"); // #194
    doTestNoException("wÔΩø\u00dc<<070005000\n"); // #130
    doTestNoException("t\"\\400n\"{}"); // #129
    doTestNoException("{:{"); // #128
    doTestNoException("\"\\0"); // #127
  }

  private void doTestNoException(String input) {
    try {
      printTokens(input, 0, createLexer());
    } catch (Throwable t) {
      fail("Unexpected exception in lexer:" + t.getMessage());
    }
  }
}
