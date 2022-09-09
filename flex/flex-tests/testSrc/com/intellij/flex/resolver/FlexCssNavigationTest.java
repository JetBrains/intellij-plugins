package com.intellij.flex.resolver;

import com.intellij.codeInsight.JavaCodeInsightTestCase;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestUtils;
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

public class FlexCssNavigationTest extends JavaCodeInsightTestCase {
  private static final @NonNls String BASE_PATH = "/flex_css_navigation/";

  private PsiElement @NotNull [] findTargetElements(String @NotNull ... filenames) {
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
      targets = new ArrayList<>();
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

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
    JSTestUtils.initJSIndexes(getProject());
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssStyleReference1() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssStyleReference2() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    boolean flag = false;
    for (PsiElement element : elements) {
      if (element instanceof CssSelectorSuffix) {
        flag = true;
        break;
      }
    }
    assertTrue(flag);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssStyleReference3() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssPropertyReference() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSAttributeNameValuePair.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssClassPropertyValue() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCssTypeReference() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSClass.class);
    assertEquals(((JSClass)elements[0]).getQualifiedName(), "spark.components.Button");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testCssTypeReference1() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSAttributeNameValuePair.class);
    final JSClass jsClass = PsiTreeUtil.getParentOfType(elements[0], JSClass.class);
    assertNotNull(jsClass);
    assertEquals(jsClass.getQualifiedName(), "spark.components.Button");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testStyleName1() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testStyleName2() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testStyleName3() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testStyleName4() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testStyleName5() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }
}
