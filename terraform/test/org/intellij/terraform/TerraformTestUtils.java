package org.intellij.terraform;

import com.intellij.openapi.application.PathManager;

public final class TerraformTestUtils {
  public static String getTestDataPath() {
    return PathManager.getHomePath() + getTestDataRelativePath();
  }

  public static String getTestDataRelativePath() {
    return "/plugins/terraform/test-data";
  }
}
