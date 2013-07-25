package com.intellij.coldFusion;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

/**
 * Created by fedorkorotkov.
 */
abstract public class CfmlCodeInsightFixtureTestCase extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return CfmlTestUtil.BASE_TEST_DATA_PATH + getBasePath();
  }
}
