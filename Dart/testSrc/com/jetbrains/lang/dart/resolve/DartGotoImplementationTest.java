package com.jetbrains.lang.dart.resolve;

import com.intellij.codeInsight.navigation.GotoImplementationHandler;
import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

/**
 * @author: Fedor.Korotkov
 */
public class DartGotoImplementationTest extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/gotoImplementation/");
  }

  protected void doTest(int expectedLength) throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".dart");
    doTestInner(expectedLength);
  }

  protected void doTestInner(int expectedLength) {
    final GotoTargetHandler.GotoData data =
      new GotoImplementationHandler().getSourceAndTargetElements(myFixture.getEditor(), myFixture.getFile());
    assertNotNull(myFixture.getFile().toString(), data);
    assertEquals(expectedLength, data.targets.length);
  }

  public void testGti1() throws Throwable {
    doTest(2);
  }

  public void testGti2() throws Throwable {
    doTest(1);
  }

  public void testGti3() throws Throwable {
    doTest(2);
  }

  public void testGti4() throws Throwable {
    doTest(1);
  }

  public void testMixin1() throws Throwable {
    doTest(1);
  }
}
