package org.jetbrains.vuejs.lang

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import java.io.File

private const val VUE_TEST_DATA_PATH = "/vuejs/vuejs-tests/testData"

fun getVueTestDataPath(): String =
  getContribPath() + VUE_TEST_DATA_PATH

fun vueRelativeTestDataPath(): String = "/contrib$VUE_TEST_DATA_PATH"

private fun getContribPath(): String {
  val homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy()
  return if (File(homePath, "contrib/.gitignore").isFile) {
    homePath + File.separatorChar + "contrib"
  }
  else homePath
}