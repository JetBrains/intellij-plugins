package com.intellij.tapestry.tests;

import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.psi.TapestryAccessorMethod;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.xml.Html5SchemaProvider;
import com.intellij.xml.util.XmlUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jul 16, 2009
 *         Time: 6:11:55 PM
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
    ExternalResourceManagerEx.getInstanceEx().setDefaultHtmlDoctype(myOldDoctype, myFixture.getProject());
    super.tearDown();
  }

  public void testHtmlTagName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    Assert.assertEquals("xs:element", ref.getName());
  }

  public void testHtml5TagName() throws Throwable {
    final ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
    final String doctype = manager.getDefaultHtmlDoctype(myFixture.getProject());
    manager.setDefaultHtmlDoctype(Html5SchemaProvider.HTML5_SCHEMA_LOCATION, myFixture.getProject());
    try {
      addComponentToProject("Count");
      initByComponent();
      LeafElement ref = resolveReferenceAtCaretPosition(LeafElement.class);
      Assert.assertEquals("body.inner", ref.getText());
    }
    finally {
      manager.setDefaultHtmlDoctype(doctype, myFixture.getProject());
    }
  }

  public void testHtmlAttrName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    Assert.assertEquals("xs:attribute", ref.getName());
  }

  public void testLibTmlTagName() throws Throwable {
    initByComponent();
    PsiClass ref = resolveReferenceAtCaretPosition(PsiClass.class);
    Assert.assertEquals("org.apache.tapestry5.corelib.components.Any", ref.getQualifiedName());
  }

  public void testTmlParameter() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    Assert.assertEquals("start", ref.getName());
  }

  public void testTmlTagName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    PsiClass ref = resolveReferenceAtCaretPosition(PsiClass.class);
    Assert.assertEquals(TEST_APPLICATION_PACKAGE + "." + COMPONENTS + ".Count", ref.getQualifiedName());
  }

  public void testTmlTagNameUsingSubpackage() throws Throwable {
    addComponentToProject("other.Count");
    initByComponent();
    PsiClass ref = resolveReferenceAtCaretPosition(PsiClass.class);
    Assert.assertEquals(TEST_APPLICATION_PACKAGE + "." + COMPONENTS + ".other.Count", ref.getQualifiedName());
  }

  public void testTmlAttrName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    Assert.assertEquals("end", ref.getName());
  }

  public void testTmlAttrNameInHtmlTag() throws Throwable {
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    Assert.assertEquals("page", ref.getName());
  }

  public void testTmlAttrNameWithoutPrefixInHtmlTag() throws Throwable {
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    Assert.assertEquals("page", ref.getName());
  }

  public void testTmlAttrNameWithPrefix() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    Assert.assertEquals("end", ref.getName());
  }

  public void testAttrNameWithUnknownPrefix() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testAttrNameWithUnknownPrefixInHtmlTag() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testUnknownAttrName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testHtmlTypeAttrName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    Assert.assertEquals("xs:attribute", ref.getName());
  }

  public void testTypeAttrName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    Assert.assertEquals("button", ref.getName());
  }

  public void testTypeAttrValue() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    PsiClass ref = resolveReferenceAtCaretPosition(PsiClass.class);
    Assert.assertEquals(TEST_APPLICATION_PACKAGE + "." + COMPONENTS + ".Count", ref.getQualifiedName());
  }

  public void testTypeAttrUnknownValue() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testHtmlIdAttrName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    Assert.assertEquals("xs:attribute", ref.getName());
  }

  public void testIdAttrName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    Assert.assertEquals("a", ref.getName());
  }

  public void testIdAttrValue() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    Assert.assertEquals("index55", ref.getName());
  }

  public void testIdAttrValueTypeAttrPresent() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    XmlAttribute ref = resolveReferenceAtCaretPosition(XmlAttribute.class);
    Assert.assertEquals("t:id", ref.getName());
  }

  public void testIdAttrValueInTmlTag() throws Throwable {
    initByComponent();
    XmlAttribute ref = resolveReferenceAtCaretPosition(XmlAttribute.class);
    Assert.assertEquals("t:id", ref.getName());
  }

  public void testIdAttrValueUnresolved() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    checkReferenceAtCaretPositionUnresolved();
  }

  public void testPageAttrValue() throws Throwable {
    addPageToProject("StartPage");
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    Assert.assertEquals("StartPage.tml", ref.getName());
  }

  public void testPageAttrValueOfPagelinkTag() throws Throwable {
    addPageToProject("StartPage");
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    Assert.assertEquals("StartPage.tml", ref.getName());
  }

  public void testPrefixedPageAttrValue() throws Throwable {
    addPageToProject("StartPage");
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    Assert.assertEquals("StartPage.tml", ref.getName());
  }

  public void testPageAttrValueReferencingToSubpackage() throws Throwable {
    addPageToProject("subpack.StartPage");
    initByComponent();
    TmlFile ref = resolveReferenceAtCaretPosition(TmlFile.class);
    Assert.assertEquals("StartPage.tml", ref.getName());
  }

  public void testTapestryAttrValueWithPropPrefix() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    PsiMethod ref = resolveReferenceAtCaretPosition(PsiMethod.class);
    Assert.assertEquals("getHours", ref.getName());
  }

  public void testTapestryAttrValueReferencingToField() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    PsiField ref = resolveReferenceAtCaretPosition(PsiField.class);
    Assert.assertEquals("intFieldProp", ref.getName());
  }

  public void testTelSetterByProperty() throws Throwable {
    initByComponent();
    TapestryAccessorMethod ref = resolveReferenceAtCaretPosition(TapestryAccessorMethod.class);
    Assert.assertEquals("setSomeProp", ref.getName());
    PsiField field = assertInstanceOf(ref.getNavigationElement(), PsiField.class);
    Assert.assertEquals("someProp", field.getName());
  }

  public void testTelPropertyByGetter() throws Throwable {
    initByComponent();
    PsiMethod ref = resolveReferenceAtCaretPosition(PsiMethod.class);
    Assert.assertEquals("getCurrentTime", ref.getName());
  }

  private void checkReferenceAtCaretPositionUnresolved() {
    PsiReference ref = getReferenceAtCaretPosition();
    Assert.assertNotNull(ref);
    final PsiElement element = ref.resolve();
    Assert.assertNull(String.valueOf(element), element);
  }

  @NotNull
  private <T> T resolveReferenceAtCaretPosition(Class<T> aClass) {
    return assertInstanceOf(resolveReferenceAtCaretPosition(), aClass);
  }
}
