package com.intellij.aws.cloudformation.tests

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil

import java.io.File

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
}
