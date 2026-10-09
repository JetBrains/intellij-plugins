// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.lexer.Lexer;
import org.intellij.terraform.hcl.HCLElementTypes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TfConfigLexerTest extends HCLLexerTest {
  @Override
  protected @NotNull Lexer createLexer() {
    return TfParserDefinition.createLexer();
  }

  @Test
  public void testTerraformILWithSpecials() {
    doTest("a = \"${$()}\"", """
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${$()}"')""");
  }

  @Test
  public void testTerraformILWithSpecials2() {
    doTest("a = \"${{$}$}}\"", """
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${{$}$}}"')""");
  }

  @Test
  public void testTerraformILWithIncorrectString() {
    doTest("count = \"${call(incomplete\")}\"", """
      ID ('count')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${call(incomplete")}"')""");
  }

  @Test
  public void testTerraformILWithStringWithClosingBrace() {
    doTest("a = \"${foo(\"}\")}\"", """
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${foo("}")}"')""");
  }

  @Test
  public void testTerraformILWithString_Unfinished() {
    doTest("a = '${\"uf)}'", """
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      SINGLE_QUOTED_STRING (''${"uf)}'')""");
  }

  @Test
  public void testTerraformILWithString_Unfinished2() {
    doTest("a = \"${\"uf)}\"", """
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${"uf)}"')""");
  }

  @Test
  public void testTerraformILWithString_Unfinished3() {
    doTest("c{a = \"${f(\"b.json\")}\"'}", """
      ID ('c')
      { ('{')
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${f("b.json")}"')
      SINGLE_QUOTED_STRING (''}')""");
  }

  @Test
  @Override
  public void testUnfinishedInterpolation2() {
    doTest("a = \"${b(\"c\")}${\"\nx=y", """
      ID ('a')
      WHITE_SPACE (' ')
      = ('=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('"${b("c")}${"\\nx=y')""");
  }

  @Test
  public void testSimpleTokens_String_With_Interpolation() {
    List<String> strings = Arrays.asList(
        "\"${file(\"foo\")}\"",
        "\"${~file(\\\"foo\\\")}\"",
        "\"${file(\\\"" + f100 + "\\\")~}\"",
        "\"${join(\"\\\\\",\\\\\"\", values(var.developers))}\""
    );
    for (String input : strings) {
      doSimpleTokenTest(HCLElementTypes.DOUBLE_QUOTED_STRING, input);
    }
  }

  @Test
  public void testMultilineString_WithInterpolation() {
    doTest("mli=\"${hello\n  world}\"", """
      ID ('mli')
      = ('=')
      DOUBLE_QUOTED_STRING ('"${hello\\n  world}"')
      """);
  }

  @Test
  public void testNonEscapedQuoteInInterpolation() {
    doTest("""
             x=[
              "${"a"}",
              "${"b\\\\"}\\\\",
             ]""", """
             ID ('x')
             = ('=')
             [ ('[')
             WHITE_SPACE ('\\n ')
             DOUBLE_QUOTED_STRING ('"${"a"}"')
             , (',')
             WHITE_SPACE ('\\n ')
             DOUBLE_QUOTED_STRING ('"${"b\\\\"}\\\\"')
             , (',')
             WHITE_SPACE ('\\n')
             ] (']')""");
  }

  @Test
  public void testEscapedQuoteInInterpolation() {
    doTest("\"${\"\\\"x\\\"\"}\"\n",
           """
             DOUBLE_QUOTED_STRING ('"${"\\"x\\""}"')
             WHITE_SPACE ('\\n')
             """);
  }

  @Test
  public void testForArray() {
    doTest("a = [for k, v in foo: v if true]",
           """
             ID ('a')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             [ ('[')
             ID ('for')
             WHITE_SPACE (' ')
             ID ('k')
             , (',')
             WHITE_SPACE (' ')
             ID ('v')
             WHITE_SPACE (' ')
             ID ('in')
             WHITE_SPACE (' ')
             ID ('foo')
             : (':')
             WHITE_SPACE (' ')
             ID ('v')
             WHITE_SPACE (' ')
             ID ('if')
             WHITE_SPACE (' ')
             true ('true')
             ] (']')""");
  }

  @Test
  public void testSelectExpression() {
    doTest("a = foo.bar.baz",
           """
             ID ('a')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             ID ('foo')
             . ('.')
             ID ('bar')
             . ('.')
             ID ('baz')""");
  }

  @Test
  public void testIndexSelectExpression() {
    doTest("a = foo[5].baz",
           """
             ID ('a')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             ID ('foo')
             [ ('[')
             NUMBER ('5')
             ] (']')
             . ('.')
             ID ('baz')""");
  }


  @Test
  public void testSplatExpression() {
    doTest("a = foo.*.baz",
           """
             ID ('a')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             ID ('foo')
             . ('.')
             * ('*')
             . ('.')
             ID ('baz')""");
  }

  @Test
  public void testFullSplatExpression() {
    doTest("a = foo[*].baz",
           """
             ID ('a')
             WHITE_SPACE (' ')
             = ('=')
             WHITE_SPACE (' ')
             ID ('foo')
             [ ('[')
             * ('*')
             ] (']')
             . ('.')
             ID ('baz')""");
  }

  @Test
  public void testTemplateInjection() {
    doTest("\"%{ for v in [\"true\"] }${v}%{ endfor }\"",
        "DOUBLE_QUOTED_STRING ('\"%{ for v in [\"true\"] }${v}%{ endfor }\"')");
    doTest("\"%{~ for v in [\"true\"] ~}\"",
        "DOUBLE_QUOTED_STRING ('\"%{~ for v in [\"true\"] ~}\"')");
  }
}
