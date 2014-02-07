package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.JstdTestRoot;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;

import java.io.IOException;
import java.util.Collection;

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

  @Override
  protected Collection<HighlightInfo> defaultTest() throws IOException {
    return doTestFor(true, getTestName(true)+".js");
  }

  @Override
  protected String getTestDataPath() {
    return JstdTestRoot.getTestDataDir().getAbsolutePath();
  }

  public void testJstdTestCaseQuickFixAvailable() throws Exception {
    JstdAssertionFrameworkSupportInspection inspection = new JstdAssertionFrameworkSupportInspection();
    enableInspectionTool(inspection);
    defaultTest();
  }
}
