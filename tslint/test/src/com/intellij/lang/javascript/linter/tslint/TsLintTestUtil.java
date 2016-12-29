package com.intellij.lang.javascript.linter.tslint;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtil;

import java.io.File;


public class TsLintTestUtil {
  public static final String BASE_TEST_DATA_PATH = findTestDataPath();

  private static String findTestDataPath() {
    String homePath = PathManager.getHomePath();
    if (new File(homePath + "/contrib").isDirectory()) {
      // started from IntelliJ IDEA Ultimate project
      return FileUtil.toSystemIndependentName(homePath + "/contrib/tslint/test/data");
    }

    final File f = new File("test/data");
    if (f.isDirectory()) {
      // started from 'Dart-plugin' project
      return FileUtil.toSystemIndependentName(f.getAbsolutePath());
    }

    final String parentPath = PathUtil.getParentPath(homePath);

    if (new File(parentPath + "/intellij-plugins").isDirectory()) {
      // started from IntelliJ IDEA Community Edition
      return FileUtil.toSystemIndependentName(parentPath + "/intellij-plugins/tslint/test/data");
    }

    if (new File(parentPath + "/contrib").isDirectory()) {
      // started from IntelliJ IDEA Community
      return FileUtil.toSystemIndependentName(parentPath + "/contrib/tslint/test/data");
    }

    return "";
  }
}
