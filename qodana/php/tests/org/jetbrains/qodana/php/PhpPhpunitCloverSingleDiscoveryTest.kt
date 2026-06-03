package org.jetbrains.qodana.php

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test

class PhpPhpunitCloverSingleDiscoveryTest : QodanaCoverageDiscoveryTest("PhpCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "cloverBuildLogs" -> listOf(ReportLocation("report.xml", "build/logs/clover.xml"))
    "cloverRoot" -> listOf(ReportLocation("report.xml", "clover.xml"))
    "phpunitCoverageRoot" -> listOf(ReportLocation("report.xml", "phpunit.coverage.xml"))
    "coverageDir" -> listOf(ReportLocation("report.xml", "coverage/coverage.xml"))
    else -> error("Unknown test method: $testName")
  }

  @Test fun cloverBuildLogs() = runDiscovery()
  @Test fun cloverRoot() = runDiscovery()
  @Test fun phpunitCoverageRoot() = runDiscovery()
  @Test fun coverageDir() = runDiscovery()
}
