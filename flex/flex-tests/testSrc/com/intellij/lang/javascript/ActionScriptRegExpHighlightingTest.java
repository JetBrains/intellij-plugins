package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;

import java.io.IOException;

/**
 * @author Bas Leijdekkers
 */
public class ActionScriptRegExpHighlightingTest extends ActionScriptDaemonAnalyzerTestCase {

  public void testLookBehind() throws IOException {
    defaultTest();
  }

  public void testNamedGroup() throws IOException {
    defaultTest();
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected String getBasePath() {
    return "as_regexp";
  }

  @Override
  protected String getExtension() {
    return "as";
  }
}
