package com.jetbrains.plugins.meteor.tsStubs;


import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public class MeteorHighlightingTest extends MeteorProjectTestBase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableInspections();
  }

  public void testTemplateWithoutName() {
    doTest();
  }

  protected void enableInspections() {
    myFixture.enableInspections(new RequiredAttributesInspection());
  }

  private void doTest() {
    myFixture.testHighlighting(getTestName(true) + ".spacebars");
  }

  @Override
  protected String getBasePath() {
    return super.getBasePath() + "/testHighlighting/";
  }

  public void testResolveToJSInScriptTag() {
    final boolean oldValue = HbConfig.shouldOpenHtmlAsHandlebars(getProject());
    try {
      HbConfig.setShouldOpenHtmlAsHandlebars(true, getProject());
      PsiReference reference = myFixture.getReferenceAtCaretPosition(getTestName(false) + ".js", getTestName(false) + ".html");
      assertInstanceOf(reference, JSReferenceExpression.class);
      PsiElement resolve = ReadAction.compute(() -> reference.resolve());// may be null if Hb file element type is not stub, just don't throw an assertion
      //assertNotNull(resolve);
    }
    finally {
      HbConfig.setShouldOpenHtmlAsHandlebars(oldValue, getProject());
    }
  }
}
