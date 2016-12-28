package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationPsiUtils.getLineNumber
import com.intellij.aws.cloudformation.IndentWriter
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.CharsetToolkit
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

    //parsed.root.dump(printer)

    checkContent("$name.expected", writer.toString())
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
