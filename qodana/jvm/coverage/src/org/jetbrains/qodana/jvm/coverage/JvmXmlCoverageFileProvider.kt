package org.jetbrains.qodana.jvm.coverage

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.readReportHead
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.xmlRootElement
import java.nio.file.Path

internal class JvmXmlCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.XMLReportEngine

  override val canonicalExtension: String = "xml"

  override fun isValidCoverageReport(file: Path): Boolean = isJacocoXmlReport(file)

  // Tool-stamped JaCoCo names (Maven default + Gradle JacocoReport task output) are unique to the JVM JaCoCo schema.
  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("jacoco.xml", "jacocoTestReport.xml"),
      dirs = listOf(
        "target/site/jacoco", "target/site/jacoco-it", "target/site/jacoco-aggregate",
        "build/reports/jacoco/test", "build/reports/jacoco/*",
      ),
    )

  // Generically named JaCoCo-schema XML (Kover, AGP)
  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("report.xml", "coverage.xml"),
      dirs = listOf(
        "build/reports/kover", "build/reports/kover/xml", "reports/kover/project-xml", "build/reports/coverage/**",
      ),
    )
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
