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
import java.util.concurrent.Callable;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlParserTest extends CfmlCodeInsightFixtureTestCase {

  // public void testInit() throws Throwable {}

  public void testDynamicVarName() throws Throwable {
    doTest();
  }

  public void testUnaryInc() throws Throwable {
    doTest();
  }

  public void testUnclosedCfset() throws Throwable {
    doTest();
  }

  public void testRailoCfadminTag() throws Throwable {
    Util.runTestWithLanguageLevel(() -> {
      doTest();
      return null;
    }, CfmlLanguage.RAILO, getProject());
  }

  public void testOpenCloseTag() throws Throwable {
    doTest();
  }

  public void testSingleTag() throws Throwable {
    doTest();
  }

  public void testExprInAttr() throws Throwable {
    doTest();
  }

  public void testNestedSharps() throws Throwable {
    doTest();
  }

  // tests on incorrect input
  public void testIunbalanceTags() throws Throwable {
    doTest();
  }

  public void testIunbalanceSharps() throws Throwable {
    doTest();
  }

  public void testIquotesInSharps() throws Throwable {
    doTest();
  }

  public void testIbadToken() throws Throwable {
    doTest();
  }

  public void testNonUniqueUnclosedTag() throws Throwable {
    doTest();
  }

  public void testCfinvokeTagMightBeSingle() throws Throwable {
    doTest();
  }

  public void testParseAssignAsExpression() throws Throwable {
    doTest();
  }

  public void testUnquotedConstantAsAttributeValue() throws Throwable {
    doTest();
  }

  public void testCfThreadAsInCf9Syntax() throws Throwable {
    doTest();
  }

  public void testUnaryNot() throws Throwable {
    doTest();
  }

  public void testStructureInsideArrayDef() throws Throwable {
    doTest();
  }

  public void testCfZip() throws Throwable {
    doTest();
  }

  public void testCfTagWithPrefixWithoutCloseTag() throws Throwable {
    doTest();
  }

  public void testNewExpression() throws Throwable {
    doTest();
  }

  public void testPowOperator() throws Throwable {
    doTest();
  }

  public void testStructureDefinitionAsParameter() throws Throwable {
    doTest();
  }

  public void testFinally() throws Throwable {
    doTest();
  }

  public void testClosure() throws Throwable {
    doTest();
  }

  public void testStructureInStringDefinition() throws Throwable {
    doTest();
  }

  public void testArrayDefInArgumentsList() throws Throwable {
    doTest();
  }

  public void testCfloop() throws Throwable {
    doTest();
  }

  public void testAccessMethodViaArray() throws Throwable {
    doTest();
  }

  public void testCreateQueryInstance() throws Throwable {
    doTest();
  }

  public void testActions() throws Throwable {
    doTest();
  }

  public void testExpressions() throws Throwable {
    doTest();
  }

  public void testTransactionInScript() throws Throwable {
    doTest();
  }

  private void doTest() throws IOException {
    Util.doParserTest(getTestName(true), getProject(), getDataSubpath());
  }

  protected String getDataSubpath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + "/parser/";
  }
}
