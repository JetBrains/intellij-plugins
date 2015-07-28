package com.intellij.flex;

import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.NotNull;

public class FlexTestUtils {

  @NotNull
  public static String getTestDataPath(@NotNull final String relativePath) {
    return PathManager.getHomePath() + "/flex/flex-tests/testData/" + relativePath;
  }
}
