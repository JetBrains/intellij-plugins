package org.jetbrains.qodana.php

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.readReportHead
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.xmlRootElement
import java.nio.file.Path


class PhpCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.PhpUnitCoverageEngine

  override val canonicalExtension: String = "xml"

  override fun isValidCoverageReport(file: Path): Boolean = isCloverXmlReport(file)

  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("clover.xml", "phpunit.coverage.xml"),
      dirs = PHP_REPORT_DIRS,
    )

  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("*.xml"),
      dirs = PHP_REPORT_DIRS,
    )
}

private val PHP_REPORT_DIRS = listOf(
  ".", "build/logs", "build/coverage", "build/reports", "coverage", "reports", "test-reports", "tests/coverage", "target",
)

/** `<project>` wrapper element. */
private val CLOVER_PROJECT_REGEX = Regex("""<project\b""")

/** A single `<line>` element carrying both `num` and `count` attributes, in any order (PHPUnit Clover). */
private val CLOVER_LINE_REGEX = Regex("""<line\b(?=[^>]*\bnum\s*=)(?=[^>]*\bcount\s*=)""")

/**
 * PHPUnit Clover XML check: a `<coverage>` root with a `<project>` wrapper and at least one `<line num=... count=...>`
 * record
 */
private fun isCloverXmlReport(path: Path): Boolean {
  val head = readReportHead(path) ?: return false
  if (!xmlRootElement(head).equals("coverage", ignoreCase = true)) return false
  return CLOVER_PROJECT_REGEX.containsMatchIn(head) && CLOVER_LINE_REGEX.containsMatchIn(head)
}