package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.JstdTestRoot;
import com.intellij.javascript.testFramework.AbstractJsPsiTestCase;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JstdStructureTest extends AbstractJsPsiTestCase {

  public void testEmptyAsyncTestCase() {
    validateJsFile();
  }

  public void testEmptyTestCase() {
    validateJsFile();
  }

  public void testSingleObjectLiteralTest() {
    validateJsFile();
  }

  public void testMultipleObjectLiteralTests() {
    validateJsFile();
  }

  public void testMultipleTestCases() {
    validateJsFile();
  }

  public void testPrototypeTestCase() {
    validateJsFile();
  }

  public void testPrototypeTestCase1() {
    validateJsFile();
  }

  public void testMixObjectLiteralAndPrototypeMethodTests_with_var() {
    validateJsFile();
  }

  public void testMixObjectLiteralAndPrototypeMethodTests_without_var() {
    validateJsFile();
  }

  public void testTestCaseNameResolve() {
    validateJsFile();
  }

  @Override
  protected String getTestDataPath() {
    return JstdTestRoot.getTestDataDir().getAbsolutePath() + "/assertFramework/jstd/structure/";
  }

  @Override
  protected void validateJsFile(JSFile jsFile, String fileText) {
    JstdTestFileStructure jsTestFileStructure = buildJsTestFileStructureByJsFile(jsFile);
    MarkedJsTestFileStructure markedJsTestFileStructure = MarkedJstdTestStructureUtils.buildMarkedJsTestFileStructureByFileText(
      fileText, jsFile
    );
    validateJsTestFileStructure(markedJsTestFileStructure, jsTestFileStructure);
  }

  @NotNull
  private static JstdTestFileStructure buildJsTestFileStructureByJsFile(@NotNull JSFile jsFile) {
    JstdTestFileStructure jsTestFileStructure = JstdTestFileStructureBuilder.getInstance().buildTestFileStructure(jsFile);
    assertSame(jsTestFileStructure.getJsFile(), jsFile);
    return jsTestFileStructure;
  }

  private static void validateJsTestFileStructure(MarkedJsTestFileStructure markedJsTestFileStructure,
                                                  JstdTestFileStructure jsTestFileStructure) {
    List<MarkedTestCaseStructure> markedTestCaseStructures = markedJsTestFileStructure.getMarkedTestCaseStructures();
    for (MarkedTestCaseStructure markedTestCaseStructure : markedTestCaseStructures) {
      JstdTestCaseStructure testCaseStructure = jsTestFileStructure.getTestCaseStructureByName(markedTestCaseStructure.getName());
      if (testCaseStructure == null) {
        fail("TestCase with name '" + markedTestCaseStructure.getName() + "' is not found in jsTestFileStructure!");
      }
      matchTestCase(markedTestCaseStructure, testCaseStructure);
    }
    if (markedTestCaseStructures.size() != jsTestFileStructure.getTestCaseCount()) {
      fail("Marked testCases count is " + markedTestCaseStructures.size()
                                          + ", but built testCases count is " + jsTestFileStructure.getTestCaseCount());
    }
  }

  private static void matchTestCase(@NotNull MarkedTestCaseStructure markedTestCaseStructure, @NotNull JstdTestCaseStructure testCaseStructure) {
    assertEquals(testCaseStructure.getName(), markedTestCaseStructure.getName());
    assertSame(testCaseStructure.getEnclosingCallExpression(), markedTestCaseStructure.getPsiElement());
    List<MarkedTestStructure> markedTestStructures = markedTestCaseStructure.getMarkedTestStructures();
    for (MarkedTestStructure markedTestStructure : markedTestStructures) {
      JstdTestStructure testStructure = testCaseStructure.getTestStructureByName(markedTestStructure.getName());
      if (testStructure == null) {
        fail("Test '" + markedTestStructure.getName() + "' is not found in test-case '" + testCaseStructure.getName() + "'!");
      }
      matchTest(markedTestStructure, testStructure);
    }
    if (markedTestStructures.size() != testCaseStructure.getTestCount()) {
      fail("Marked test count is " + markedTestStructures.size()
                                          + ", but built tests count is " + testCaseStructure.getTestCount());
    }
  }

  private static void matchTest(@NotNull MarkedTestStructure markedTestStructure, @NotNull JstdTestStructure testStructure) {
    assertEquals("Build test name is not equal to the marked one", markedTestStructure.getName(), testStructure.getName());
    String testName = testStructure.getName();
    JSProperty markedJsProperty = markedTestStructure.getPropertyPsiElement();
    if (markedJsProperty != null) {
      matchPsiElement(testName, "property", markedJsProperty, testStructure.getJsProperty());
    } else {
      matchPsiElement(testName, "declaration", markedTestStructure.getDeclarationPsiElement(), testStructure.getTestMethodNameDeclaration());
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
    assertSame("Expected " + markedDescription + ", but found " + foundDescription, markedPsiElement, foundPsiElement);
  }
}
