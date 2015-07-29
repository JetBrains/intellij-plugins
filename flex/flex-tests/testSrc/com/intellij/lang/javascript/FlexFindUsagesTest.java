package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.psi.PsiReference;
import com.intellij.util.ThrowableRunnable;

/**
 * @author Konstantin.Ulitin
 */
public class FlexFindUsagesTest extends JSAbstractFindUsagesTest {

  @Override
  protected String getBasePath() {
    return "/as_findUsages/";
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  public void testFindConstructorUsages() throws Exception {
    PsiReference[] references = findElementAtCaret("ConstructorUsages.js2");
    assertEquals("Constructor references",2, references.length);
  }

  public void testFindClassUsages() throws Exception {
    PsiReference[] references = findElementAtCaret("ClassUsages.js2");
    assertEquals("Class references", 3, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindMxmlComponentsFileReferences() throws Exception {
    PsiReference[] references = findElementAtCaret("18.mxml", "C18_2.mxml", "18_3.mxml", "18_4.as");
    assertEquals("Mxml component files references", 5, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindMxmlComponentsFileReferences_2() throws Exception {
    PsiReference[] references;

    references = findElementAtCaret("18.as", "C18_2.mxml", "18_3.mxml", "18_4.as");
    assertEquals("Mxml component files references", 5, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindMxmlComponentsFileReferences2() throws Exception {
    PsiReference[] references = findElementAtCaret("19.mxml", "19_2.as", "19_3.mxml");
    assertEquals("Mxml component files references",4, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindParameterWith$InMxmlComponent() throws Exception {
    PsiReference[] references = findElementAtCaret("20.mxml");
    assertEquals("Parameter with $ in mxml component",2, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindImplementationMethodWhenSearchingFromInterface() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals("Find implementation method when searching for", 3, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindPrivateVarInMxml() throws Exception {
    PsiReference[] references = findElementAtCaret("21.mxml");
    assertEquals("Mxml component private variable", 3, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindSetterUsagesInMxml() throws Exception {
    String s = getTestName(false);
    PsiReference[] references = findElementAtCaret( s + ".mxml", s + "_2.mxml" );
    assertEquals("Setter usages", 7, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindGetterUsagesInMxml() throws Exception {
    String s = getTestName(false);
    PsiReference[] references = findElementAtCaret( s + ".mxml", s + "_2.mxml" );
    assertEquals("Getter usages", 4, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindPackageRefs() throws Exception {
    String testName = getTestName(false);
    PsiReference[] references = findElementAtCaret(testName + ".mxml", testName + "_2.as");
    assertEquals("Package refs", 5, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMixJsAndJs2() throws Exception {
    String testName = getTestName(false);
    PsiReference[] references = findElementAtCaret(testName + ".js2", testName + ".mxml", testName + ".js");
    assertEquals( 3, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMixJsAndJs2_2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.JS_1_6, getProject(), new ThrowableRunnable<Exception>() {
      @Override
      public void run() throws Exception {
        String testName = getTestName(false);
        PsiReference[] references = findElementAtCaret(testName + ".js", testName + ".mxml", testName + ".js2");
        assertEquals(3, references.length);
      }
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testNoDynamicUsages() throws Exception {
    String testName = getTestName(false);
    PsiReference[] references = findElementAtCaret(testName + ".js2");
    assertEquals(3, references.length);
  }

  public void testSuperCall() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(3, references.length);
  }

  public void testSuperCall2() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(3, references.length);
  }

  public void testInheritorCall() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  public void testInheritorCall2() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(3, references.length);
  }

  public void testReferenceInStringLiteral() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as", getTestName(false) + "_2.mxml");
    assertEquals(1, references.length);
  }

  public void testASGenericsAndDollarsInName() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(2, references.length);
  }
}
