package com.intellij.lang.javascript.linter.tslint;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public class TsLintTestUtil {
  public static final String BASE_TEST_DATA_PATH = findTestDataPath();

  private static String findTestDataPath() {
    String homePath = PathManager.getHomePath();
    if (new File(homePath + "/contrib").isDirectory()) {
      // started from IntelliJ IDEA Ultimate project
      return FileUtil.toSystemIndependentName(homePath + getTestDataRelativePath());
    }

    return "";
  }

  @NotNull
  public static String getTestDataRelativePath() {
    return "/contrib/tslint/test/data/";
  }
}
