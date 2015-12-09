/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.lang.CodeInsightActions;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartServerGotoSuperHandlerTest extends CodeInsightFixtureTestCase {

  protected String getBasePath() {
    return "/analysisServer/gotoSuper";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
  }

  public void testSuperClass() throws Throwable {
    doTest();
  }

  public void testSuperClassMethod() throws Throwable {
    doTest();
  }

  public void testSuperInterface() throws Throwable {
    doTest();
  }

  public void testSuperOperator() throws Throwable {
    doTest();
  }

  private void doTest() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".dart");
    initServer();
    final CodeInsightActionHandler handler = CodeInsightActions.GOTO_SUPER.forLanguage(DartLanguage.INSTANCE);
    handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile());
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  private void initServer() {
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
    myFixture.doHighlighting(); // make sure server is warmed up
  }
}
