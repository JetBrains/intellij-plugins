package org.intellij.prisma

import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.IncorrectOperationException
import org.intellij.prisma.ide.formatter.PrismaFormattingService

class PrismaFormatterTest : JSTempDirWithNodeInterpreterTest() {
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
    doInternalFormatterTest()
  }

  fun testEmptyFilePrismaFmt() {
    doPrismaFmtFormatterTest()
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

  private fun doInternalFormatterTest() {
    doFormatterTest(true, false)
  }

  private fun doPrismaFmtFormatterTest() {
    doFormatterTest(false, true)
  }

  private fun doFormatterTest(internal: Boolean = true, prismaFmt: Boolean = true) {
    val testName = getTestName(true)
    myFixture.configureByFile("$testName.prisma")

    val psiFile = myFixture.file
    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)!!

    if (internal) {
      format(psiFile, document, false)
      myFixture.checkResultByFile("${testName}_after.prisma")
    }

    if (prismaFmt) {
      format(psiFile, document, true)
      myFixture.checkResultByFile("${testName}_after.prisma")
    }
  }

  private fun format(psiFile: PsiFile, document: Document, usePrismaFmt: Boolean) {
    WriteCommandAction.runWriteCommandAction(project) {
      try {
        PrismaFormattingService.USE_PRISMA_FMT.set(psiFile, usePrismaFmt)
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