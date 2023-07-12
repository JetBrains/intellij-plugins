// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil;

import com.intellij.lexer.Lexer;
import org.intellij.terraform.BaseLexerTestCase;
import org.intellij.terraform.hil.psi.HILLexer;
import org.jetbrains.annotations.NotNull;

public class HILLexerTest extends BaseLexerTestCase {
  @Override
  protected @NotNull Lexer createLexer() {
    return new HILLexer();
  }

  @Override
  protected @NotNull String getDirPath() {
    return "data/hil/lexer";
  }

  public void testLogicalOps() throws Exception {
    doTest("true && true", """
      true ('true')
      WHITE_SPACE (' ')
      && ('&&')
      WHITE_SPACE (' ')
      true ('true')""");
    doTest("true || false", """
      true ('true')
      WHITE_SPACE (' ')
      || ('||')
      WHITE_SPACE (' ')
      false ('false')""");
    doTest("!true", "! ('!')\n" +
        "true ('true')");
  }

  public void testCompareOps() throws Exception {
    doTest("1==2", """
      NUMBER ('1')
      == ('==')
      NUMBER ('2')""");
    doTest("1!=2", """
      NUMBER ('1')
      != ('!=')
      NUMBER ('2')""");
    doTest("1>2", """
      NUMBER ('1')
      > ('>')
      NUMBER ('2')""");
    doTest("1<2", """
      NUMBER ('1')
      < ('<')
      NUMBER ('2')""");
    doTest("1>=2", """
      NUMBER ('1')
      >= ('>=')
      NUMBER ('2')""");
    doTest("1<=2", """
      NUMBER ('1')
      <= ('<=')
      NUMBER ('2')""");
  }

  public void testTernaryOp() throws Exception {
    doTest("true ? 1 : 2", """
      true ('true')
      WHITE_SPACE (' ')
      ? ('?')
      WHITE_SPACE (' ')
      NUMBER ('1')
      WHITE_SPACE (' ')
      : (':')
      WHITE_SPACE (' ')
      NUMBER ('2')""");
  }

  public void testTernaryOpWithInterpolationBranch() throws Exception {
    doTest("true ? 1 : \"${\"x\"}\"", """
      true ('true')
      WHITE_SPACE (' ')
      ? ('?')
      WHITE_SPACE (' ')
      NUMBER ('1')
      WHITE_SPACE (' ')
      : (':')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${"x"}"')""");
  }

  public void testMultiline() throws Exception {
    doTest("1 +\n2", """
      NUMBER ('1')
      WHITE_SPACE (' ')
      + ('+')
      WHITE_SPACE ('\\n')
      NUMBER ('2')""");
  }

  public void testMultilineStringInception() throws Exception {
    doTest("\"${\n\"x\"\n}\"", "DOUBLE_QUOTED_STRING ('\"${\\n\"x\"\\n}\"')");
  }

  public void testMultilineStringInception2() throws Exception {
    doTest("\"${\n\"x\n y\"}\"", "DOUBLE_QUOTED_STRING ('\"${\\n\"x\\n y\"}\"')");
  }

  public void testMultilineStringInception3() throws Exception {
    doTest("\"${\"${1\n+1}\n \"}", "DOUBLE_QUOTED_STRING ('\"${\"${1\\n+1}\\n \"}')");
  }

  public void testEscapesInString() throws Exception {
    doTest("\"\\\\\"", "DOUBLE_QUOTED_STRING ('\"\\\\\"')");
  }

  public void testEscapesInString2() throws Exception {
    doTest("join(\"\\\",\\\"\", x)", """
      ID ('join')
      ( ('(')
      DOUBLE_QUOTED_STRING ('"\\",\\""')
      , (',')
      WHITE_SPACE (' ')
      ID ('x')
      ) (')')""");
  }

  public void testUnsupportedOps() throws Exception {
    doTest("1|2", """
      NUMBER ('1')
      BAD_CHARACTER ('|')
      NUMBER ('2')""");
    doTest("1&2", """
      NUMBER ('1')
      BAD_CHARACTER ('&')
      NUMBER ('2')""");
    doTest("1=2", """
      NUMBER ('1')
      = ('=')
      NUMBER ('2')""");
  }

  public void testClosingCurlyBraceInString() throws Exception {
    doTest("${x(\"\\\"}\\\\\")}", """
      ${ ('${')
      ID ('x')
      ( ('(')
      DOUBLE_QUOTED_STRING ('"\\"}\\\\"')
      ) (')')
      } ('}')""");
  }

  public void testIdStartsWithNumber() throws Exception {
    doTest("${null_resource.2a.id}", """
      ${ ('${')
      ID ('null_resource')
      . ('.')
      ID ('2a')
      . ('.')
      ID ('id')
      } ('}')""");
  }

  public void testIdIsHexNumber() throws Exception {
    doTest("${null_resource.0x0.id}", """
      ${ ('${')
      ID ('null_resource')
      . ('.')
      NUMBER ('0x0')
      . ('.')
      ID ('id')
      } ('}')""");
  }

  public void testNumbers() throws Exception {
    doTest("0", "NUMBER ('0')");
    doTest("0x0", "NUMBER ('0x0')");
    doTest("0x0.0", "NUMBER ('0x0.0')");
    doTest("0x0.0e0", "NUMBER ('0x0.0e0')");
    doTest("0x0.0e-0", "NUMBER ('0x0.0e-0')");
    doTest("0x0.0e+0", "NUMBER ('0x0.0e+0')");
  }

  public void testNumberOps() throws Exception {
    doTest("1+1", "NUMBER ('1')\n+ ('+')\nNUMBER ('1')\n");
    doTest("1-1", "NUMBER ('1')\n- ('-')\nNUMBER ('1')\n");
    doTest("1*1", "NUMBER ('1')\n* ('*')\nNUMBER ('1')\n");
    doTest("1/1", "NUMBER ('1')\n/ ('/')\nNUMBER ('1')\n");
  }

  public void testTemplateFor() throws Exception {
    doTest("%{for a, b in var.test~}", """
      %{ ('%{')
      ID ('for')
      WHITE_SPACE (' ')
      ID ('a')
      , (',')
      WHITE_SPACE (' ')
      ID ('b')
      WHITE_SPACE (' ')
      ID ('in')
      WHITE_SPACE (' ')
      ID ('var')
      . ('.')
      ID ('test')
      } ('~}')""");
  }

  public void testArray() throws Exception {
    doTest("${[true,false,]}", """
      ${ ('${')
      [ ('[')
      true ('true')
      , (',')
      false ('false')
      , (',')
      ] (']')
      } ('}')""");
  }

  public void testObject() throws Exception {
    doTest("${{a=1,b=2\nc=3}}}", """
      ${ ('${')
      { ('{')
      ID ('a')
      = ('=')
      NUMBER ('1')
      , (',')
      ID ('b')
      = ('=')
      NUMBER ('2')
      WHITE_SPACE ('\\n')
      ID ('c')
      = ('=')
      NUMBER ('3')
      } ('}')
      } ('}')
      } ('}')""");
  }
}
