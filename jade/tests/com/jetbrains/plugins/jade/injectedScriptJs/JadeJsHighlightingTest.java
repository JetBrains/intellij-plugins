package com.jetbrains.plugins.jade.injectedScriptJs;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.plugins.jade.JadeHighlightingTest;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class JadeJsHighlightingTest extends BasePlatformTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return JadeHighlightingTest.TEST_DATA_PATH + "/injectedScriptJs/";
  }

  public void testHighlightingVar() {
    myFixture.testHighlighting(getTestName(true) + "." + "jade");
  }

  public void testArrowFunctionExpression() {
    myFixture.testHighlighting(getTestName(true) + "." + "jade");
  }
}
