// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil;

import com.intellij.lang.Language;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestUtil;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.terraform.TfTestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestApplication
public class HILParserTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  private final TestFixture<ParsingTestFixture> parsingFixture =
    parsingFixture(projectFixture, TfTestUtils.getTestDataPath() + "/hil/psi", "hil", true, new HILParserDefinition());

  @Test
  public void testSimple() throws Exception {
    doCodeTest("${variable}");
  }

  @Test
  public void testSimpleNoHolder() throws Exception {
    doCodeTest("variable");
  }

  @Test
  public void testMethodCall() throws Exception {
    doCodeTest("method(a,v)");
  }

  @Test
  public void testMethodCallTrailingComma() throws Exception {
    doCodeTest("method(a,b,)");
  }

  @Test
  public void testMethodCallTrailingEllipsis() throws Exception {
    doCodeTest("method(a,b...)");
  }

  @Test
  public void testLongMethodCall() throws Exception {
    doCodeTest("foo.bar.baz.method(a,v)");
  }

  @Test
  public void testOperator() throws Exception {
    doCodeTest("${count.index+1}");
  }

  @Test
  public void testNoOperator() throws Exception {
    doCodeTest("${var.amis.us-east-1}");
  }

  @Test
  public void testStarVariable() throws Exception {
    doCodeTest("${aws_instance.web.*.id}");
  }

  @Test
  public void testStarQuotedVariable() throws Exception {
    doCodeTest("${aws_instance.web.\"*\".id}");
  }

  @Test
  public void testSelectFromNumber() throws Exception {
    doCodeTest("${aws_instance.web.10.id}");
  }

  @Test
  public void testInception() throws Exception {
    doCodeTest("${aws_instance.web.${count.index}.id}");
  }

  @Test
  public void testString() throws Exception {
    doCodeTest("${file(\"ecs-container-definitions.json\")}");
  }

  @Test
  public void testUnaryNumbers() throws Exception {
    doCodeTest("${format(\"\", 0, 1, -1, +1, -0, +0, -10.0e5, +10.5e-2)}");
  }

  @Test
  public void testUnaryMathExpressions() throws Exception {
    doCodeTest("${format(\"\", +10 - 9, -10 + -9, -10 + (-9), -1 * -1)}");
  }

  @Test
  public void testSimpleMath() throws Exception {
    doCodeTest("${format(\"\", 2 + 2, 2 - 2, 2 * 2, 2 / 2, 2 % 2)}");
  }

  @Test
  public void testSimpleMathCompact() throws Exception {
    doCodeTest("${format(\"\", 2+2, 2-2, 2*2, 2/2, 2%2)}");
  }

  @Test
  public void testOrderOfMathOperations() throws Exception {
    doCodeTest("${format(\"\", 2 + 2 * 2, 2 * (2 + 2), 1 - 2 + 3)}");
  }

  @Test
  public void testSimpleIndexes() throws Exception {
    doCodeTest("${format(foo[1], baz[0])}");
  }

  @Test
  public void testGreedyIndexes() throws Exception {
    doCodeTest("${aws_instance.web.*.list[2]}");
  }

  @Test
  public void testInceptionIndexes() throws Exception {
    doCodeTest("${foo[bar[0]]}");
  }

  @Test
  public void testMultipleIndexes() throws Exception {
    doCodeTest("${foo[0][1][2][3]}");
  }

  @Test
  public void testTooManyIndexes() throws Exception {
    doCodeTest("${foo[a[0]][b[1]][c[2]][d[3]]}");
  }

  @Test
  public void testSlashesEscaping() throws Exception {
    doCodeTest("${join(\"\\\",\\\"\", values(var.developers))}");
  }

  @Test
  public void testTernaryOp() throws Exception {
    doCodeTest("${true ? 1 : 2}");
  }

  @Test
  public void testTernaryComplexOp() throws Exception {
    doCodeTest("${a < 5 ? a + 5 : !false && true}");
  }

  @Test
  public void testTernaryNested() throws Exception {
    doCodeTest("${a == \"1\" ? b == \"2\" ? \"x\" : \"y\" : \"z\"}");
  }

  @Test
  public void testTernaryNestedWithStrings() throws Exception {
    doCodeTest("${a == \"1\" ? \"${b == \"2\" ? \"x\" : \"y\"}\" : \"z\"}");
  }

  @Test
  public void testInterpolationStringInception() throws Exception {
    doCodeTest("\"${\"${\"${1+1}\"}\"}\"");
  }

  @Test
  public void testInterpolationStringInceptionEscaping() throws Exception {
    doCodeTest("\"${\"${\"${\"\\\",\\\"\"}\"}\"}\"");
  }

  @Test
  public void testLogicalOps() throws Exception {
    doCodeTest("${true || !false && true}");
  }

  @Test
  public void testLogicalOps2() throws Exception {
    doCodeTest("${!!(true || !false) && true}");
  }

  @Test
  public void testCompareOps() throws Exception {
    doCodeTest("${format(\"\", 1 < 2, 1 > 2, 1 <= 2, 1 >= 2, 1 == 2, 1 != 2)}");
  }

  @Test
  public void testOrderOfBinaryOperations() throws Exception {
    doCodeTest("${format(\"\", a<5||b>2, a<5&&b>2, a<5 != b>2, a+1 != b+2)}");
  }

  @Test
  public void testUnfinishedConditional1() throws Exception {
    doCodeTest("${true?}");
  }

  @Test
  public void testUnfinishedConditional2() throws Exception {
    doCodeTest("${true?:}");
  }

  @Test
  public void testUnfinishedConditional3() throws Exception {
    doCodeTest("${true?true}");
  }

  @Test
  public void testUnfinishedConditional4() throws Exception {
    doCodeTest("${true?true:}");
  }

  @Test
  public void testUnfinishedConditional5() throws Exception {
    doCodeTest("${true?:true}");
  }

  @Test
  public void testUnfinishedConditional6() throws Exception {
    doCodeTest("?:", """
      HILFile: a.hil
        PsiErrorElement:${ or TEMPLATE_START expected, got '?'
          PsiElement(?)('?')
        PsiElement(:)(':')""");
  }

  @Test
  public void testClosingCurlyBraceInString() throws Exception {
    doCodeTest("${x(\"}\")}");
  }

  @Test
  public void testEscapedQuotes() throws Exception {
    doCodeTest("${\"\\\"x\\\"\"}");
  }

  @Test
  public void testUnaryOverSelect() throws Exception {
    doCodeTest("!var.private");
  }

  @Test
  public void testUnaryOverIndexSelect() throws Exception {
    doCodeTest("!var.list[0]");
  }

  @Test
  public void testConditionalOverUnary() {
    doCodeTest("!false?!false:!false");
  }

  @Test
  public void testIdStartsWithNumber() {
    doCodeTest("${null_resource.2a.id}");
  }

  @Test
  public void testIdWithHyphen() {
    doCodeTest("${null_resource.1-1.id}");
  }

  @Test
  public void testMixUnaryBinary() {
    doCodeTest("(-a+b) * (-1+1)");
  }

  @Test
  public void testArray() {
    doCodeTest("${[true,false]}");
  }

  @Test
  public void testObject() {
    doCodeTest("${{a=true,b=[0,false,]}}");
  }

  @Test
  public void testObjectAsMethodCallParameter() {
    doCodeTest("${foo({a=true\nb=false})}");
  }

  @Test
  public void testTemplateFor() {
    doCodeTest("%{for a, b in var.test~} 123 %{endfor}");
  }

  @Test
  public void testNestedTemplateFor() {
    doCodeTest("%{for a in var.test~} %{for b in var.test~} 123 %{endfor} %{endfor}");
  }

  @Test
  public void testTemplateIf() {
    doCodeTest("%{~ if test() > -1 ~} 123 %{endif}");
  }

  @Test
  public void testSimpleDefinedMethodExpression() throws Exception {
    doCodeTest("provider::aws::createInstance()");
  }

  @Test
  public void testDefinedMethodExpressionWithParameters() throws Exception {
    doCodeTest("provider::google::startServer(\"us-central1\", 2)");
  }

  @Test
  public void testComplexDefinedMethodExpression() throws Exception {
    doCodeTest("provider::custom::nestedMethodCall(param1, param2).innerCall()");
  }

  @Test
  public void testDefinedMethodExpressionWithQualifier() throws Exception {
    doCodeTest("provider::aws::resource(\"web_server\").deploy()");
  }

  @Test
  public void testInvalidDefinedMethodExpression_MissingParts() throws Exception {
    doCodeTest("provider::aws::");
  }

  @Test
  public void testInvalidDefinedMethodExpression_InvalidSyntax() throws Exception {
    doCodeTest("provider::identifier createInstance()");
  }


  protected void doCodeTest(@NotNull String code) {
    if (!code.startsWith("${") && !code.endsWith("}")) {
      code = "${" + code + "}";
    }
    parsingFixture.get().doCodeTest(code);
  }

  protected void doCodeTest(@NotNull final String code, @NotNull final String expected) {
    ParsingTestFixture fixture = parsingFixture.get();
    PsiFile file = fixture.createPsiFile("a", code);
    ReadAction.runBlocking(() -> {
      ParsingTestUtil.ensureParsed(file);
      assertEquals(code, file.getText());
      boolean skipSpaces = fixture.getSkipSpaces();
      boolean printRanges = fixture.getIncludeRanges();
      FileViewProvider provider = file.getViewProvider();
      Set<Language> languages = provider.getLanguages();

      if (!fixture.getCheckAllPsiRoots() || languages.size() == 1) {
        String actual = ParsingTestFixture.toParseTreeText(file, skipSpaces, printRanges).trim();
        UsefulTestCase.assertSameLines(expected, actual);
      } else {
        fail("Use test files since there're many languages in parsed file: " + languages);
      }
    });
  }
}
