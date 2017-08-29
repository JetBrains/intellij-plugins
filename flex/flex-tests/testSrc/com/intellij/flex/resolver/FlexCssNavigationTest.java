package com.intellij.flex.resolver;

import com.intellij.codeInsight.CodeInsightTestCase;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
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

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexCssNavigationTest extends CodeInsightTestCase {
  private static final @NonNls String BASE_PATH = "/flex_css_navigation/";

  @NotNull
  private PsiElement[] findTargetElements(@NotNull String... filenames) {
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

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    JSTestUtils.initJSIndexes(getProject());
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssStyleReference1() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssStyleReference2() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    boolean flag = false;
    for (PsiElement element : elements) {
      if (element instanceof CssSelectorSuffix) {
        flag = true;
      }
    }
    assertTrue(flag);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssStyleReference3() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssPropertyReference() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSAttributeNameValuePair.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssClassPropertyValue() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssSelectorSuffix.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCssTypeReference() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSClass.class);
    assertEquals(((JSClass)elements[0]).getQualifiedName(), "spark.components.Button");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCssTypeReference1() {
    PsiElement[] elements = findTargetElements(getTestName(false) + ".css");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], JSAttributeNameValuePair.class);
    final JSClass jsClass = PsiTreeUtil.getParentOfType(elements[0], JSClass.class);
    assertNotNull(jsClass);
    assertEquals(jsClass.getQualifiedName(), "spark.components.Button");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName1() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName2() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName3() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName4() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet})
  public void testStyleName5() {
    final PsiElement[] elements = findTargetElements(getTestName(false) + ".mxml");
    assertEquals(1, elements.length);
    assertInstanceOf(elements[0], CssClass.class);
  }
}
