package org.intellij.prisma

import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.IncorrectOperationException
import org.intellij.prisma.ide.formatter.PrismaFormattingService

class PrismaIntegrationTest : JSTempDirWithNodeInterpreterTest() {
  override fun getBasePath(): String = "${getPrismaRelativeTestDataPath()}/integration"

  fun testPrismaFmtSchema() {
    doFormatterTestWithPrismaFmt()
  }

  fun testPrismaFmtEmptyFile() {
    doFormatterTestWithPrismaFmt()
  }

  private fun doFormatterTestWithPrismaFmt() {
    val testName = getTestName(true)
    myFixture.copyDirectoryToProject(testName, "")
    myFixture.configureFromTempProjectFile("$testName.prisma")

    val psiFile = myFixture.file
    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)

    WriteCommandAction.runWriteCommandAction(project) {
      try {
        PrismaFormattingService.USE_PRISMA_FMT.set(psiFile, true)
        CodeStyleManager.getInstance(project)
          .reformatText(psiFile, psiFile.textRange.startOffset, psiFile.textRange.endOffset)
        PsiDocumentManager.getInstance(project).commitDocument(document!!)
      }
      catch (e: IncorrectOperationException) {
        fail()
      }
    }

    myFixture.checkResultByFile("$testName/${testName}_after.prisma")
  }
}