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
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import org.jetbrains.yaml.psi.YAMLFile
import org.junit.Assert
import org.junit.Test
import java.io.File

abstract class BaseInspectionTestCase(val folder: String) : LightCodeInsightFixtureTestCase() {
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
    val testsPath = File(testDataPath)
    Assert.assertTrue("Is not a directory: $testsPath", testsPath.isDirectory)

    val list = (testsPath.listFiles() ?: emptyArray())
        .filter { !it.isDirectory && !it.name.endsWith(".expected")}
    Assert.assertTrue("No files in $testsPath", list.isNotEmpty())

    val allPsiFiles = myFixture.configureByFiles(*list.map { it.name }.toTypedArray())
    Assert.assertTrue(allPsiFiles.size == list.size)

    allPsiFiles.forEach { psiFile ->
      val actualContent = getTestResultContent(psiFile)
      val expectFile = File(testDataPath, "${File(psiFile.virtualFile.path).nameWithoutExtension}.expected")

      TestUtil.checkContent(expectFile, actualContent)
    }
  }

  override fun getTestDataPath(): String {
    val path = File(TestUtil.getTestDataPath(folder)).absoluteFile
    Assert.assertTrue("$path is not a directory", path.isDirectory)
    return path.path
  }
}
