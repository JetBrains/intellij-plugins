package com.intellij.flex.highlighting;

import com.intellij.flex.util.ActionScriptDaemonAnalyzerTestCase;
import com.intellij.flex.util.FlexTestUtils;

import java.io.IOException;

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
