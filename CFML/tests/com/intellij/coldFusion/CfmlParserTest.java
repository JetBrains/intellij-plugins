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

import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.DebugUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by Lera Nikolaenko
 * Date: 14.11.2008
 */
public class CfmlParserTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
  }

  // public void testInit() throws Throwable {}
  public void testUnclosedCfset() throws Throwable {
    doTest();
  }

  public void testRailoCfadminTag() throws Throwable {
    CfmlProjectConfiguration.State currentState = new CfmlProjectConfiguration.State();
    try {
      currentState.setLanguageLevel(CfmlLanguage.RAILO);
      CfmlProjectConfiguration.getInstance(getProject()).loadState(currentState);
      doTest();
    }
    finally {
      currentState.setLanguageLevel(CfmlLanguage.CF9);
      CfmlProjectConfiguration.getInstance(getProject()).loadState(currentState);
    }
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
    String fileName = getTestName(true) + ".cfml";

    String testText = StringUtil.convertLineSeparators(loadFile(getTestName(true) + ".test.cfml"));
    final PsiFile psiFile = PsiFileFactory.getInstance(getProject()).createFileFromText(fileName, testText);
    final String tree = DebugUtil.psiTreeToString(psiFile, true);

    assertSameLinesWithFile(getDataSubpath() + getTestName(true) + ".test.expected", tree);
  }

  private String loadFile(String fileName) throws IOException {
    return FileUtil.loadFile(new File(FileUtil.toSystemDependentName(getDataSubpath() + fileName)));
  }

  protected String getDataSubpath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + "/parser/";
  }
}
