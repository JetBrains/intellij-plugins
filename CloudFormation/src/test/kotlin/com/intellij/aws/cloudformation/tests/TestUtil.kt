package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationInspections
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationProblem
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.rt.execution.junit.FileComparisonFailure
import junit.framework.TestCase
import java.io.File
import java.io.IOException
import java.io.StringWriter

object TestUtil {
  fun getTestDataPath(relativePath: String): String {
    return getTestDataFile(relativePath).path + File.separator
  }

  fun getTestDataFile(relativePath: String): File {
    return File(testDataRoot, relativePath)
  }

  val testDataRoot by lazy {
    val lookupDirectories = listOf(
      File(PathManager.getHomePath()),
      File(PathManager.getHomePath()).parentFile,
      File(System.getProperty("user.dir"))
    )

    val lookupMarker = listOf(
      "intellij-plugins/CloudFormation/testData/cloudFormationTestData.marker",
      "contrib/CloudFormation/testData/cloudFormationTestData.marker",
      "testData/cloudFormationTestData.marker"
    )

    for (directory in lookupDirectories) {
      for (marker in lookupMarker) {
        val file = File(directory, marker)
        if (file.exists()) return@lazy file.parentFile
      }
    }

    error("Could not find CloudFormation plugin testData")
  }

  fun nodeToString(node: CfnNode): String = MyToStringStyle.toString(node, arrayOf("allTopLevelProperties", "functionId"))

  fun checkContent(expectFile: File, actualContent: String) {
    val normalizedLines = actualContent
        .lines()
        .map { it.trimEnd() }
        .joinToString(separator = "\n")

    val actualNormalized = normalizedLines.trimEnd('\n') + '\n'

    if (actualNormalized.isBlank()) {
      if (expectFile.exists()) {
        TestCase.fail("Actual content is empty (i.e. no problems) => expect file $expectFile should not exist")
      }

      return
    }

    if (!expectFile.exists()) {
      expectFile.writeText("")
    }

    val expectText: String
    try {
      expectText = StringUtil.convertLineSeparators(FileUtil.loadFile(expectFile, Charsets.UTF_8))
    } catch (e: IOException) {
      throw RuntimeException(e)
    }

    if (expectText != actualNormalized) {
      throw FileComparisonFailure("Expected text mismatch", expectText, actualNormalized, expectFile.path)
    }
  }

  private fun addMarkers(s: String, markers: List<Pair<Int, String>>): String {
    return markers
        .sortedBy { it.first }
        .fold(
            Pair(s, 0),
            { acc, el ->
              val (str, drift) = acc
              val (pos, marker) = el

              Pair(str.substring(0, pos + drift) + marker + str.substring(pos + drift), drift + marker.length)
            }
        ).first
  }

  fun renderProblems(file: PsiFile, problems: List<CloudFormationProblem>): String {
    if (problems.isEmpty()) {
      return ""
    }

    val writer = StringWriter()

    val markers = problems.mapIndexed { n, problem ->
      val el = problem.element
      listOf(Pair(el.textOffset, "$n@<"), Pair(el.textOffset + el.textLength, ">"))
    }.flatten()

    val textWithMarkers = addMarkers(file.text, markers)

    writer.append(textWithMarkers)
    if (!textWithMarkers.endsWith("\n")) {
      writer.appendln()
    }

    if (problems.isNotEmpty()) {
      writer.appendln()
    }

    for (problem in problems.withIndex()) {
      writer.appendln("${problem.index}: ${problem.value.description}")
    }

    return writer.toString()
  }

  fun renderReferences(file: PsiFile): String {
    val parsed = CloudFormationParser.parse(file)
    val inspected = CloudFormationInspections.inspectFile(parsed)
    val referencesList = inspected.references.values().sortedBy { it.element.textOffset }

    if (inspected.references.isEmpty) {
      return "No references"
    }

    val writer = StringWriter()

    val markers = referencesList.mapIndexed { n, ref ->
      val el = ref.element
      val rangeInEl = ref.rangeInElement
      listOf(Pair(el.textOffset + rangeInEl.startOffset, "$n@<"), Pair(el.textOffset + rangeInEl.endOffset, ">"))
    }.flatten()

    val textWithMarkers = addMarkers(file.text, markers)

    writer.append(textWithMarkers)
    if (!textWithMarkers.endsWith("\n")) {
      writer.appendln()
    }

    for (ref in referencesList.withIndex()) {
      val result = ref.value.resolve()
      writer.append("${ref.index}: ")
      if (result == null) {
        writer.appendln("not resolved")
      } else {
        writer.appendln(result.text)
      }
    }

    return writer.toString()
  }
}
