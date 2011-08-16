package com.intellij.flex.uiDesigner;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.JSTestUtils;

abstract class FlexUIDesignerBaseTestCase extends JSDaemonAnalyzerTestCase {
  private static String testDataPath;

  public static String getTestDataPathImpl() {
    if (testDataPath == null) {
      testDataPath = DebugPathManager.getFudHome() + "/idea-plugin/testData";
    }
    return testDataPath;
  }

  protected static String getFudHome() {
    return DebugPathManager.getFudHome();
  }

  @Override
  protected String getExtension() {
    return "mxml";
  }

  @Override
  protected String getTestDataPath() {
    return getTestDataPathImpl();
  }

  @Override
  protected void setUpJdk() {
    JSTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(),
        DebugPathManager.getIdeaHome() + "/plugins/JavaScriptLanguage/testData/flex_highlighting/MockGumboSdk", false);
  }

  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    return new LocalInspectionTool[]{};
  }
}
