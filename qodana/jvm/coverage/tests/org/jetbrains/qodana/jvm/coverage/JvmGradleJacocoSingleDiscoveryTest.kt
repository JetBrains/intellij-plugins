package org.jetbrains.qodana.jvm.coverage

import org.junit.Test


class JvmGradleJacocoSingleDiscoveryTest : JvmCoverageDiscoveryTest("JvmCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "jacocoTestDir" -> listOf(ReportLocation("report.xml", "build/reports/jacoco/test/jacocoTestReport.xml"))
    "jacocoGlobDir" -> listOf(ReportLocation("report.xml", "build/reports/jacoco/integrationTest/jacocoTestReport.xml"))
    else -> error("Unknown test method: $testName")
  }

  @Test fun jacocoTestDir() = runDiscovery()
  @Test fun jacocoGlobDir() = runDiscovery()
}
