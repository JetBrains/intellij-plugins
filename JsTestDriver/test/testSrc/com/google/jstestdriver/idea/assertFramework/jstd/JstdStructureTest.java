package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.AbstractJsPsiTestCase;
import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.psi.PsiElement;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JstdStructureTest extends AbstractJsPsiTestCase {

  public void testEmptyAsyncTestCase() throws Exception {
    validateJsFile();
  }

  public void testEmptyTestCase() throws Exception {
    validateJsFile();
  }

  public void testSingleObjectLiteralTest() throws Exception {
    validateJsFile();
  }

  public void testMultipleObjectLiteralTests() throws Exception {
    validateJsFile();
  }

  public void testMultipleTestCases() throws Exception {
    validateJsFile();
  }

  public void testPrototypeTestCase() throws Exception {
    validateJsFile();
  }

  public void testPrototypeTestCase1() throws Exception {
    validateJsFile();
  }

  public void testMixObjectLiteralAndPrototypeMethodTests_with_var() throws Exception {
    validateJsFile();
  }

  public void testMixObjectLiteralAndPrototypeMethodTests_without_var() throws Exception {
    validateJsFile();
  }

  public void testTestCaseNameResolve() throws Exception {
    validateJsFile();
  }

  @Override
  protected String getTestDataPath() {
    return JsTestDriverTestUtils.getTestDataDir().getAbsolutePath() + "/assertFramework/jstd/structure/";
  }

  @Override
  protected void validateJsFile(JSFile jsFile, String fileText) throws Exception {
    JstdTestFileStructure jsTestFileStructure = buildJsTestFileStructureByJsFile(jsFile);
    MarkedJsTestFileStructure markedJsTestFileStructure = MarkedJstdTestStructureUtils.buildMarkedJsTestFileStructureByFileText(
      fileText, jsFile
    );
    validateJsTestFileStructure(markedJsTestFileStructure, jsTestFileStructure);
  }

  @NotNull
  private static JstdTestFileStructure buildJsTestFileStructureByJsFile(@NotNull JSFile jsFile) throws Exception {
    JstdTestFileStructure jsTestFileStructure = JstdTestFileStructureBuilder.getInstance().buildTestFileStructure(jsFile);
    Assert.assertTrue(jsTestFileStructure.getJsFile() == jsFile);
    return jsTestFileStructure;
  }

  private static void validateJsTestFileStructure(MarkedJsTestFileStructure markedJsTestFileStructure,
                                                  JstdTestFileStructure jsTestFileStructure) {
    List<MarkedTestCaseStructure> markedTestCaseStructures = markedJsTestFileStructure.getMarkedTestCaseStructures();
    for (MarkedTestCaseStructure markedTestCaseStructure : markedTestCaseStructures) {
      JstdTestCaseStructure testCaseStructure = jsTestFileStructure.getTestCaseStructureByName(markedTestCaseStructure.getName());
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

  private static void matchTestCase(@NotNull MarkedTestCaseStructure markedTestCaseStructure, @NotNull JstdTestCaseStructure testCaseStructure) {
    Assert.assertEquals(testCaseStructure.getName(), markedTestCaseStructure.getName());
    Assert.assertTrue(testCaseStructure.getEnclosingCallExpression() == markedTestCaseStructure.getPsiElement());
    List<MarkedTestStructure> markedTestStructures = markedTestCaseStructure.getMarkedTestStructures();
    for (MarkedTestStructure markedTestStructure : markedTestStructures) {
      JstdTestStructure testStructure = testCaseStructure.getTestStructureByName(markedTestStructure.getName());
      if (testStructure == null) {
        Assert.fail("Test '" + markedTestStructure.getName() + "' is not found in test-case '" + testCaseStructure.getName() + "'!");
      }
      matchTest(markedTestStructure, testStructure);
    }
    if (markedTestStructures.size() != testCaseStructure.getTestCount()) {
      Assert.fail("Marked test count is " + markedTestStructures.size()
                                          + ", but built tests count is " + testCaseStructure.getTestCount());
    }
  }

  private static void matchTest(@NotNull MarkedTestStructure markedTestStructure, @NotNull JstdTestStructure testStructure) {
    Assert.assertEquals("Build test name is not equal to the marked one", markedTestStructure.getName(), testStructure.getName());
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
    Assert.assertTrue("Expected " + markedDescription + ", but found " + foundDescription, markedPsiElement == foundPsiElement);
  }
}
