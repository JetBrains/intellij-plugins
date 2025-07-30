// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.FileBasedTestCaseHelperEx
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.Parameterized
import org.angular2.Angular2TestUtil
import org.angular2.lang.html.Angular2TemplateSyntax
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(Parameterized::class)
open class Angular2ParserTest : LightPlatformCodeInsightTestCase(), FileBasedTestCaseHelperEx {

  protected open val templateSyntax: Angular2TemplateSyntax get() = Angular2TemplateSyntax.V_2

  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "expr/parser"
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
        .createFileFromText("test.$name.$extension", templateSyntax.expressionLanguage, line)
      result.append(DebugUtil.psiToString(psiFile, true, false))
    }
    assertSameLinesWithFile(findTestDataFile(path, suffix.replace("js", "txt")),
                            result.toString())
  }

  override fun getRelativeBasePath(): String {
    return ""
  }


  private fun findTestDataFile(basePath: String, fileName: String): String {
    // Iterate over syntax versions starting from the `templateSyntax` down to V_2
    return Angular2TemplateSyntax.entries.toList().asReversed().asSequence()
             .dropWhile { it != templateSyntax }
             .filter { it != Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS }
             .firstNotNullOfOrNull { syntax ->
               "${basePath}${syntax.dirSuffix}/$fileName".takeIf { File(it).exists() }
             }
           ?: "${basePath}${templateSyntax.dirSuffix}/$fileName"
  }

  private val Angular2TemplateSyntax.dirSuffix: String get() = if (this == Angular2TemplateSyntax.V_2) "" else "_$this"


}
