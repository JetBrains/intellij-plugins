package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;

import java.io.IOException;

public class JstdHighlightingTest extends JSDaemonAnalyzerTestCase {
  @Override
  protected String getBasePath() {
    return "/assertFramework/jstd/highlighting";
  }

  @Override
  protected String getExtension() {
    return "js";
  }

  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    return new LocalInspectionTool[] {};
  }

  private void defaultTest() throws IOException {
    doTestFor(true, getTestName(true)+".js");
  }

  @Override
  protected String getTestDataPath() {
    return JsTestDriverTestUtils.getTestDataDir().getAbsolutePath();
  }

  public void testJstdTestCaseQuickFixAvailable() throws Exception {
    JstdAssertionFrameworkSupportInspection inspection = new JstdAssertionFrameworkSupportInspection();
    enableInspectionTool(inspection);
    defaultTest();
  }
}
