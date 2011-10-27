package com.intellij.flex.uiDesigner;

import com.intellij.codeInsight.CodeInsightTestCase;
import com.intellij.lang.javascript.JSTestUtils;

public abstract class FlexUIDesignerBaseTestCase extends CodeInsightTestCase {
  private static String testDataPath;

  @Override
  protected boolean isRunInWriteAction() {
    return false;
  }

  public static String testDataPath() {
    if (testDataPath == null) {
      testDataPath = DebugPathManager.getFudHome() + "/idea-plugin/testData";
    }
    return testDataPath;
  }

  protected static String getFudHome() {
    return DebugPathManager.getFudHome();
  }

  @Override
  protected String getTestDataPath() {
    return testDataPath();
  }

  @Override
  protected void setUpJdk() {
    JSTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(),
                             DebugPathManager.getIdeaHome() + "/plugins/JavaScriptLanguage/testData/flex_highlighting/MockGumboSdk", false);
  }
}