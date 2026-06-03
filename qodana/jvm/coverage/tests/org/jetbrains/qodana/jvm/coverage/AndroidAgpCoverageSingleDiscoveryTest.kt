package org.jetbrains.qodana.jvm.coverage

import org.junit.Test


class AndroidAgpCoverageSingleDiscoveryTest : JvmCoverageDiscoveryTest("JvmCoverageInspection") {

  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "unitDebug" -> listOf(ReportLocation("report.xml", "build/reports/coverage/test/debug/report.xml"))
    "androidDebug" -> listOf(ReportLocation("report.xml", "build/reports/coverage/androidTest/debug/report.xml"))
    "unitFlavorDebug" -> listOf(ReportLocation("report.xml", "build/reports/coverage/test/foss/debug/report.xml"))
    "androidFlavorConnected" -> listOf(ReportLocation("report.xml", "build/reports/coverage/androidTest/google/debug/connected/report.xml"))
    else -> error("Unknown test method: $testName")
  }

  @Test fun unitDebug() = runDiscovery()
  @Test fun androidDebug() = runDiscovery()
  @Test fun unitFlavorDebug() = runDiscovery()
  @Test fun androidFlavorConnected() = runDiscovery()
}
