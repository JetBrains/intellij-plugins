// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil;

import com.intellij.lexer.Lexer;
import org.intellij.terraform.BaseLexerTestCase;
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

  public void testLogicalOps() {
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

  public void testCompareOps() {
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

  public void testTernaryOp() {
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

  public void testTernaryOpWithInterpolationBranch() {
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

  public void testMultiline() {
    doTest("1 +\n2", """
      NUMBER ('1')
      WHITE_SPACE (' ')
      + ('+')
      WHITE_SPACE ('\\n')
      NUMBER ('2')""");
  }

  public void testMultilineStringInception() {
    doTest("\"${\n\"x\"\n}\"", "DOUBLE_QUOTED_STRING ('\"${\\n\"x\"\\n}\"')");
  }

  public void testMultilineStringInception2() {
    doTest("\"${\n\"x\n y\"}\"", "DOUBLE_QUOTED_STRING ('\"${\\n\"x\\n y\"}\"')");
  }

  public void testMultilineStringInception3() {
    doTest("\"${\"${1\n+1}\n \"}", "DOUBLE_QUOTED_STRING ('\"${\"${1\\n+1}\\n \"}')");
  }

  public void testEscapesInString() {
    doTest("\"\\\\\"", "DOUBLE_QUOTED_STRING ('\"\\\\\"')");
  }

  public void testEscapesInString2() {
    doTest("join(\"\\\",\\\"\", x)", """
      ID ('join')
      ( ('(')
      DOUBLE_QUOTED_STRING ('"\\",\\""')
      , (',')
      WHITE_SPACE (' ')
      ID ('x')
      ) (')')""");
  }

  public void testUnsupportedOps() {
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

  public void testClosingCurlyBraceInString() {
    doTest("${x(\"\\\"}\\\\\")}", """
      ${ ('${')
      ID ('x')
      ( ('(')
      DOUBLE_QUOTED_STRING ('"\\"}\\\\"')
      ) (')')
      } ('}')""");
  }

  public void testIdStartsWithNumber() {
    doTest("${null_resource.2a.id}", """
      ${ ('${')
      ID ('null_resource')
      . ('.')
      ID ('2a')
      . ('.')
      ID ('id')
      } ('}')""");
  }

  public void testIdIsHexNumber() {
    doTest("${null_resource.0x0.id}", """
      ${ ('${')
      ID ('null_resource')
      . ('.')
      NUMBER ('0x0')
      . ('.')
      ID ('id')
      } ('}')""");
  }

  public void testNumbers() {
    doTest("0", "NUMBER ('0')");
    doTest("0x0", "NUMBER ('0x0')");
    doTest("0x0.0", "NUMBER ('0x0.0')");
    doTest("0x0.0e0", "NUMBER ('0x0.0e0')");
    doTest("0x0.0e-0", "NUMBER ('0x0.0e-0')");
    doTest("0x0.0e+0", "NUMBER ('0x0.0e+0')");
  }

  public void testNumberOps() {
    doTest("1+1", "NUMBER ('1')\n+ ('+')\nNUMBER ('1')\n");
    doTest("1-1", "NUMBER ('1')\n- ('-')\nNUMBER ('1')\n");
    doTest("1*1", "NUMBER ('1')\n* ('*')\nNUMBER ('1')\n");
    doTest("1/1", "NUMBER ('1')\n/ ('/')\nNUMBER ('1')\n");
  }

  public void testTemplateFor() {
    doTest("%{for a, b in var.test~} 123 %{endfor} ", """
      TEMPLATE_START ('%{')
      for ('for')
      WHITE_SPACE (' ')
      ID ('a')
      , (',')
      WHITE_SPACE (' ')
      ID ('b')
      WHITE_SPACE (' ')
      in ('in')
      WHITE_SPACE (' ')
      ID ('var')
      . ('.')
      ID ('test')
      } ('~}')
      WHITE_SPACE (' ')
      NUMBER ('123')
      WHITE_SPACE (' ')
      TEMPLATE_START ('%{')
      endfor ('endfor')
      } ('}')
      WHITE_SPACE (' ')""".trim());
  }

  public void testArray() {
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

  public void testObject() {
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

  public void testProviderFunctionCall() {
    doTest("provider::aws::createInstance()", """
      ID ('provider')
      :: ('::')
      ID ('aws')
      :: ('::')
      ID ('createInstance')
      ( ('(')
      ) (')')""".trim());
  }

  public void testNestedProviderFunctionCall() {
    doTest("provider::custom::outer(provider::inner::compute(42))", """
      ID ('provider')
      :: ('::')
      ID ('custom')
      :: ('::')
      ID ('outer')
      ( ('(')
      ID ('provider')
      :: ('::')
      ID ('inner')
      :: ('::')
      ID ('compute')
      ( ('(')
      NUMBER ('42')
      ) (')')
      ) (')')""".trim());
  }

  public void testInvalidProviderFunctionCall_MissingParentheses() {
    doTest("provider::aws::createInstance", """
      ID ('provider')
      :: ('::')
      ID ('aws')
      :: ('::')
      ID ('createInstance')
      """.trim());
  }
}
