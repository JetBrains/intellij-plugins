package com.jetbrains.lang.dart.resolve;

import com.jetbrains.lang.dart.util.DartHtmlUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartGotoImplementationInHtmlTest extends DartGotoImplementationTest {
  @Override
  protected void doTest(int expectedLength) throws Throwable {
    DartHtmlUtil.createHtmlAndConfigureFixture(myFixture, getTestName(false));
    doTestInner(expectedLength);
  }
}
