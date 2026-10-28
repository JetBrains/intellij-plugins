// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.service

import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.platform.lsp.testFramework.waitForDiagnosticsFromLspServer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.IncorrectOperationException
import org.intellij.prisma.getPrismaRelativeTestDataPath
import org.intellij.prisma.ide.lsp.PrismaLspServerActivationRule
import java.io.File

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

  fun testContractUnchanged() {
    // Language server 31.13.0 returns no edits for a contract, and the reformat must not fall back to the native formatter
    val text = "model   M {\nid    Int   @id\n}\n"
    assertFalse(formatTextByServer("schema.prisma", text) == text)

    val contract = "// use prisma-8\n$text"
    assertEquals(contract, formatTextByServer("contract.prisma", contract))
  }

  private fun formatTextByServer(fileName: String, text: String): String {
    PrismaLspServerActivationRule.markForceEnabled(true)
    val psiFile = myFixture.configureByText(fileName, text)
    waitForServerFormatting(psiFile)
    WriteCommandAction.runWriteCommandAction(project) {
      CodeStyleManager.getInstance(project).reformatText(psiFile, 0, psiFile.textLength)
    }
    return myFixture.editor.document.text
  }

  private fun waitForServerFormatting(psiFile: PsiFile) {
    // The server starts asynchronously. Without it, the reformat silently falls back to the native formatter.
    // The first diagnostics also confirm that the server got `didOpen`, which the client can send after the formatting request.
    waitForDiagnosticsFromLspServer(project, psiFile.virtualFile)
    val service = FormattingService.EP_NAME.findFirstSafe { it.canFormat(psiFile) }
    assertEquals("LspFormattingService", service?.javaClass?.simpleName)
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
      // prisma-fmt keeps some spacing that the native formatter normalizes
      val prismaFmtResult = "${testName}_prismaFmt_after.prisma"
      myFixture.checkResultByFile(if (File(testDataPath, prismaFmtResult).isFile) prismaFmtResult else "${testName}_after.prisma")
    }
  }

  private fun format(testName: String, usePrismaFmt: Boolean) {
    PrismaLspServerActivationRule.markForceEnabled(usePrismaFmt)
    myFixture.configureByFile("$testName.prisma")

    val psiFile = myFixture.file
    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)!!

    if (usePrismaFmt) {
      waitForServerFormatting(psiFile)
    }

    WriteCommandAction.runWriteCommandAction(project) {
      try {
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