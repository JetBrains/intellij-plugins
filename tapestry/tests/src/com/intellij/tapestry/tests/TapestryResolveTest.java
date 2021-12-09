package com.intellij.tapestry.tests;

import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.psi.*;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.MappingDataCache;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.psi.TapestryAccessorMethod;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.xml.Html5SchemaProvider;
import com.intellij.xml.util.XmlUtil;
import org.intellij.plugins.relaxNG.compact.RncElementTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * @author Alexey Chmutov
 */
public class TapestryResolveTest extends TapestryBaseTestCase {
  @Override
  protected String getBasePath() {
    return "resolve/";
  }

  private String myOldDoctype;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
    myOldDoctype = manager.getDefaultHtmlDoctype(myFixture.getProject());
    manager.setDefaultHtmlDoctype(XmlUtil.XHTML_URI, myFixture.getProject());
  }

  @Override
  public void tearDown() throws Exception {
    try {
      ExternalResourceManagerEx.getInstanceEx().setDefaultHtmlDoctype(myOldDoctype, myFixture.getProject());
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  public void testHtmlTagName() {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    assertEquals("xs:element", ref.getName());
  }

  public void testHtml5TagName() {
    final ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
    final String doctype = manager.getDefaultHtmlDoctype(myFixture.getProject());
    manager.setDefaultHtmlDoctype(Html5SchemaProvider.getHtml5SchemaLocation(), myFixture.getProject());
    try {
      addComponentToProject("Count");
      initByComponent();
      PsiElement ref = resolveReferenceAtCaretPosition(PsiElement.class).getNavigationElement();
      assertEquals(RncElementTypes.NAME_CLASS, ref.getNode().getElementType());
      assertEquals("body", ref.getText());
    }
    finally {
      manager.setDefaultHtmlDoctype(doctype, myFixture.getProject());
    }
  }

  public void testHtmlAttrName() {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    assertEquals("xs:attribute", ref.getName());
  }

  public void testLibTmlTagName() {
    initByComponent();
    PsiClass ref = resolveReferenceAtCaretPosition(PsiClass.class);
    assertEquals("org.apache.tapestry5.corelib.components.Any", ref.getQualifiedName());
  }

  public void testTmlMapping() {
    final PsiFile psiFile = myFixture.configureByFile("TmlMapping.java");
    final Map<String,String> compute = MappingDataCache.getMappingData(psiFile);
    assertTrue(compute.containsKey("foo"));
  }

  public void testTmlMapping2() {
    final PsiFile psiFile = myFixture.configureByFile("TmlMapping2.java");
    final Map<String,String> compute = MappingDataCache.getMappingData(psiFile);
    assertTrue(compute.containsKey("foo"));
  }

  public void testTmlMapping3() {
    myFixture.configureByFile("TmlMapping3.java");
    final TapestryModuleSupportLoader moduleSupportLoader = TapestryModuleSupportLoader.getInstance(myModule);
    final Collection<TapestryLibrary> libraries = moduleSupportLoader.getTapestryProject().getLibraries();
    TapestryLibrary libraryOfInterest = null;

    for(TapestryLibrary library:libraries) {
      if ("dk.nesluop.librarymapping.framework".equals(library.getBasePackage())) {
        libraryOfInterest = library;
        break;
      }
    }
    assertNotNull(libraryOfInterest);
  }

  public void testTmlMixin() {
    addComponentToProject("Count");
    addMixinToProject("FooMixin");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    assertEquals("bar", ref.getName());
  }

  public void testTmlParameter() {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    assertEquals("start", ref.getName());
  }

  public void testTmlTagName() {
    addComponentToProject("Count");
    initByComponent();
    PsiClass ref = resolveReferenceAtCaretPosition(PsiClass.class);
    assertEquals(TEST_APPLICATION_PACKAGE + "." + COMPONENTS + ".Count", ref.getQualifiedName());
  }

  public void testTmlTagNameUsingSubpackage() {
    addComponentToProject("other.Count");
    initByComponent();
    PsiClass ref = resolveReferenceAtCaretPosition(PsiClass.class);
    assertEquals(TEST_APPLICATION_PACKAGE + "." + COMPONENTS + ".other.Count", ref.getQualifiedName());
  }

  public void testTmlAttrName() {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    assertEquals("end", ref.getName());
  }

  public void testTmlAttrNameInHtmlTag() {
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    assertEquals("page", ref.getName());
  }

  public void testTmlAttrNameWithoutPrefixInHtmlTag() {
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    assertEquals("page", ref.getName());
  }

  public void testTmlAttrNameWithPrefix() {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    assertEquals("end", ref.getName());
  }

  public void testAttrNameWithUnknownPrefix() {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testAttrNameWithUnknownPrefixInHtmlTag() {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testUnknownAttrName() {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testHtmlTypeAttrName() {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    assertEquals("xs:attribute", ref.getName());
  }

  public void testTypeAttrName() {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    assertEquals("button", ref.getName());
  }

  public void testTypeAttrValue() {
    addComponentToProject("Count");
    initByComponent();
    PsiClass ref = resolveReferenceAtCaretPosition(PsiClass.class);
    assertEquals(TEST_APPLICATION_PACKAGE + "." + COMPONENTS + ".Count", ref.getQualifiedName());
  }

  public void testTypeAttrUnknownValue() {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testHtmlIdAttrName() {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    assertEquals("xs:attribute", ref.getName());
  }

  public void testIdAttrName() {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    assertEquals("a", ref.getName());
  }

  public void testIdAttrValue() {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    assertEquals("index55", ref.getName());
  }

  public void testIdAttrValueTypeAttrPresent() {
    addComponentToProject("Count");
    initByComponent();
    XmlAttributeValue ref = resolveReferenceAtCaretPosition(XmlAttributeValue.class);
    assertEquals("t:id", ((XmlAttribute)ref.getParent()).getName());
  }

  public void testIdAttrValueInTmlTag() {
    initByComponent();
    XmlAttributeValue ref = resolveReferenceAtCaretPosition(XmlAttributeValue.class);
    assertEquals("t:id", ((XmlAttribute)ref.getParent()).getName());
  }

  public void testIdAttrValueUnresolved() {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testPageAttrValue() {
    addPageToProject("StartPage");
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    assertEquals("StartPage.tml", ref.getName());
  }

  public void testPageAttrValue2() {
    addElementToProject(PAGES_PACKAGE_PATH, "StartPage2", Util.DOT_GROOVY);
    addElementToProject(PAGES_PACKAGE_PATH, "StartPage2", getTemplateExtension());
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    assertEquals("StartPage2.tml", ref.getName());
  }

  public void testPageAttrValueOfPagelinkTag() {
    addPageToProject("StartPage");
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    assertEquals("StartPage.tml", ref.getName());
  }

  public void testPrefixedPageAttrValue() {
    addPageToProject("StartPage");
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    assertEquals("StartPage.tml", ref.getName());
  }

  public void testPageAttrValueReferencingToSubpackage() {
    addPageToProject("subpack.StartPage");
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    assertEquals("StartPage.tml", ref.getName());
  }

  public void testTapestryAttrValueWithPropPrefix() {
    addComponentToProject("Count");
    initByComponent();
    PsiMethod ref = resolveReferenceAtCaretPosition(PsiMethod.class);
    assertEquals("getHours", ref.getName());
  }

  public void testTapestryAttrValueReferencingToField() {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    assertEquals("intFieldProp", ref.getName());
  }

  public void testTelSetterByProperty() {
    initByComponent();
    TapestryAccessorMethod ref = resolveReferenceAtCaretPosition(TapestryAccessorMethod.class);
    assertEquals("setSomeProp", ref.getName());
    PsiField field = assertInstanceOf(ref.getNavigationElement(), PsiField.class);
    assertEquals("someProp", field.getName());
  }

  public void testTelPropertyByGetter() {
    initByComponent();
    PsiMethod ref = resolveReferenceAtCaretPosition(PsiMethod.class);
    assertEquals("getCurrentTime", ref.getName());
  }

  public void testCssClass() {
    myFixture.copyFileToProject("CssClass.css", COMPONENTS_PACKAGE_PATH + "CssClass.css");
    initByComponent();
    CssClass cssClass = resolveReferenceAtCaretPosition(CssClass.class);
    assertEquals("cssClassTapestry", cssClass.getName());
    assertEquals("CssClass.css", cssClass.getContainingFile().getName());
  }

  private void checkReferenceAtCaretPositionUnresolved() {
    PsiReference ref = getReferenceAtCaretPosition();
    assertNotNull(ref);
    final PsiElement element = ref.resolve();
    assertNull(String.valueOf(element), element);
  }

  @NotNull
  private <T> T resolveReferenceAtCaretPosition(Class<T> aClass) {
    return assertInstanceOf(resolveReferenceAtCaretPosition(), aClass);
  }
}
