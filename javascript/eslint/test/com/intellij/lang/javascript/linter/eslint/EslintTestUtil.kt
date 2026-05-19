@file:JvmName("EslintTestUtil")

package com.intellij.lang.javascript.linter.eslint

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import java.io.File

private const val ESLINT_TEST_DATA_PATH = "/javascript/eslint/testData"

@JvmField
val ESLINT_TEST_DATA_RELATIVE_PATH: String = "/contrib$ESLINT_TEST_DATA_PATH"

fun getEslintTestDataPath(): String =
  getContribPath() + ESLINT_TEST_DATA_PATH

fun getEslintTestDataRelativePath(): String = ESLINT_TEST_DATA_RELATIVE_PATH

private fun getContribPath(): String {
  val homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy()
  return if (File(homePath, "contrib/.gitignore").isFile) {
    homePath + File.separatorChar + "contrib"
  }
  else homePath
}
