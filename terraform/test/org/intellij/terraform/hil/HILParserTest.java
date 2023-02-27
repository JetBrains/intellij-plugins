/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hil;

import com.intellij.lang.Language;
import com.intellij.psi.FileViewProvider;
import com.intellij.testFramework.ParsingTestCase;
import org.intellij.terraform.TerraformTestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

public class HILParserTest extends ParsingTestCase {
  public HILParserTest() {
    super("hil/psi", "hil", true, new HILParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return TerraformTestUtils.getTestDataPath();
  }

  private void doTest() {
    doTest(true);
  }

  public void testSimple() throws Exception {
    doCodeTest("${variable}");
  }

  public void testSimpleNoHolder() throws Exception {
    doCodeTest("variable");
  }

  public void testMethodCall() throws Exception {
    doCodeTest("method(a,v)");
  }

  public void testMethodCallTrailingComma() throws Exception {
    doCodeTest("method(a,b,)");
  }

  public void testMethodCallTrailingEllipsis() throws Exception {
    doCodeTest("method(a,b...)");
  }

  public void testLongMethodCall() throws Exception {
    doCodeTest("foo.bar.baz.method(a,v)");
  }

  public void testOperator() throws Exception {
    doCodeTest("${count.index+1}");
  }

  public void testNoOperator() throws Exception {
    doCodeTest("${var.amis.us-east-1}");
  }

  public void testStarVariable() throws Exception {
    doCodeTest("${aws_instance.web.*.id}");
  }

  public void testStarQuotedVariable() throws Exception {
    doCodeTest("${aws_instance.web.\"*\".id}");
  }

  public void testSelectFromNumber() throws Exception {
    doCodeTest("${aws_instance.web.10.id}");
  }

  public void testInception() throws Exception {
    doCodeTest("${aws_instance.web.${count.index}.id}");
  }

  public void testString() throws Exception {
    doCodeTest("${file(\"ecs-container-definitions.json\")}");
  }

  public void testUnaryNumbers() throws Exception {
    doCodeTest("${format(\"\", 0, 1, -1, +1, -0, +0, -10.0e5, +10.5e-2)}");
  }

  public void testUnaryMathExpressions() throws Exception {
    doCodeTest("${format(\"\", +10 - 9, -10 + -9, -10 + (-9), -1 * -1)}");
  }

  public void testSimpleMath() throws Exception {
    doCodeTest("${format(\"\", 2 + 2, 2 - 2, 2 * 2, 2 / 2, 2 % 2)}");
  }

  public void testSimpleMathCompact() throws Exception {
    doCodeTest("${format(\"\", 2+2, 2-2, 2*2, 2/2, 2%2)}");
  }

  public void testOrderOfMathOperations() throws Exception {
    doCodeTest("${format(\"\", 2 + 2 * 2, 2 * (2 + 2), 1 - 2 + 3)}");
  }

  public void testSimpleIndexes() throws Exception {
    doCodeTest("${format(foo[1], baz[0])}");
  }

  public void testGreedyIndexes() throws Exception {
    doCodeTest("${aws_instance.web.*.list[2]}");
  }

  public void testInceptionIndexes() throws Exception {
    doCodeTest("${foo[bar[0]]}");
  }

  public void testMultipleIndexes() throws Exception {
    doCodeTest("${foo[0][1][2][3]}");
  }

  public void testTooManyIndexes() throws Exception {
    doCodeTest("${foo[a[0]][b[1]][c[2]][d[3]]}");
  }

  public void testSlashesEscaping() throws Exception {
    doCodeTest("${join(\"\\\",\\\"\", values(var.developers))}");
  }

  public void testTernaryOp() throws Exception {
    doCodeTest("${true ? 1 : 2}");
  }

  public void testTernaryComplexOp() throws Exception {
    doCodeTest("${a < 5 ? a + 5 : !false && true}");
  }

  public void testTernaryNested() throws Exception {
    doCodeTest("${a == \"1\" ? b == \"2\" ? \"x\" : \"y\" : \"z\"}");
  }

  public void testTernaryNestedWithStrings() throws Exception {
    doCodeTest("${a == \"1\" ? \"${b == \"2\" ? \"x\" : \"y\"}\" : \"z\"}");
  }

  public void testInterpolationStringInception() throws Exception {
    doCodeTest("\"${\"${\"${1+1}\"}\"}\"");
  }

  public void testInterpolationStringInceptionEscaping() throws Exception {
    doCodeTest("\"${\"${\"${\"\\\",\\\"\"}\"}\"}\"");
  }

  public void testLogicalOps() throws Exception {
    doCodeTest("${true || !false && true}");
  }

  public void testLogicalOps2() throws Exception {
    doCodeTest("${!!(true || !false) && true}");
  }

  public void testCompareOps() throws Exception {
    doCodeTest("${format(\"\", 1 < 2, 1 > 2, 1 <= 2, 1 >= 2, 1 == 2, 1 != 2)}");
  }

  public void testOrderOfBinaryOperations() throws Exception {
    doCodeTest("${format(\"\", a<5||b>2, a<5&&b>2, a<5 != b>2, a+1 != b+2)}");
  }

  public void testUnfinishedConditional1() throws Exception {
    doCodeTest("${true?}");
  }

  public void testUnfinishedConditional2() throws Exception {
    doCodeTest("${true?:}");
  }

  public void testUnfinishedConditional3() throws Exception {
    doCodeTest("${true?true}");
  }

  public void testUnfinishedConditional4() throws Exception {
    doCodeTest("${true?true:}");
  }

  public void testUnfinishedConditional5() throws Exception {
    doCodeTest("${true?:true}");
  }

  public void testUnfinishedConditional6() throws Exception {
    doCodeTest("?:", """
      HILFile: a.hil
        PsiErrorElement:'?' unexpected
          PsiElement(?)('?')
        PsiElement(:)(':')""");
  }

  public void testClosingCurlyBraceInString() throws Exception {
    doCodeTest("${x(\"}\")}");
  }

  public void testEscapedQuotes() throws Exception {
    doCodeTest("${\"\\\"x\\\"\"}");
  }

  public void testUnaryOverSelect() throws Exception {
    doCodeTest("!var.private");
  }

  public void testUnaryOverIndexSelect() throws Exception {
    doCodeTest("!var.list[0]");
  }

  public void testConditionalOverUnary() throws IOException {
    doCodeTest("!false?!false:!false");
  }

  public void testIdStartsWithNumber() throws IOException {
    doCodeTest("${null_resource.2a.id}");
  }

  public void testIdWithHyphen() throws IOException {
    doCodeTest("${null_resource.1-1.id}");
  }

  public void testMixUnaryBinary() throws IOException {
    doCodeTest("(-a+b) * (-1+1)");
  }

  public void testArray() throws IOException {
    doCodeTest("${[true,false]}");
  }

  public void testObject() throws IOException {
    doCodeTest("${{a=true,b=[0,false,]}}");
  }

  public void testObjectAsMethodCallParameter() throws IOException {
    doCodeTest("${foo({a=true\nb=false})}");
  }

  public void testTemplateFor() throws IOException {
    doCodeTest("%{for a, b in var.test~}");
  }

  public void testTemplateIf() throws IOException {
    doCodeTest("%{~ if test() > -1 ~}");
  }

  public void testTemplateEnds() throws IOException {
    doCodeTest("%{endfor}", """
      HILFile: a.hil
        ILTemplateHolder
          PsiElement(%{)('%{')
          ILTemplateEndForStatement
            PsiElement(ID)('endfor')
          PsiElement(})('}')""");
    doCodeTest("%{else}", """
      HILFile: a.hil
        ILTemplateHolder
          PsiElement(%{)('%{')
          ILTemplateElseStatement
            PsiElement(ID)('else')
          PsiElement(})('}')""");
    doCodeTest("%{endif}", """
      HILFile: a.hil
        ILTemplateHolder
          PsiElement(%{)('%{')
          ILTemplateEndIfStatement
            PsiElement(ID)('endif')
          PsiElement(})('}')""");
  }

  @Override
  protected void doCodeTest(@NotNull String code) throws IOException {
    if (!code.startsWith("${") && !code.endsWith("}")) {
      code = "${" + code + "}";
    }
    super.doCodeTest(code);
  }

  protected void doCodeTest(@NotNull final String code, @NotNull final String expected) throws IOException {
    myFile = createPsiFile("a", code);
    ensureParsed(myFile);
    assertEquals(code, myFile.getText());
    boolean skipSpaces = this.skipSpaces();
    boolean printRanges = this.includeRanges();
    FileViewProvider provider = myFile.getViewProvider();
    Set<Language> languages = provider.getLanguages();

    if (!this.checkAllPsiRoots() || languages.size() == 1) {
      String actual = toParseTreeText(myFile, skipSpaces, printRanges).trim();
      assertSameLines(expected, actual);
    } else {
      fail("Use test files since there're many languages in parsed file: " + languages);
    }
  }
}
