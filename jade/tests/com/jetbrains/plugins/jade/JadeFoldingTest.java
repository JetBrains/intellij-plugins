package com.jetbrains.plugins.jade;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class JadeFoldingTest extends BasePlatformTestCase {

  public static final String RELATIVE_TEST_DATA_PATH = "/plugins/Jade/testData";
  public static final String TEST_DATA_PATH = PathManager.getHomePath() + RELATIVE_TEST_DATA_PATH;

  public void testFolding() {
    defaultTest();
  }

  public void testWeb17111() {
    defaultTest();
  }

  public void defaultTest() {
    myFixture.testFolding(getTestDataPath() + getTestName(true) + ".jade");
  }

  @Override
  protected String getTestDataPath() {
    return TEST_DATA_PATH + "/folding/";
  }
}
