package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationInspections
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationProblem
import com.intellij.aws.cloudformation.CloudFormationPsiUtils
import com.intellij.aws.cloudformation.inspections.JsonUnresolvedReferencesInspection
import com.intellij.aws.cloudformation.inspections.YamlUnresolvedReferencesInspection
import com.intellij.codeInspection.InspectionManager
import com.intellij.json.psi.JsonFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.ModuleFixture
import org.jetbrains.yaml.psi.YAMLFile
import org.junit.Assert
import org.junit.Test
import java.io.File

abstract class BaseInspectionTestCase(private val folder: String) : CodeInsightFixtureTestCase<ModuleFixtureBuilder<ModuleFixture>>() {
  private fun getTestResultContent(psiFile: PsiFile): String {
    val filePath = psiFile.virtualFile.path
    println("Working on $filePath")

    if (!CloudFormationPsiUtils.isCloudFormationFile(psiFile)) {
      return "Not a cloudformation file"
    }

    val parsed = CloudFormationParser.parse(psiFile)
    if (parsed.root.resourcesNode == null) {
      return "Resources node"
    }

    val inspected = CloudFormationInspections.inspectFile(parsed)

    val unresolvedInspection = when (psiFile) {
      is YAMLFile -> YamlUnresolvedReferencesInspection()
      is JsonFile -> JsonUnresolvedReferencesInspection()
      else -> error("Unsupported psi file type ${psiFile.javaClass.simpleName} in $filePath")
    }

    val unresolvedReferenceProblems = unresolvedInspection
        .checkFile(psiFile, InspectionManager.getInstance(psiFile.project), true)!!
        .sortedBy { it.psiElement.textOffset }
        .map { CloudFormationProblem(it.psiElement, it.descriptionTemplate) }
    return TestUtil.renderProblems(psiFile, parsed.problems + inspected.problems + unresolvedReferenceProblems)
  }

  @Test
  fun testAll() {
    val testsPath = File(myFixture.testDataPath)
    Assert.assertTrue("Is not a directory: $testsPath", testsPath.isDirectory)

    val list = (testsPath.listFiles() ?: emptyArray())
        .filter {
          !it.isDirectory &&
            !it.name.endsWith(".expected") &&
            !it.name.endsWith("swagger.yaml")
        }
    Assert.assertTrue("No files in $testsPath", list.isNotEmpty())

    val allPsiFiles = myFixture.configureByFiles(*list.map { it.name }.toTypedArray())
    Assert.assertTrue(allPsiFiles.size == list.size)

    allPsiFiles.forEach { psiFile ->
      val actualContent = getTestResultContent(psiFile)
      val expectFile = File(myFixture.testDataPath, "${File(psiFile.virtualFile.path).nameWithoutExtension}.expected")

      TestUtil.checkContent(expectFile, actualContent)
    }
  }

  override fun setUp() {
    super.setUp()
    myFixture.testDataPath = TestUtil.getTestDataPath(folder)
  }
}
