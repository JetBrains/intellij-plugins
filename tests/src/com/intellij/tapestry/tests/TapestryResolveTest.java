package com.intellij.tapestry.tests;

import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.psi.TmlFile;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  public void testHtmlTagName() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    XmlTag ref = resolveReferenceAtCaretPosition(XmlTag.class);
    Assert.assertEquals("xs:element", ref.getName());
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

  public void testPageAttrValue() throws Throwable {
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

  @Nullable
  private PsiReference getReferenceAtCaretPosition() {
    return myFixture.getFile().findReferenceAt(myFixture.getEditor().getCaretModel().getOffset());
  }

  @NotNull
  private <T> T resolveReferenceAtCaretPosition(Class<T> aClass) {
    PsiReference ref = getReferenceAtCaretPosition();
    Assert.assertNotNull("No reference at caret", ref);
    final PsiElement element = ref.resolve();
    Assert.assertNotNull("unresolved reference '" + ref.getCanonicalText() + "'", element);
    return assertInstanceOf(element, aClass);
  }

  private void checkReferenceAtCaretPositionUnresolved() {
    PsiReference ref = getReferenceAtCaretPosition();
    Assert.assertNotNull(ref);
    final PsiElement element = ref.resolve();
    Assert.assertNull(String.valueOf(element), element);
  }
}
