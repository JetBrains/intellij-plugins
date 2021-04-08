package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationPsiUtils
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.rd.attach
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.jetbrains.yaml.psi.YAMLFile
import org.junit.Assert

class CloudFormationFileTypeDetectionTest: LightPlatformCodeInsightTestCase() {
  fun `test some random file`() =
    assertNotCloudFormationFile("my1.some_random_extension", "Nothing about CloudFormation")

  fun `test yaml file without cloudFormation content`() =
    assertNotCloudFormationFile("my1.yaml", "Nothing about CloudFormation")

  fun `test yaml file with cloudFormation format version`() =
    assertCloudFormationFile("my2.yaml", """
      Parameters:
      AWSTemplateFormatVersion: '2010-09-09'
    """.trimIndent()) { file -> Assert.assertTrue("File: $file instead of Yaml", file is YAMLFile) }

  fun `test template file with format version and yaml format`() =
    assertCloudFormationFile("my2.template", """
      Parameters:
      AWSTemplateFormatVersion: '2010-09-09'
    """.trimIndent()) { file -> Assert.assertTrue("File: $file instead of Yaml", file is YAMLFile) }

  fun `test json file without cloudFormation content`() =
    assertNotCloudFormationFile("my1.json", "{}")

  fun `test json file with cloudFormation format version`() =
    assertCloudFormationFile("my2.json", """
      {
        "AWSTemplateFormatVersion": "2010-09-09",
        "Resources": ["XXX"]
      }
    """.trimIndent()) { file -> Assert.assertTrue("File: $file instead of Json", file is JsonFile) }

  fun `test template file with format version and json format`() =
    assertCloudFormationFile("my2.template", """
      {
        "AWSTemplateFormatVersion": "2010-09-09",
        "Resources": ["XXX"]
      }
    """.trimIndent()) { file -> Assert.assertTrue("File: $file instead of Json", file is JsonFile) }

  fun `test plain text template file with format version and json format`() {
    mapTemplateExtensionToPlainText()
    assertCloudFormationFile("my2.template", """
      {
        "AWSTemplateFormatVersion": "2010-09-09",
        "Resources": ["XXX"]
      }
    """.trimIndent()) { file -> Assert.assertTrue("File: $file instead of Json", file is JsonFile) }
  }

  fun `test plain template file with format version and yaml format`() {
    mapTemplateExtensionToPlainText()
    assertCloudFormationFile("my2.template", """
      Parameters:
      AWSTemplateFormatVersion: '2010-09-09'
    """.trimIndent()) { file -> Assert.assertTrue("File: $file instead of Yaml", file is YAMLFile) }
  }

  private fun mapTemplateExtensionToPlainText() {
    CodeInsightTestFixtureImpl.associateExtensionTemporarily(PlainTextFileType.INSTANCE, "template", testRootDisposable)
  }

  private fun assertNotCloudFormationFile(fileName: String, fileText: String, check: (PsiFile) -> Unit = {}) {
    val document = configureFromFileText(fileName, fileText)
    val file = FileDocumentManager.getInstance().getFile(document)
    Assert.assertNotNull(file)

    val psiFile = PsiManager.getInstance(project).findFile(file!!)
    Assert.assertNotNull("Can't get psi file for file name '$fileName' with text '$fileText'", psiFile)

    if (CloudFormationPsiUtils.isCloudFormationFile(psiFile!!)) {
      Assert.assertNotNull("A cloudFormation file, while it should not be. file name '$fileName' with text '$fileText'", psiFile)
    }

    try {
      check(psiFile)
    } catch (t: Throwable) {
      throw AssertionError("Failed check, file name '$fileName' with text '$fileText'. Cause: ${t.message}", t)
    }
  }

  private fun assertCloudFormationFile(fileName: String, fileText: String, check: (PsiFile) -> Unit = {}) {
    val document = configureFromFileText(fileName, fileText)
    val file = FileDocumentManager.getInstance().getFile(document)
    Assert.assertNotNull(file)

    val psiFile = PsiManager.getInstance(project).findFile(file!!)
    Assert.assertNotNull("Can't get psi file for file name '$fileName' with text '$fileText'", psiFile)

    if (!CloudFormationPsiUtils.isCloudFormationFile(psiFile!!)) {
      Assert.assertNotNull("Not a CloudFormation file '$fileName' with text '$fileText'", psiFile)
    }

    try {
      check(psiFile)
    } catch (t: Throwable) {
      throw AssertionError("Failed check, file name '$fileName' with text '$fileText'. Cause: ${t.message}", t)
    }
  }
}