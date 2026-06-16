package org.jetbrains.qodana.jvm.coverage

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.readReportHead
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.xmlRootElement
import java.nio.file.Path

internal object JvmXmlCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.XMLReportEngine

  override val canonicalExtension: String = "xml"

  override fun isValidCoverageReport(file: Path): Boolean = isJacocoXmlReport(file)

  override fun getCoverageFilesLocations(project: Project): List<Path> {
    val jacocoReports = discover(
      project,
      names = listOf("jacoco.xml", "jacocoTestReport.xml"),
      dirs = listOf(
        "target/site/jacoco", "target/site/jacoco-it", "target/site/jacoco-aggregate",
        "build/reports/jacoco/test", "build/reports/jacoco/*",
      ),
    )
    val koverReports = discover(
      project,
      names = listOf("report.xml", "coverage.xml"),
      dirs = listOf(
        "build/reports/kover", "build/reports/kover/xml", "reports/kover/project-xml",
        "target/site/kover",
      ),
    )
    val agpReports = discover(
      project,
      names = listOf("report.xml", "coverage.xml"),
      dirs = listOf(
        "build/reports/coverage/**/debug",
      ),
    )
    return jacocoReports + koverReports + agpReports
  }
}

/** `<sessioninfo>` element, or a `<counter type=...>` coverage counter — the JaCoCo structural hallmarks. */
private val JACOCO_MARKER_REGEX = Regex("""<sessioninfo\b|<counter\s+type\s*=""")

/**
 * JaCoCo / Kover XML check: a `<report>` root carrying a JaCoCo hallmark — the `-//JACOCO//DTD` DOCTYPE,
 * a `<sessioninfo>` element, or `<counter type=...>` coverage counters
 */
private fun isJacocoXmlReport(path: Path): Boolean {
  val head = readReportHead(path) ?: return false
  if (!xmlRootElement(head).equals("report", ignoreCase = true)) return false
  return head.contains("//JACOCO//") || JACOCO_MARKER_REGEX.containsMatchIn(head)
}
