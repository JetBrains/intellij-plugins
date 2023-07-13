// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.FileBasedTestCaseHelperEx
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.Parameterized
import com.intellij.testFramework.UsefulTestCase
import org.angularjs.AngularTestUtil
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(Parameterized::class)
class Angular2ParserTest : LightPlatformCodeInsightTestCase(), FileBasedTestCaseHelperEx {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "expr/parser"
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
    val extension = suffix.substring(0, firstDot)
    val secondDot = suffix.indexOf('.', firstDot + 1)
    val name = if (secondDot > 0) suffix.substring(firstDot + 1, secondDot) else ""
    for (line in StringUtil.splitByLines(text)) {
      if (!result.isEmpty()) result.append("------\n")
      val psiFile = PsiFileFactory.getInstance(project)
        .createFileFromText("test.$name.$extension", Angular2Language.INSTANCE, line)
      result.append(DebugUtil.psiToString(psiFile, true, false))
    }
    UsefulTestCase.assertSameLinesWithFile(File(path, suffix.replace("js", "txt")).toString(), result.toString())
  }

  override fun getRelativeBasePath(): String {
    return ""
  }
}
