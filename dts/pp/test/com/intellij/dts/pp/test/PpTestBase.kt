package com.intellij.dts.pp.test

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.UsefulTestCase
import java.util.Locale

val PP_TEST_ROOT_PATH: String = PathManager.getHomePath() + "/contrib/dts/pp"
val PP_TEST_DATA_PATH: String = PathManager.getHomePath() + "/contrib/dts/pp/testData"

fun UsefulTestCase.getPpTestName(): String {
  return PlatformTestUtil.getTestName(name, false)
    .replace(Regex("[#@_.,-]+"), " ")
    .toPascalCase()
}

private fun String.toPascalCase(): String {
  val capitalize = { word: String -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

  return split(' ').joinToString("", transform = capitalize)
}
