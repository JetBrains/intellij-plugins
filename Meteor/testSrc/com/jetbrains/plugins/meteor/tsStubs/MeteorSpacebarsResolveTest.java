package com.jetbrains.plugins.meteor.tsStubs;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

public class MeteorSpacebarsResolveTest extends CodeInsightFixtureTestCase {
  
  
  @Override
  protected String getBasePath() {
    return MeteorTestUtil.getBasePath() + "/testResolveHelpers/";
  }

  public void testSingleTag() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("Template.myTemplateForHelper.testProperty", ((JSQualifiedNamedElement)resolve).getQualifiedName());
  }

  public void testSingleUnescapedTag() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("Template.myTemplateForHelper.testProperty", ((JSQualifiedNamedElement)resolve).getQualifiedName());
  }

  public void testThreeTags() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("Template.myTemplateForHelper2.testProperty", ((JSQualifiedNamedElement)resolve).getQualifiedName());
  }

  public void testNotResolve() {
    PsiElement resolve = resolveElement();
    assertNull(resolve);
  }

  public void testResolvedObjectLiteralHelper() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    String qualifiedName = ((JSQualifiedNamedElement)resolve).getQualifiedName();
    assertEquals("MeteorTemplateHelpersNamespace.myTemplateForLiteral." + "testPropertyLiteral", qualifiedName);
  }

  public void testResolveGlobalHelper() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("MeteorGlobalHelpersNamespace.globalHelper1", ((JSPsiElementBase)resolve).getQualifiedName());
  }

  public void testResolveTemplate() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("\"templateForResolving\"", resolve.getText());
  }

  public void testResolveForBodyTag() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("MeteorTemplateHelpersNamespace.body.testPropertyForBody", ((JSPsiElementBase)resolve).getQualifiedName());
  }

  public void testResolveBlockTag() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("MeteorGlobalHelpersNamespace.globalHelperForBlock", ((JSPsiElementBase)resolve).getQualifiedName());
  }

  private PsiElement resolveElement() {
    VirtualFile directory = myFixture.copyDirectoryToProject(getTestName(true) + "/module", "module");
    MeteorProjectTestBase.initMeteorDirs(getProject());
    VirtualFile templates = directory.findFileByRelativePath("templates.html");
    assertNotNull(templates);
    myFixture.configureFromExistingVirtualFile(templates);
    int offset = myFixture.getCaretOffset();
    PsiReference reference = myFixture.getFile().findReferenceAt(offset);
    return reference.resolve();
  }

  @Override
  protected void setUp() throws Exception {
    MeteorTestUtil.enableMeteor();
    super.setUp();
    HbTestUtils.setOpenHtmlAsHandlebars(true, getProject(), myFixture.getTestRootDisposable());
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      MeteorTestUtil.disableMeteor();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }
}
