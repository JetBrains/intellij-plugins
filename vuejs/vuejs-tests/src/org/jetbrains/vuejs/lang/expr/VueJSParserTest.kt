// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

/**
 * Runs one test for each `.js` file of the test data. Each line of the file is one expression.
 */
@TestApplication
class VueJSParserTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("testFiles")
  fun runSingle(suffix: String) {
    doSingleTest(suffix, testDataPath)
  }

  private fun doSingleTest(suffix: String, path: String) {
    val text = FileUtil.loadFile(File(path, suffix), true)
    val result = StringBuilder()

    val firstDot = suffix.indexOf('.')
    val attributeName = suffix.substring(0, firstDot)
      .replace('=', ':')
      .replace(' ', '.')
    val secondDot = suffix.indexOf('.', firstDot + 1)
    val extension = if (secondDot > 0) suffix.substring(firstDot + 1, secondDot) else ""

    for (line in StringUtil.splitByLines(text, false)) {
      if (result.isNotEmpty()) result.append("------\n")

      result.append(runReadActionBlocking {
        val psiFile = PsiFileFactory.getInstance(projectFixture.get())
          .createFileFromText("test.js.$attributeName.$extension", VueJSLanguage, line)
        DebugUtil.psiToString(psiFile, true, false)
      })
    }
    PlatformTestUtil.assertSameLinesWithFile(File(path, suffix.replace("js", "txt")).toString(), result.toString())
  }

  companion object {
    private val projectFixture = projectFixture()

    private val testDataPath: String = getVueTestDataPath() + "/expr/parser"

    @JvmStatic
    fun testFiles(): List<String> =
      File(testDataPath).list()!!.filter { it.endsWith("js") }.sorted()
  }
}
