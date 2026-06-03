package org.jetbrains.qodana.jvm.coverage

import org.junit.Test

class JvmKoverIcSingleDiscoveryTest : JvmCoverageDiscoveryTest("JvmCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "koverIcReportsDir" -> listOf(ReportLocation("report.ic", "build/reports/kover/report.ic"))
    "koverIcBuildDir" -> listOf(ReportLocation("report.ic", "build/kover/report.ic"))
    else -> error("Unknown test method: $testName")
  }

  @Test fun koverIcReportsDir() = runDiscovery()
  @Test fun koverIcBuildDir() = runDiscovery()
}
