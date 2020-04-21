// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.ModuleFixture;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext;
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext;
import com.intellij.testFramework.utils.parameterInfo.MockUpdateParameterInfoContext;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.info.DartFunctionDescription;
import com.jetbrains.lang.dart.ide.info.DartParameterInfoHandler;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartParameterInfoTest extends CodeInsightFixtureTestCase<ModuleFixtureBuilder<ModuleFixture>> {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @Override
  protected String getBasePath() {
    return "/paramInfo";
  }

  private void doTest(String infoText, int highlightedParameterIndex, int highlightStart, int highlightEnd) {
    myFixture.configureByFile(getTestName(false) + "." + DartFileType.DEFAULT_EXTENSION);
    myFixture.doHighlighting(); // warm up the server

    final DartParameterInfoHandler parameterInfoHandler = new DartParameterInfoHandler();
    MockCreateParameterInfoContext createContext = new MockCreateParameterInfoContext(getEditor(), getFile());
    PsiElement elt = parameterInfoHandler.findElementForParameterInfo(createContext);
    assertNotNull(elt);
    parameterInfoHandler.showParameterInfo(elt, createContext);
    Object[] items = createContext.getItemsToShow();
    assertNotNull(items);
    assertTrue(items.length > 0);
    MockParameterInfoUIContext<PsiElement> context = new MockParameterInfoUIContext<>(elt);
    parameterInfoHandler.updateUI((DartFunctionDescription)items[0], context);
    assertEquals(infoText, parameterInfoHandler.getParametersListPresentableText());

    // index check
    MockUpdateParameterInfoContext updateContext = new MockUpdateParameterInfoContext(getEditor(), getFile());
    final PsiElement element = parameterInfoHandler.findElementForUpdatingParameterInfo(updateContext);
    assertEquals(elt, element);
    parameterInfoHandler.updateParameterInfo(element, updateContext);
    assertEquals(highlightedParameterIndex, updateContext.getCurrentParameter());

    // range check
    if (highlightStart != -1) {
      assertEquals(highlightStart, context.getHighlightStart());
      assertEquals(highlightEnd, context.getHighlightEnd());
    }
  }

  private void doTest(String infoText, int highlightedParameterIndex) {
    doTest(infoText, highlightedParameterIndex, -1 /* ignored */, -1 /* ignored */);
  }

  public void testParamInfo1() {
    doTest("int p1, p2, Node p3", 0, 0, 6);
  }

  public void testParamInfo2() {
    doTest("int p1, p2, Node p3", 2);
  }

  public void testParamInfo3() {
    doTest("int x, int y", 0);
  }

  public void testParamInfo4() {
    doTest("int x, int y", 0);
  }

  public void testParamInfo5() {
    doTest("int x, int y", 1);
  }

  public void testParamInfo6() {
    doTest("int x, {int y: 239}", 1);
  }

  public void testParamInfo7() {
    doTest("int x, {int y: 239}", 0);
  }

  public void testParamInfo8() {
    doTest("String one, [String two, String three]", 0, 0, 10);
  }

  public void testParamInfo9() {
    doTest("String one, [String two, String three]", 1);
  }

  public void testParamInfo10() {
    doTest("String one, [String two, String three]", 1);
  }

  public void testParamInfo11() {
    doTest("String one, [String two, String three]", 1);
  }

  public void testParamInfo13() {
    doTest("Foo<Foo, Foo> param", 0);
  }

  public void testParamInfo14() {
    doTest("<no parameters>", 0);
  }

  public void testParamInfo15() {
    doTest("[String s]", 0);
  }

  public void testParamInfo16() {
    doTest("{String s}", 0, 1, 9);
  }

  public void testParamInfo17() {
    doTest("{String s: 'foo'}", 0, 1, 16);
  }

  public void testParamInfo18() {
    doTest("[String s = 'foo']", 0, 1, 17);
  }

  public void testParamInfo19() {
    doTest("[String str]", 0);
  }

  public void testParamInfo20() {
    doTest("[String str]", 0);
  }

  public void testParamInfo21() {
    doTest("[String str]", 0);
  }

  public void testParamInfo22() {
    doTest("{String str}", 0);
  }

  public void testParamInfo23() {
    doTest("{String str}", 0);
  }

  public void testParamInfo24() {
    doTest("{String str}", 0);
  }

  public void testParamInfo_call_localVariable() {
    doTest("int a, double b", 0);
  }

  public void testParamInfo_call_newExpression() {
    doTest("int a, double b", 0);
  }

  public void testParamInfo_call_functionInvocation() {
    doTest("int a, double b", 0);
  }

  public void testParamInfo_call_getterInvocation() {
    doTest("int a, double b", 0);
  }

  public void testParamInfo_fieldFormal_normal() {
    doTest("int a, double b", 0);
  }

  public void testParamInfo_fieldFormal_named() {
    doTest("int a, {double b}", 0);
  }

  public void testAnnotation() {
    doTest("String itemA, int itemB", 0);
  }
}