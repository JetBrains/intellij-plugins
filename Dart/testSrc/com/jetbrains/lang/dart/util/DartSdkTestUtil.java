package com.jetbrains.lang.dart.util;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;

public class DartSdkTestUtil {
  public static void configFakeSdk(CodeInsightTestFixture fixture) {
    String sdkHome = FileUtil.toSystemDependentName("../sdk/");
    configFakeSdk(fixture, sdkHome);
  }

  public static void configFakeSdk(CodeInsightTestFixture fixture, String sdkHome) {
    VirtualFile file = fixture.copyDirectoryToProject(sdkHome, "sdk");
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(DartSettingsUtil.DART_SDK_PATH_PROPERTY_NAME, file.getUrl());
  }
}
