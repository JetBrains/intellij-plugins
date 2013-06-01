package com.jetbrains.lang.dart;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.util.DartTestUtils;

import java.util.Map;

public class DartSettingsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + "/settings/";
  }

  public void testLibraries() throws Throwable {
    Map<String, String> libs = DartSettings.computeData(myFixture.configureByFile("libraries.dart"));
    assertEquals("html/dartium/html_dartium.dart", libs.get("html"));
  }
}
