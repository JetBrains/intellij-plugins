package com.jetbrains.lang.dart;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;

/**
 * Created by fedorkorotkov.
 */
abstract public class DartCodeInsightFixtureTestCase extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }
}
