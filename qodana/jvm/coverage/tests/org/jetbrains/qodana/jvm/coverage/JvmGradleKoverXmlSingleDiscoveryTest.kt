package org.jetbrains.qodana.jvm.coverage

import org.junit.Test

class JvmGradleKoverXmlSingleDiscoveryTest : JvmCoverageDiscoveryTest("JvmCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "koverReport" -> listOf(ReportLocation("report.xml", "build/reports/kover/report.xml"))
    "koverXmlSubdir" -> listOf(ReportLocation("report.xml", "build/reports/kover/xml/report.xml"))
    else -> error("Unknown test method: $testName")
  }

  @Test fun koverReport() = runDiscovery()
  @Test fun koverXmlSubdir() = runDiscovery()
}
