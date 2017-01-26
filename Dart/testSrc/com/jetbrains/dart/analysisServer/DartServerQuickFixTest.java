package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class DartServerQuickFixTest extends CodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  protected String getBasePath() {
    return "/analysisServer/quickfix";
  }

  private void doQuickFixTest(@NotNull final String intentionStartText) {
    doQuickFixTest(intentionStartText, null, true);
  }

  private void doQuickFixTest(@NotNull final String intentionStartText, @Nullable final VirtualFile fileUpdatedByFix) {
    doQuickFixTest(intentionStartText, fileUpdatedByFix, true);
  }

  /**
   * @param fileUpdatedByFix <code>null</code> if quick fix updates the file from which it was invoked
   */
  private void doQuickFixTest(@NotNull final String intentionStartText,
                              @Nullable final VirtualFile fileUpdatedByFix,
                              boolean fixAvailable) {
    myFixture.configureByFile(getTestName(false) + ".dart");

    final IntentionAction quickFix = myFixture.findSingleIntention(intentionStartText);
    assertEquals(fixAvailable, quickFix.isAvailable(getProject(), getEditor(), getFile()));
    if (!fixAvailable) return;

    if (fileUpdatedByFix != null) {
      // todo This is a workaround because DartQuickFix.navigate() behaves differently in test environment.
      // Would be great to remove this code and instead check that correct editor becomes open after fix.invoke()
      myFixture.openFileInEditor(fileUpdatedByFix);
    }

    ApplicationManager.getApplication().runWriteAction(() -> quickFix.invoke(getProject(), getEditor(), getFile()));

    final String updatedFileName = fileUpdatedByFix == null ? getTestName(false) : fileUpdatedByFix.getNameWithoutExtension();
    myFixture.checkResultByFile(updatedFileName + ".after.dart");
  }

  public void testCreateMethodInAnotherFile() throws Throwable {
    final VirtualFile partFile = myFixture.copyFileToProject(getTestName(false) + "_part.dart");
    doQuickFixTest("Create method 'doSomething'", partFile);
  }

  public void testCreateClass() throws Throwable {
    doQuickFixTest("Create class 'A'");
  }

  public void testCreatePartFile() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".dart");

    final IntentionAction quickFix = myFixture.findSingleIntention("Create file 'CreatePartFile_part.dart'");
    assertEquals(true, quickFix.isAvailable(getProject(), getEditor(), getFile()));

    ApplicationManager.getApplication().runWriteAction(() -> quickFix.invoke(getProject(), getEditor(), getFile()));

    final VirtualFile newFile = myFixture.findFileInTempDir("CreatePartFile_part.dart");
    assertNotNull(newFile);

    // TODO content of created file is not added because DartQuickFix.navigate() behaves differently in test environment
    //myFixture.openFileInEditor(newFile);
    //myFixture.checkResultByFile("CreatePartFile_part.after.dart");
  }

  public void testUseEqEqNull() throws Throwable {
    doQuickFixTest("Use == null instead of 'is Null'");
  }

  private void doCrLfAwareTest(@NotNull final String content, @NotNull final String intentionStartText, @NotNull final String after) {
    final VirtualFile file = myFixture.configureByText("foo.dart", content).getVirtualFile();

    // configureByText() has normalized line breaks, save once again
    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        final int i = content.indexOf("<caret>");
        VfsUtil.saveText(file, content.substring(0, i) + content.substring(i + "<caret>".length()));
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    final IntentionAction quickFix = myFixture.findSingleIntention(intentionStartText);
    ApplicationManager.getApplication().runWriteAction(() -> quickFix.invoke(getProject(), getEditor(), getFile()));

    myFixture.checkResult(after);
  }

  public void testHandleCrlf1() throws Throwable {
    final String content = "\r\n\r\n \r\r \n\n" +
                           "class A{\r\n" +
                           "}\r\n" +
                           "foo() {\r\n" +
                           "  List a = new A().<caret>bar(1, true, '');\r\n" +
                           "}";
    final String after = "\n\n \n\n \n\n" +
                         "class A{\n" +
                         "  List<caret> bar(int i, bool arg1, String s) {}\n" +
                         "}\n" +
                         "foo() {\n" +
                         "  List a = new A().bar(1, true, '');\n" +
                         "}";
    doCrLfAwareTest(content, "Create method", after);
  }

  public void testHandleCrlf2() throws Throwable {
    final String content = "\r\n\r\n \r\r \n\n" +
                           "foo() {\r\n" +
                           "  List a = new A().<caret>bar(1, true, '');\r\n" +
                           "}\r\n" +
                           "class A{\r\n" +
                           "}";
    final String after = "\n\n \n\n \n\n" +
                         "foo() {\n" +
                         "  List a = new A().bar(1, true, '');\n" +
                         "}\n" +
                         "class A{\n" +
                         "  List<caret> bar(int i, bool arg1, String s) {}\n" +
                         "}";
    doCrLfAwareTest(content, "Create method", after);
  }
}
