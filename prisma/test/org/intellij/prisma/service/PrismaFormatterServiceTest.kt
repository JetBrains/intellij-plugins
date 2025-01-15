// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.service

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.IncorrectOperationException
import org.intellij.prisma.getPrismaRelativeTestDataPath
import org.intellij.prisma.ide.lsp.PrismaLspServerActivationRule

class PrismaFormatterServiceTest : PrismaServiceTestBase() {
  override fun getBasePath(): String = "${getPrismaRelativeTestDataPath()}/formatter"

  fun testIndents() {
    doFormatterTest()
  }

  fun testLineSpacing() {
    doFormatterTest()
  }

  fun testTrailingNewLine() {
    doFormatterTest()
  }

  fun testEmptyFile() {
    doFormatterTest()
  }

  fun testModelSpacing() {
    doFormatterTest()
  }

  fun testEnumSpacing() {
    doFormatterTest()
  }

  fun testTypeSpacing() {
    doFormatterTest()
  }

  fun testGeneratorSpacing() {
    doFormatterTest()
  }

  fun testDatasourceSpacing() {
    doFormatterTest()
  }

  fun testKeyValueAlignment() {
    doFormatterTest()
  }

  fun testFieldsAlignment() {
    doFormatterTest()
  }

  fun testTypeAliasAlignment() {
    doFormatterTest()
  }

  fun testAlignmentWithDocComments() {
    doFormatterTest()
  }

  fun testEnumAlignment() {
    doFormatterTest()
  }

  fun testSchema() {
    doPrismaFmtFormatterTest()
  }

  fun testDocComment() {
    doFormatterTest()
  }

  private fun doInternalFormatterTest() {
    doFormatterTest(true, false)
  }

  private fun doPrismaFmtFormatterTest() {
    doFormatterTest(false, true)
  }

  private fun doFormatterTest(internal: Boolean = true, prismaFmt: Boolean = true) {
    val testName = getTestName(true)

    if (internal) {
      format(testName, false)
      myFixture.checkResultByFile("${testName}_after.prisma")
    }

    if (prismaFmt) {
      format(testName, true)
      myFixture.checkResultByFile("${testName}_after.prisma")
    }
  }

  private fun format(testName: String, usePrismaFmt: Boolean) {
    myFixture.configureByFile("$testName.prisma")

    val psiFile = myFixture.file
    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)!!

    WriteCommandAction.runWriteCommandAction(project) {
      try {
        PrismaLspServerActivationRule.markForceEnabled(usePrismaFmt)
        CodeStyleManager.getInstance(project)
          .reformatText(psiFile, psiFile.textRange.startOffset, psiFile.textRange.endOffset)
        PsiDocumentManager.getInstance(project).commitDocument(document)
      }
      catch (e: IncorrectOperationException) {
        fail()
      }
    }
  }
}