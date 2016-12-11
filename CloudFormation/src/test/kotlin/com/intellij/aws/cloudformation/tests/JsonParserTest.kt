package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.IndentWriter
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.rt.execution.junit.FileComparisonFailure
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import junit.framework.TestCase
import java.io.File
import java.io.IOException
import java.io.StringWriter

class JsonParserTest : LightPlatformCodeInsightTestCase() {
  fun testFile1() = runTest("file1")
  fun testWrongResources() = runTest("wrongResources")

  fun runTest(name: String) {
    configureByFile("$name.template")
    val parsed = CloudFormationParser.parse(myFile)

    val writer = StringWriter()

    val printer = IndentWriter(writer, "  ")
    for (problem in parsed.problems) {
      printer.println(problem.description + " at line " + getLineNumber(problem.element))
    }

    if (!parsed.problems.isEmpty()) {
      printer.println()
    }

    parsed.root.dump(printer)

    checkContent("$name.expected", writer.toString())
  }

  private fun getLineNumber(psiElement: PsiElement): Int {
    if (!psiElement.isValid) return -1
    assertTrue(psiElement.isPhysical)
    val manager = InjectedLanguageManager.getInstance(psiElement.project)
    val containingFile = manager.getTopLevelFile(psiElement)
    val document = PsiDocumentManager.getInstance(psiElement.project).getDocument(containingFile) ?: return -1
    var textRange = psiElement.textRange ?: return -1
    textRange = manager.injectedToHost(psiElement, textRange)
    val startOffset = textRange.startOffset
    val textLength = document.textLength
    assertTrue(" at $startOffset, $textLength", startOffset <= textLength)
    return document.getLineNumber(startOffset) + 1
  }

  fun checkContent(expectFileName: String, actualContent: String) {
    val actualNormalized = StringUtil.convertLineSeparators(actualContent)
    val expectFile = File(testDataPath, expectFileName)

    if (!expectFile.exists()) {
      expectFile.writeText(actualNormalized)
      TestCase.fail("Wrote ${expectFile.path} with actual content")
    }

    val expectText: String
    try {
      expectText = StringUtil.convertLineSeparators(FileUtil.loadFile(expectFile, CharsetToolkit.UTF8_CHARSET))
    } catch (e: IOException) {
      throw RuntimeException(e)
    }

    if (expectText != actualNormalized) {
      throw FileComparisonFailure("Expected text mismatch", expectText, actualNormalized, expectFile.path)
    }
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("parser/json")
  }
}
