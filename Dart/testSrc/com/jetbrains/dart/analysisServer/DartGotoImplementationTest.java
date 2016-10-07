package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

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
    final GotoTargetHandler.GotoData data = CodeInsightTestUtil.gotoImplementation(myFixture.getEditor(), myFixture.getFile());
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
    letAnalyzerSmellCoreFile(sdk, "set.dart");
    letAnalyzerSmellCoreFile(sdk, "string.dart");
    letAnalyzerSmellCoreFile(sdk, "list.dart");
    letAnalyzerSmellCoreFile(sdk, "iterable.dart");

    final DartClass iterableClass = PsiTreeUtil.findChildOfType(getFile(), DartClass.class);
    assertNotNull(iterableClass);
    assertEquals("Iterable", iterableClass.getName());
    getEditor().getCaretModel().moveToOffset(iterableClass.getTextOffset());

    final GotoTargetHandler.GotoData data = CodeInsightTestUtil.gotoImplementation(myFixture.getEditor(), myFixture.getFile());
    final List<String> actual = ContainerUtil.map(data.targets,
                                                  psiElement -> psiElement instanceof PsiNamedElement
                                                                ? ((PsiNamedElement)psiElement).getName()
                                                                : psiElement.toString());

    assertSameElements(actual, "List", "Set", "Runes"); // only subclasses from dart:core are known to analyzer at this point
  }

  private void letAnalyzerSmellCoreFile(@NotNull final DartSdk sdk, @NotNull final String fileName)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final VirtualFile iterableFile = LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib/core/" + fileName);
    assertNotNull(iterableFile);
    myFixture.openFileInEditor(iterableFile);

    // let's keep updateVisibleFiles() method package-local, but here we need to invoke it because FileEditorManagerListener is not notified in test environment
    final Method method = DartAnalysisServerService.class.getDeclaredMethod("updateVisibleFiles");
    method.setAccessible(true);
    method.invoke(DartAnalysisServerService.getInstance());
  }
}
