package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.codeInsight.TargetElementUtil
import com.intellij.codeInspection.InspectionManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.LightPlatformTestCase
import junit.framework.TestCase
import java.io.File
import java.io.IOException

class JsonParserTest : LightPlatformCodeInsightTestCase() {
  fun test1() {
    configureByFile("wrongResources.template")
    val parser = CloudFormationParser(InspectionManager.getInstance(LightPlatformCodeInsightTestCase.myFile.project), true)
    val file = parser.file(LightPlatformCodeInsightTestCase.myFile)

    val element = TargetElementUtil.findTargetElement(
        LightPlatformCodeInsightTestCase.myEditor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)!!
    RenameProcessor(LightPlatformTestCase.getProject(), element, "NEW_NAME", false, false).run()
    checkResultByFile("simpleEntity.after.template")
  }

  fun checkContent(expectFileName: String, actualContent: String) {
    val expectFile = File(testDataPath, expectFileName)

    TestCase.assertTrue("Cannot find file " + expectFile, expectFile.exists())
    val fileText: String
    try {
      fileText = FileUtil.loadFile(expectFile, CharsetToolkit.UTF8_CHARSET)
    } catch (e: IOException) {
      throw RuntimeException(e)
    }

    checkResultByText("File content mismatch", StringUtil.convertLineSeparators(fileText), true, expectFile.path)
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("parser/json")
  }
}
