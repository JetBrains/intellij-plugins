package com.intellij.flex.codeInsight;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSAbstractFindUsagesTest;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.LightProjectDescriptor;

import java.util.Arrays;

public class FlexFindUsagesTest extends JSAbstractFindUsagesTest {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
  }

  @Override
  protected String getBasePath() {
    return "/as_findUsages/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("") + getBasePath();
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  public void testFindConstructorUsages() {
    PsiReference[] references = findElementAtCaret("ConstructorUsages.js2");
    assertEquals("Constructor references", 1, references.length);
  }

  public void testFindClassUsages() {
    PsiReference[] references = findElementAtCaret("ClassUsages.js2");
    assertEquals("Class references", 2, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindMxmlComponentsFileReferences() {
    PsiReference[] references = findElementAtCaret("18.mxml", "C18_2.mxml", "18_3.mxml", "18_4.as");
    assertEquals("Mxml component files references", 5, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindMxmlComponentsFileReferences_2() {
    PsiReference[] references;

    references = findElementAtCaret("18.as", "C18_2.mxml", "18_3.mxml", "18_4.as");
    assertEquals("Mxml component files references", 5, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindMxmlComponentsFileReferences2() {
    PsiReference[] references = findElementAtCaret("19.mxml", "19_2.as", "19_3.mxml");
    assertEquals("Mxml component files references", 3, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindParameterWith$InMxmlComponent() {
    PsiReference[] references = findElementAtCaret("20.mxml");
    assertEquals("Parameter with $ in mxml component", 2, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindImplementationMethodWhenSearchingFromInterface() {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals("Find implementation method when searching for", 2, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindPrivateVarInMxml() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
    PsiReference[] references = findElementAtCaret("21.mxml");
    assertEquals("Mxml component private variable", 2, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindSetterUsagesInMxml() {
    String s = getTestName(false);
    PsiReference[] references = findElementAtCaret(s + ".mxml", s + "_2.mxml");
    assertEquals("Setter usages", 6, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindGetterUsagesInMxml() {
    String s = getTestName(false);
    PsiReference[] references = findElementAtCaret(s + ".mxml", s + "_2.mxml");
    assertEquals("Getter usages", 3, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindPackageRefs() {
    String testName = getTestName(false);
    PsiReference[] references = findElementAtCaret(testName + ".mxml", testName + "_2.as");
    assertEquals("Package refs", 5, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMixJsAndJs2() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.JS_1_8_5, getProject(), () -> {
    String testName = getTestName(false);
    PsiReference[] references = findElementAtCaret(testName + ".js2", testName + ".mxml", testName + ".js");
    assertTrue(Arrays.stream(references).noneMatch(r -> r.getElement().getContainingFile().getName().endsWith(".js")));
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMixJsAndJs2_2() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.JS_1_8_5, getProject(), () -> {
      String testName = getTestName(false);
      PsiReference[] references = findElementAtCaret(testName + ".js", testName + ".mxml", testName + ".js2");
      assertEquals(2, references.length);
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testNoDynamicUsages() {
    String testName = getTestName(false);
    PsiReference[] references = findElementAtCaret(testName + ".js2");
    assertEquals(2, references.length);
  }

  public void testSuperCall() {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  public void testSuperCall2() {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  public void testBaseMethod() {
    final PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(1, references.length);
  }

  public void testInheritorCall() {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(1, references.length);
  }

  public void testInheritorCall2() {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  public void testReferenceInStringLiteral() {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as", getTestName(false) + "_2.mxml");
    assertEquals(0, references.length);
  }

  public void testASGenericsAndDollarsInName() {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(1, references.length);
  }

  public void testUnrelatedUnqualifiedDefinition() {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as"); // IDEA-189640
    assertEquals(0, references.length);
  }
}
