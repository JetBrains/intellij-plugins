// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.expr.parser

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.FileBasedTestCaseHelperEx
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.Parameterized
import com.intellij.testFramework.UsefulTestCase
import org.angular2.lang.expr.Angular2Language
import org.angularjs.AngularTestUtil
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(Parameterized::class)
class Angular2ParserTest : LightPlatformCodeInsightTestCase(), FileBasedTestCaseHelperEx {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(Angular2ParserTest::class.java)
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
