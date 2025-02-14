// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

internal class ToolPathDetectorMock: ToolPathDetector {

  override suspend fun detect(path: String): String? {
    return "terraform"
  }
}