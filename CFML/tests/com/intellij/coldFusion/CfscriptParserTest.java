/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion;

import com.intellij.coldFusion.model.CfmlLanguage;

import java.io.IOException;

/**
 * Created by Lera Nikolaenko
 */
public class CfscriptParserTest extends CfmlCodeInsightFixtureTestCase {
  public void testSimpleStatement() throws Throwable {
    doTest();
  }

  public void testTwoSimpleStatements() throws Throwable {
    doTest();
  }

  public void testIfExpression() throws Throwable {
    doTest();
  }

  public void testIfElseExpression() throws Throwable {
    doTest();
  }

  public void testIfElseNestedExpression() throws Throwable {
    doTest();
  }

  public void testWhileExpression() throws Throwable {
    doTest();
  }

  public void testSwitchExpression() throws Throwable {
    doTest();
  }

  public void testActionExpression() throws Throwable {
    doTest();
  }

  public void testActionSaveContentExpression() throws Throwable {
    doTest();
  }

  public void testDefaultAttribute() throws Throwable {
    doTest();
  }

  public void testAttributesInFunctionDefinition() throws Throwable {
    doTest();
  }

  public void testAttributesInProperty() throws Throwable {
    doTest();
  }

  public void testNewExpressionInReturn() throws Throwable {
    doTest();
  }

  public void testStructArrayAsReferenceExpression() throws Throwable {
    doTest();
  }

  public void testEmptyOneLineComment() throws Throwable {
    doTest();
  }

  public void testOneLineComment() throws Throwable {
    doTest();
  }

  public void testMultiLineComment() throws Throwable {
    doTest();
  }

  public void testInvalidDivisionOperator() throws Throwable {
    doTest();
  }

  public void testCfml_24_try_bug() throws Throwable {
    doTest();
  }

  public void testDotBracesConstruction() throws Throwable {
    doTest();
  }

  public void testCfsetTag() throws Throwable {
    doTest();
  }

  public void testBraceStructure() throws Throwable {
    doTest();
  }

  public void testOldFunctionSyntax() throws Throwable {
    doTest();
  }

  public void testNewComponentSyntax() throws Throwable {
    doTest("/newSyntax");
  }

  public void testNewComponentSyntax11() throws Throwable {
    doTest("/newSyntax");
  }

  public void testNewInitAsReturnStatement() throws Throwable {
    doTest();
  }

  public void testDefaultFunctionNameInCf8() throws Throwable {
    Util.runTestWithLanguageLevel(() -> {
      doTest();
      return null;
    }, CfmlLanguage.CF8, getProject());
  }

  public void testDefaultFunctionName() throws Throwable {
    doTest();
  }

  public void testNewFunctionSyntax1() throws Throwable {
    doTest("/newSyntax");
  }

  public void testNewFunctionSyntax2() throws Throwable {
    doTest("/newSyntax");
  }

  public void testNewFunctionSyntax3() throws Throwable {
    doTest("/newSyntax");
  }

  public void testNewFunctionSyntax4() throws Throwable {
    doTest("/newSyntax");
  }

  public void testComponentAsReturnType() throws Throwable {
    doTest();
  }

  public void testParameterAttributes() throws Throwable {
    doTest("/newSyntax");
  }

  public void testDefineComponentWithoutTag() throws Throwable {
    doTest("/newSyntax");
  }

  public void testAllowImportsBeforeComponentDecl() throws Throwable {
    doTest("/newSyntax");
  }

  public void testConsequentCaseLabels() throws Throwable {
    doTest("");
  }

  public void testMultipleStatementsInCase() throws Throwable {
    doTest("");
  }

  public void testInfiniteCycleWhileCaseParsing() throws Throwable {
    this.
    doTest("");
  }

  public void testCreateStructWithQuotedLValue() throws Throwable {
    this.doTest("");
  }

  public void testDefaultValueInFunctionDefinition() throws Throwable {
    this.doTest("");
  }

  public void testTernaryOperator() throws Throwable {
    doTest("");
  }

  public void testAbort() throws Throwable {
    doTest("");
  }

  public void testAbortAttributeInProperty() throws Throwable {
    doTest("");
  }

  public void testPropertyAsVariable() throws Throwable {
    doTest("");
  }

  public void testDefineInterfaceCaseInsensitive() throws Throwable {
    doTest("/newSyntax");
  }

  public void testInclude() throws Throwable { doTest(); }
  public void testPageencoding() throws Throwable { doTest(); }
  public void testForInWithVar() throws Throwable { doTest(); }
  public void testConcateq() throws Throwable { doTest(); }
  public void testImplicitArrayOrStruct() throws Throwable { doTest(); }

  public void testForInWithArray() throws Throwable {
    doTest();
  }

  public void testForInWithField() throws Throwable {
    doTest();
  }

  public void testStruct_cf10() throws Throwable {
    doTest();
  }

  public void testElvis_cf11() throws Throwable {
    doTest();
  }

  private void doTest() throws IOException {
    doTest("");
  }

  /**
   * @param relatedPath - "/directory"
   */
  private void doTest(String relatedPath) throws IOException {
    Util.doParserTest(getTestName(true), getProject(), getDataSubpath() + relatedPath + "/");
  }

  protected String getDataSubpath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + "/cfscript/parser";
  }
}
