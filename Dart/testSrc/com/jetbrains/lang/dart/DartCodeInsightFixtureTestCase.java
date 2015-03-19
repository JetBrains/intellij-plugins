package com.jetbrains.lang.dart;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

abstract public class DartCodeInsightFixtureTestCase extends LightPlatformCodeInsightFixtureTestCase {
  protected void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable());
  }

  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  public void addStandardPackage(@NotNull final String packageName) {
    myFixture.copyDirectoryToProject("../packages/" + packageName, "packages/" + packageName);
  }
}
