package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.navigation.GotoImplementationHandler;
import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartGotoImplementationTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/analysisServer/gotoImplementation");
  }

  protected void doTest(int expectedLength) throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".dart");
    myFixture.doHighlighting();
    doTestInner(expectedLength);
  }

  protected void doTestInner(int expectedLength) {
    final GotoTargetHandler.GotoData data =
      new GotoImplementationHandler().getSourceAndTargetElements(myFixture.getEditor(), myFixture.getFile());
    assertNotNull(myFixture.getFile().toString(), data);
    assertEquals(expectedLength, data.targets.length);
  }

  public void testGti1() throws Throwable {
    doTest(2);
  }

  public void testGti2() throws Throwable {
    doTest(1);
  }

  public void testGti3() throws Throwable {
    doTest(2);
  }

  public void testGti4() throws Throwable {
    doTest(1);
  }

  public void testMixin1() throws Throwable {
    doTest(1);
  }

  public void testOperator() throws Throwable {
    doTest(3);
  }

  public void testIterableSubclasses() throws Throwable {
    myFixture.configureByText("foo.dart", "");
    myFixture.doHighlighting();
    final DartSdk sdk = DartSdk.getDartSdk(getProject());
    assertNotNull(sdk);
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib/core/iterable.dart");
    assertNotNull(file);
    myFixture.openFileInEditor(file);
    final DartClass iterableClass = PsiTreeUtil.findChildOfType(getFile(), DartClass.class);
    assertNotNull(iterableClass);
    assertEquals("Iterable", iterableClass.getName());
    final PsiElement nameIdentifier = iterableClass.getNameIdentifier();
    assertNotNull(nameIdentifier);
    getEditor().getCaretModel().moveToOffset(nameIdentifier.getTextRange().getStartOffset());
    final GotoTargetHandler.GotoData data =
      new GotoImplementationHandler().getSourceAndTargetElements(myFixture.getEditor(), myFixture.getFile());
    assertNotNull(data);
    assertTrue(String.valueOf(data.targets.length), data.targets.length > 50);
  }
}
