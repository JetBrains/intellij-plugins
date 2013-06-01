package com.jetbrains.lang.dart.util;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;

public class DartSdkTestUtil {
  public static void configFakeSdk(CodeInsightTestFixture fixture) {
    String sdkHome = PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/sdk/");

    final File targetFile = new File(fixture.getTempDirPath() + "/dart-sdk");
    try {
      FileUtil.copyDir(new File(sdkHome), targetFile);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetFile);
    Assert.assertNotNull(file);
    file.refresh(false, true);

    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(DartSettingsUtil.DART_SDK_PATH, file.getPath());
  }
}
