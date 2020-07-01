// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.FileBasedTestCaseHelperEx
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.UsefulTestCase
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(com.intellij.testFramework.Parameterized::class)
class VueJSParserTest : LightPlatformCodeInsightTestCase(), FileBasedTestCaseHelperEx {

  override fun getTestDataPath(): String {
    return PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/expr/parser"
  }

  override fun getFileSuffix(fileName: String): String? {
    return if (fileName.endsWith("js")) fileName else null
  }

  @Test
  fun runSingle() {
    doSingleTest(myFileSuffix, myTestDataPath)
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

      val psiFile = PsiFileFactory.getInstance(project)
        .createFileFromText("test.js.$attributeName.$extension", VueJSLanguage.INSTANCE, line)

      result.append(DebugUtil.psiToString(psiFile, false, false))
    }
    UsefulTestCase.assertSameLinesWithFile(File(path, suffix.replace("js", "txt")).toString(), result.toString())
  }

  override fun getRelativeBasePath(): String {
    return ""
  }
}
