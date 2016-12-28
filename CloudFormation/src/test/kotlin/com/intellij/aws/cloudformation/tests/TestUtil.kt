package com.intellij.aws.cloudformation.tests

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.rt.execution.junit.FileComparisonFailure
import junit.framework.TestCase
import java.io.File
import java.io.IOException

object TestUtil {
  fun getTestDataPath(relativePath: String): String {
    return getTestDataFile(relativePath).path + File.separator
  }

  fun getTestDataFile(relativePath: String): File {
    return File(testDataRoot, relativePath)
  }

  private val testDataRoot: File
    get() = File("testData").absoluteFile

  fun getTestDataPathRelativeToIdeaHome(relativePath: String): String {
    val homePath = File(PathManager.getHomePath())
    val testDir = File(testDataRoot, relativePath)

    val relativePathToIdeaHome = FileUtil.getRelativePath(homePath, testDir) ?: throw RuntimeException("getTestDataPathRelativeToIdeaHome: FileUtil.getRelativePath('$homePath', '$testDir') returned null")

    return relativePathToIdeaHome
  }

  fun checkContent(expectFile: File, actualContent: String) {
    val actualNormalized = StringUtil.convertLineSeparators(actualContent)

    if (!expectFile.exists()) {
      expectFile.writeText(actualNormalized)
      TestCase.fail("Wrote ${expectFile.path} with actual content")
    }

    val expectText: String
    try {
      expectText = StringUtil.convertLineSeparators(FileUtil.loadFile(expectFile, CharsetToolkit.UTF8_CHARSET))
    } catch (e: IOException) {
      throw RuntimeException(e)
    }

    if (expectText != actualNormalized) {
      throw FileComparisonFailure("Expected text mismatch", expectText, actualNormalized, expectFile.path)
    }
  }
}
