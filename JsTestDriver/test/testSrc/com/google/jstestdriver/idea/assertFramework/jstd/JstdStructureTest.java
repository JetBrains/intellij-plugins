package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.google.jstestdriver.idea.assertFramework.BaseTestCaseStructure;
import com.google.jstestdriver.idea.assertFramework.BaseTestStructure;
import com.google.jstestdriver.idea.assertFramework.JsTestFileStructure;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.PsiTestCase;
import com.intellij.testFramework.TestDataFile;
import junit.framework.Assert;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class JstdStructureTest extends PsiTestCase {

  @Test
  public void testEmptyAsyncTestCase() throws Exception {
    validateJsFile();
  }

  @Test
  public void testEmptyTestCase() throws Exception {
    validateJsFile();
  }

  @Test
  public void testSingleObjectLiteralTest() throws Exception {
    validateJsFile();
  }

  @Test
  public void testMultipleObjectLiteralTests() throws Exception {
    validateJsFile();
  }

  @Test
  public void testMultipleTestCases() throws Exception {
    validateJsFile();
  }

  @Test
  public void testPrototypeTestCase() throws Exception {
    validateJsFile();
  }

  @Test
  public void testPrototypeAndLiteralSimpleMix() throws Exception {
    validateJsFile();
  }

  @Test
  public void testTestCaseNameResolve() throws Exception {
    validateJsFile();
  }

  @Override
  protected String getTestDataPath() {
    return JsTestDriverTestUtils.getTestDataDir().getAbsolutePath() + "/assertFramework/jstd/structure/";
  }

  private void validateJsFile() throws Exception {
    validateJsFile(getTestName(true));
  }

  private void validateJsFile(final String fileNameWithoutExtension) throws Exception {
    validateFile(fileNameWithoutExtension + ".js");
  }

  private void validateFile(@TestDataFile @NonNls String filePath) throws Exception {
    final String fullPath = getTestDataPath() + filePath;
    final String fullRefinedPath = fullPath.replace(File.separatorChar, '/');
    final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(fullRefinedPath);
    Assert.assertNotNull("file " + fullRefinedPath + " not found", vFile);
    String fileText = StringUtil.convertLineSeparators(VfsUtil.loadText(vFile));
    final String fileName = vFile.getName();

    JSFile jsFile = createJsFile(fileText, fileName);
    JsTestFileStructure jsTestFileStructure = buildJsTestFileStructureByJsFile(jsFile);
    MarkedJsTestFileStructure markedJsTestFileStructure = MarkedJstdTestStructureUtils.buildMarkedJsTestFileStructureByFileText(
      fileText, jsFile
    );
    validateJsTestFileStructure(markedJsTestFileStructure, jsTestFileStructure);
  }

  private JSFile createJsFile(String fileText, String fileName) throws Exception {
    myFile = createFile(myModule, fileName, fileText);
    JSFile jsFile = CastUtils.tryCast(myFile, JSFile.class);
    if (jsFile == null) {
      Assert.fail(JSFile.class + " was expected, but " + (myFile == null ? "null " : myFile.getClass()) + " found.");
    }
    return jsFile;
  }

  @NotNull
  private static JsTestFileStructure buildJsTestFileStructureByJsFile(@NotNull JSFile jsFile) throws Exception {
    JsTestFileStructure jsTestFileStructure = JstdTestFileStructureBuilder.INSTANCE.buildTestFileStructure(jsFile);
    Assert.assertTrue(jsTestFileStructure.getJsFile() == jsFile);
    return jsTestFileStructure;
  }

  private static void validateJsTestFileStructure(MarkedJsTestFileStructure markedJsTestFileStructure,
                                                  JsTestFileStructure jsTestFileStructure) {
    List<MarkedTestCaseStructure> markedTestCaseStructures = markedJsTestFileStructure.getMarkedTestCaseStructures();
    for (MarkedTestCaseStructure markedTestCaseStructure : markedTestCaseStructures) {
      BaseTestCaseStructure testCaseStructure = jsTestFileStructure.getTestCaseStructureByName(markedTestCaseStructure.getName());
      if (testCaseStructure == null) {
        Assert.fail("TestCase with name '" + markedTestCaseStructure.getName() + "' is not found in jsTestFileStructure!");
      }
      matchTestCase(markedTestCaseStructure, testCaseStructure);
    }
    if (markedTestCaseStructures.size() != jsTestFileStructure.getTestCaseCount()) {
      Assert.fail("Marked testCases count is " + markedTestCaseStructures.size()
                                          + ", but built testCases count is " + jsTestFileStructure.getTestCaseCount());
    }
  }

  private static void matchTestCase(@NotNull MarkedTestCaseStructure markedTestCaseStructure, @NotNull BaseTestCaseStructure testCaseStructure) {
    Assert.assertEquals(testCaseStructure.getTestCaseName(), markedTestCaseStructure.getName());
    Assert.assertTrue(testCaseStructure.getPsiElement() == markedTestCaseStructure.getPsiElement());
    List<MarkedTestStructure> markedTestStructures = markedTestCaseStructure.getMarkedTestStructures();
    for (MarkedTestStructure markedTestStructure : markedTestStructures) {
      BaseTestStructure testStructure = testCaseStructure.getTestStructureByName(markedTestStructure.getName());
      if (testStructure == null) {
        Assert.fail("Test '" + markedTestStructure.getName() + "' is not found in test-case '" + testCaseStructure.getTestCaseName() + "'!");
      }
      matchTest(markedTestStructure, (JstdTestStructure)testStructure);
    }
    if (markedTestStructures.size() != testCaseStructure.getTestCount()) {
      Assert.fail("Marked test count is " + markedTestStructures.size()
                                          + ", but built tests count is " + testCaseStructure.getTestCount());
    }
  }

  private static void matchTest(@NotNull MarkedTestStructure markedTestStructure, @NotNull JstdTestStructure testStructure) {
    Assert.assertEquals("Build test name is not equal to the marked one", markedTestStructure.getName(), testStructure.getTestName());
    String testName = testStructure.getTestName();
    JSProperty markedJsProperty = markedTestStructure.getPropertyPsiElement();
    if (markedJsProperty != null) {
      matchPsiElement(testName, "property", markedJsProperty, testStructure.getJsProperty());
    } else {
      matchPsiElement(testName, "declaration", markedTestStructure.getDeclarationPsiElement(), testStructure.getTestMethodDeclaration());
      matchPsiElement(testName, "body", markedTestStructure.getBodyPsiElement(), testStructure.getTestMethodBody());
    }
  }

  private static String formatTestReferenceDescription(@NotNull String testName, String type, @NotNull PsiElement psiElement) {
    return "test named '" + testName + "' references " + psiElement.getClass() + " with text '" + psiElement.getText() + "' as type '" + type + "'";
  }

  private static void matchPsiElement(String testName, String type, @Nullable PsiElement markedPsiElement, @Nullable PsiElement foundPsiElement) {
    if (markedPsiElement == null) {
      throw new RuntimeException("Marked PsiElement for type '" + type + "' is null, test is '" + testName + "'");
    }
    String markedDescription = formatTestReferenceDescription(testName, type, markedPsiElement);
    if (foundPsiElement == null) {
      throw new RuntimeException("Test is '" + testName + "', psiElement is null for type '" + type + "'");
    }
    String foundDescription = formatTestReferenceDescription(testName, type, foundPsiElement);
    Assert.assertTrue("Expected " + markedDescription + ", but found " + foundDescription, markedPsiElement == foundPsiElement);
  }
}
