// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import org.intellij.terraform.install.TfToolType
import java.nio.file.Path

internal class ToolPathDetectorMock: ToolPathDetector {

  override suspend fun detectAndVerifyTool(toolType: TfToolType, overrideExistingValue: Boolean): Boolean {
    return true
  }

  override fun isExecutable(path: Path): Boolean {
    return true
  }

  override suspend fun detect(path: String): String? {
    return "terraform"
  }
}