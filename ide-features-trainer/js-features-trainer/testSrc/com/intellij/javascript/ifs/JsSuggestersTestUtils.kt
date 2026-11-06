package com.intellij.javascript.ifs

import com.intellij.openapi.application.PathManager
import java.io.File

object JsSuggestersTestUtils {
  val testDataPath: String
    get() = PathManager.getHomePath().replace(File.separatorChar, '/') + "/contrib/ide-features-trainer/js-features-trainer/testData"
}