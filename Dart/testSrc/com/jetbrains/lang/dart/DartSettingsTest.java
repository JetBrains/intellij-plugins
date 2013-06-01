package com.jetbrains.lang.dart;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.settings.DartSettings;

import java.util.Map;

public class DartSettingsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/settings/");
  }

  public void testLibraries() throws Throwable {
    Map<String, String> libs = DartSettings.computeData(myFixture.configureByFile("libraries.dart"));
    assertEquals("html/dartium/html_dartium.dart", libs.get("html"));
  }
}
