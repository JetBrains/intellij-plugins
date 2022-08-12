package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.openapi.application.PathManager

object NextJsTestUtil {
  fun getTestDataPath(): String {
    return PathManager.getHomePath() + "/contrib/nextjs/testData/"
  }
}