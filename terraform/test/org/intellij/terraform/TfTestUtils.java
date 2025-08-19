// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform;

import com.intellij.openapi.application.PathManager;

public final class TfTestUtils {
  public static String getTestDataPath() {
    return PathManager.getHomeDir() + getTestDataRelativePath();
  }

  public static String getTestDataRelativePath() {
    return "/contrib/terraform/test-data";
  }
}
