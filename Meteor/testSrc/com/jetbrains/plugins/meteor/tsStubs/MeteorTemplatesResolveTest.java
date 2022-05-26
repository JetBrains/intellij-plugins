package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

public class MeteorTemplatesResolveTest extends CodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return MeteorTestUtil.getBasePath() + "/testResolve/";
  }

  private PsiPolyVariantReference configureByFileText(String text) {
    myFixture.configureByText("testResolve.js", text);
    return (PsiPolyVariantReference)myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
  }

  public void testResolved() {
    myFixture.copyDirectoryToProject("module", "module");
    PsiPolyVariantReference expression = configureByFileText("var a = Template.myTemp<caret>late1;");
    ResolveResult[] results = expression.multiResolve(false);
    assertEquals(1, results.length);
  }

  public void testNotResolved() {
    myFixture.copyDirectoryToProject("module", "module");
    PsiPolyVariantReference expression = configureByFileText("var a = Template.myTemp<caret>late;");
    ResolveResult[] results = expression.multiResolve(false);
    assertEquals(0, results.length);
  }

  @Override
  protected void setUp() throws Exception {
    MeteorTestUtil.enableMeteor();
    super.setUp();
    MeteorProjectTestBase.initMeteorDirs(getProject());
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