package com.intellij.dts.pp.test

import com.intellij.idea.AppMode
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.PlatformTestUtil
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.Path

private const val PP_TEST_DATA_PATH = "contrib/dts/pp/tests/testData"

internal val testDataPath: Path = AppMode.getDevIdeaProjectDir()?.let { Path(it).resolve(PP_TEST_DATA_PATH) }
                                  ?: PathManager.getHomeDir().resolve(PP_TEST_DATA_PATH)

fun getPpTestName(methodName: String): String {
  return PlatformTestUtil.getTestName(methodName, false)
    .replace(Regex("[#@_.,-]+"), " ")
    .toPascalCase()
}

private fun String.toPascalCase(): String {
  val capitalize = { word: String -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

  return split(' ').joinToString("", transform = capitalize)
}
