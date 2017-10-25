package com.intellij.lang.javascript.linter.tslint;

import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class TsLintResolveTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return TsLintTestUtil.BASE_TEST_DATA_PATH + "/config/resolve/";
  }
  
  public void testExtendsArray() {
    final PsiReference ref = myFixture.getReferenceAtCaretPosition(getTestName(true) + "/tslint.json");
    assertNotNull(ref);
  }
  
  public void testExtendsProperty() {
    final PsiReference ref = myFixture.getReferenceAtCaretPosition(getTestName(true) + "/tslint.json");
    assertNotNull(ref);
  }
}
