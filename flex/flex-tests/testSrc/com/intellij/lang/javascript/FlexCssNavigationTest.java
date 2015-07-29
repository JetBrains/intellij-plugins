package com.intellij.lang.javascript;

import com.intellij.codeInsight.CodeInsightTestCase;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.ModuleType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.css.CssSelectorSuffix;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexCssNavigationTest extends CodeInsightTestCase {
  private static final @NonNls String BASE_PATH = "/flex_css_navigation/";

  @NotNull
  private PsiElement[] findTargetElements(@NotNull String... filenames) throws Exception {
    String[] fileNamesWithBasePath = new String[filenames.length];
    for (int i = 0, filenamesLength = filenames.length; i < filenamesLength; i++) {
      fileNamesWithBasePath[i] = BASE_PATH + filenames[i];
    }
    configureByFiles(null, fileNamesWithBasePath);
    Collection<PsiElement> targets;
    PsiReference reference = TargetElementUtil.findReference(myEditor);
    if (reference == null) {
      reference = JSTestUtils.findReferenceFromInjected(myEditor, myFile);
    }
    assertNotNull(reference);
    if (reference instanceof PsiMultiReference) {
      targets = new ArrayList<PsiElement>();
      for (PsiReference ref : ((PsiMultiReference)reference).getReferences()) {
        targets.addAll(TargetElementUtil.getInstance().getTargetCandidates(ref));
      }
    }
    else {
      targets = TargetElementUtil.getInstance().getTargetCandidates(reference);
    }
    assertTrue("Target elements not found", targets.size() > 0);
    return PsiUtilCore.toPsiElementArray(targets);
  }

  @Override
  protected String getTestDataPath() {
    return JSTestUtils.getTestDataPath();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    JSTestUtils.initJSIndexes(getProject());
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssStyleReference1() throws Exception {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssStyleReference2() throws Exception {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    boolean flag = false;
    for (PsiElement element : elements) {
      if (element instanceof CssSelectorSuffix) {
        flag = true;
      }                                   }
    assertTrue(flag);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssStyleReference3() throws Exception {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssPropertyReference() throws Exception {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSAttributeNameValuePair.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssClassPropertyValue() throws Exception {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCssTypeReference() throws Exception {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSClass.class);
    assertEquals(((JSClass)elements[0]).getQualifiedName(), "spark.components.Button");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCssTypeReference1() throws Exception {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSAttributeNameValuePair.class);
    final JSClass jsClass = PsiTreeUtil.getParentOfType(elements[0], JSClass.class);
    assertNotNull(jsClass);
    assertEquals(jsClass.getQualifiedName(), "spark.components.Button");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName1() throws Exception {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName2() throws Exception {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName3() throws Exception {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName4() throws Exception {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName5() throws Exception {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }
}
