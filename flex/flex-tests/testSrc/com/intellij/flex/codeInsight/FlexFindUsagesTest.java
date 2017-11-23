package com.intellij.flex.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSAbstractFindUsagesTest;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiReference;

import java.util.Arrays;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexFindUsagesTest extends JSAbstractFindUsagesTest {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected String getBasePath() {
    return "/as_findUsages/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  public void testFindConstructorUsages() throws Exception {
    PsiReference[] references = findElementAtCaret("ConstructorUsages.js2");
    assertEquals("Constructor references", 1, references.length);
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
    assertEquals("Mxml component files references", 4, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindParameterWith$InMxmlComponent() throws Exception {
    PsiReference[] references = findElementAtCaret("20.mxml");
    assertEquals("Parameter with $ in mxml component", 2, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindImplementationMethodWhenSearchingFromInterface() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals("Find implementation method when searching for", 2, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindPrivateVarInMxml() throws Exception {
    PsiReference[] references = findElementAtCaret("21.mxml");
    assertEquals("Mxml component private variable", 3, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindSetterUsagesInMxml() throws Exception {
    String s = getTestName(false);
    PsiReference[] references = findElementAtCaret(s + ".mxml", s + "_2.mxml");
    assertEquals("Setter usages", 6, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindGetterUsagesInMxml() throws Exception {
    String s = getTestName(false);
    PsiReference[] references = findElementAtCaret(s + ".mxml", s + "_2.mxml");
    assertEquals("Getter usages", 3, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testFindPackageRefs() throws Exception {
    String testName = getTestName(false);
    PsiReference[] references = findElementAtCaret(testName + ".mxml", testName + "_2.as");
    assertEquals("Package refs", 5, references.length);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMixJsAndJs2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.JS_1_8_5, getProject(), () -> {
    String testName = getTestName(false);
    PsiReference[] references = findElementAtCaret(testName + ".js2", testName + ".mxml", testName + ".js");
    assertTrue(Arrays.stream(references).noneMatch(r -> r.getElement().getContainingFile().getName().endsWith(".js")));
    assertEquals(5, references.length);
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMixJsAndJs2_2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.JS_1_6, getProject(), () -> {
      String testName = getTestName(false);
      PsiReference[] references = findElementAtCaret(testName + ".js", testName + ".mxml", testName + ".js2");
      assertEquals(3, references.length);
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
    assertEquals(2, references.length);
  }

  public void testSuperCall2() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  public void testBaseMethod() throws Exception {
    final PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(1, references.length);
  }

  public void testInheritorCall() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(1, references.length);
  }

  public void testInheritorCall2() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(2, references.length);
  }

  public void testReferenceInStringLiteral() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as", getTestName(false) + "_2.mxml");
    assertEquals(0, references.length);
  }

  public void testASGenericsAndDollarsInName() throws Exception {
    PsiReference[] references = findElementAtCaret(getTestName(false) + ".as");
    assertEquals(1, references.length);
  }
}
