// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.service

import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.platform.lsp.testFramework.waitForDiagnosticsFromLspServer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.IncorrectOperationException
import org.intellij.prisma.getPrismaRelativeTestDataPath
import org.intellij.prisma.ide.lsp.PrismaLspServerActivationRule
import org.intellij.prisma.service.PrismaServiceFixture.Companion.prismaServiceFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.File

@TestApplication
class PrismaFormatterServiceTest {
  private val prisma by prismaServiceFixture("${getPrismaRelativeTestDataPath()}/formatter")

  @Test
  fun testIndents() {
    doFormatterTest()
  }

  @Test
  fun testLineSpacing() {
    doFormatterTest()
  }

  @Test
  fun testTrailingNewLine() {
    doFormatterTest()
  }

  @Test
  fun testEmptyFile() {
    doFormatterTest()
  }

  @Test
  fun testModelSpacing() {
    doFormatterTest()
  }

  @Test
  fun testEnumSpacing() {
    doFormatterTest()
  }

  @Test
  fun testTypeSpacing() {
    doFormatterTest()
  }

  @Test
  fun testGeneratorSpacing() {
    doFormatterTest()
  }

  @Test
  fun testDatasourceSpacing() {
    doFormatterTest()
  }

  @Test
  fun testKeyValueAlignment() {
    doFormatterTest()
  }

  @Test
  fun testFieldsAlignment() {
    doFormatterTest()
  }

  @Test
  fun testTypeAliasAlignment() {
    doFormatterTest()
  }

  @Test
  fun testAlignmentWithDocComments() {
    doFormatterTest()
  }

  @Test
  fun testEnumAlignment() {
    doFormatterTest()
  }

  @Test
  fun testSchema() {
    doPrismaFmtFormatterTest()
  }

  @Test
  fun testDocComment() {
    doFormatterTest()
  }

  @Test
  fun testContractUnchanged() {
    // Language server 31.13.0 returns no edits for a contract, and the reformat must not fall back to the native formatter
    val text = "model   M {\nid    Int   @id\n}\n"
    assertFalse(formatTextByServer("schema.prisma", text) == text)

    val contract = "// use prisma-8\n$text"
    assertEquals(contract, formatTextByServer("contract.prisma", contract))
  }

  private fun formatTextByServer(fileName: String, text: String): String {
    PrismaLspServerActivationRule.markForceEnabled(true)
    val psiFile = prisma.fixture.configureByText(fileName, text)
    waitForServerFormatting(psiFile)
    WriteCommandAction.runWriteCommandAction(prisma.project) {
      CodeStyleManager.getInstance(prisma.project).reformatText(psiFile, 0, psiFile.textLength)
    }
    return prisma.fixture.editor.document.text
  }

  private fun waitForServerFormatting(psiFile: PsiFile) {
    // The server starts asynchronously. Without it, the reformat silently falls back to the native formatter.
    // The first diagnostics also confirm that the server got `didOpen`, which the client can send after the formatting request.
    runInEdtAndWait { waitForDiagnosticsFromLspServer(prisma.project, psiFile.virtualFile) }
    val service = runReadActionBlocking { FormattingService.EP_NAME.findFirstSafe { it.canFormat(psiFile) } }
    assertEquals("LspFormattingService", service?.javaClass?.simpleName)
  }

  private fun doInternalFormatterTest() {
    doFormatterTest(true, false)
  }

  private fun doPrismaFmtFormatterTest() {
    doFormatterTest(false, true)
  }

  private fun doFormatterTest(internal: Boolean = true, prismaFmt: Boolean = true) {
    val testName = prisma.getTestName(true)

    if (internal) {
      format(testName, false)
      prisma.fixture.checkResultByFile("${testName}_after.prisma")
    }

    if (prismaFmt) {
      format(testName, true)
      // prisma-fmt keeps some spacing that the native formatter normalizes
      val prismaFmtResult = "${testName}_prismaFmt_after.prisma"
      prisma.fixture.checkResultByFile(if (File(prisma.testDataPath, prismaFmtResult).isFile) prismaFmtResult else "${testName}_after.prisma")
    }
  }

  private fun format(testName: String, usePrismaFmt: Boolean) {
    PrismaLspServerActivationRule.markForceEnabled(usePrismaFmt)
    prisma.fixture.configureByFile("$testName.prisma")

    val psiFile = prisma.fixture.file
    val document = PsiDocumentManager.getInstance(prisma.project).getDocument(psiFile)!!

    if (usePrismaFmt) {
      waitForServerFormatting(psiFile)
    }

    WriteCommandAction.runWriteCommandAction(prisma.project) {
      try {
        CodeStyleManager.getInstance(prisma.project)
          .reformatText(psiFile, psiFile.textRange.startOffset, psiFile.textRange.endOffset)
        PsiDocumentManager.getInstance(prisma.project).commitDocument(document)
      }
      catch (e: IncorrectOperationException) {
        fail()
      }
    }
  }
}