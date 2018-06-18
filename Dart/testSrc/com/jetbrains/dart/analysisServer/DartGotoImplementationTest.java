// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
