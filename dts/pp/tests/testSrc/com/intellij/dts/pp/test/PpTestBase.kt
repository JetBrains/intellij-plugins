package com.intellij.dts.pp.test

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.PlatformTestUtil
import java.nio.file.Path
import java.util.Locale

val PP_TEST_DATA_PATH: Path = PathManager.getHomeDir().resolve("contrib/dts/pp/tests/testData")

fun getPpTestName(methodName: String): String {
  return PlatformTestUtil.getTestName(methodName, false)
    .replace(Regex("[#@_.,-]+"), " ")
    .toPascalCase()
}

private fun String.toPascalCase(): String {
  val capitalize = { word: String -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

  return split(' ').joinToString("", transform = capitalize)
}
