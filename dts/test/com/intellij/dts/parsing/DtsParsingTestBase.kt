package com.intellij.dts.parsing

import com.intellij.dts.DTS_TEST_DATA_PATH
import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.lang.parser.DtsParserDefinition
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.parsingFixture
import com.intellij.psi.PsiFile
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.junit5.fixture.projectFixture
import java.nio.charset.StandardCharsets

abstract class DtsParsingTestBase(dataPath: String) {
  companion object {
    private val projectFixture = projectFixture()
  }

  protected val parsing by parsingFixture(
    projectFixture,
    "$DTS_TEST_DATA_PATH/parser/$dataPath",
    "",
    definitions = arrayOf(DtsParserDefinition()),
  )

  protected fun createFile(name: String, text: String): PsiFile {
    val virtualFile = LightVirtualFile(name, DtsLanguage, text)
    virtualFile.charset = StandardCharsets.UTF_8

    SingleRootFileViewProvider.doNotCheckFileSizeLimit(virtualFile)

    return parsing.createFile(virtualFile)
  }

  protected fun parseFile(name: String, text: String): PsiFile {
    return parsing.checkParsedFile(createFile("$name.${parsing.fileExt}", text), text)
  }

  fun doTest(extension: String, ensureNoErrorElements: Boolean) {
    parsing.fileExt = extension

    val name = parsing.testName
    val file = parseFile(name, parsing.loadFile("$name.$extension"))
    parsing.checkResult(name, file)

    if (ensureNoErrorElements) {
      parsing.ensureNoErrorElements()
    }
  }
}