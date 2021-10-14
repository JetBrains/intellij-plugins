// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiNamedElement;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartTestUtils;

import java.util.List;

public class DartGotoImplementationTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/analysisServer/gotoImplementation");
  }

  protected void doTest(int expectedLength) {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.doHighlighting();
    doTestInner(expectedLength);
  }

  private void doTestInner(int expectedLength) {
    final GotoTargetHandler.GotoData data = CodeInsightTestUtil.gotoImplementation(myFixture.getEditor(), myFixture.getFile());
    assertNotNull(myFixture.getFile().toString(), data);
    assertEquals(expectedLength, data.targets.length);
  }

  public void testGti1() {
    doTest(2);
  }

  public void testGti2() {
    doTest(1);
  }

  public void testGti3() {
    doTest(2);
  }

  public void testGti4() {
    doTest(1);
  }

  public void testMixin1() {
    doTest(1);
  }

  public void testOperator() {
    doTest(3);
  }

  public void testIterableSubclasses() throws Throwable {
    myFixture.configureByText("foo.dart", "Iterable i;");
    myFixture.doHighlighting();
    final DartSdk sdk = DartSdk.getDartSdk(getProject());
    assertNotNull(sdk);

    final GotoTargetHandler.GotoData data = CodeInsightTestUtil.gotoImplementation(myFixture.getEditor(), myFixture.getFile());
    final List<String> actual = ContainerUtil.map(data.targets,
                                                  psiElement -> psiElement instanceof PsiNamedElement
                                                                ? ((PsiNamedElement)psiElement).getName()
                                                                : psiElement.toString());

    assertContainsElements(actual, "List", "Set", "Runes", "LinkedHashSet", "UnmodifiableListView", "ListBase",
                           "UnmodifiableInt32x4ListView", "_SplayTreeValueIterable");
  }
}
