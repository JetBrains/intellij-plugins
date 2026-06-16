package org.jetbrains.qodana.jvm.coverage

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class AndroidAgpCoverageSingleDiscoveryTest(case: Case) : JvmCoverageDiscoveryTest("JvmCoverageInspection", case) {
  @Test fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("unitDebug", ReportLocation("report.xml", "build/reports/coverage/test/debug/report.xml")),
      Case("androidDebug", ReportLocation("report.xml", "build/reports/coverage/androidTest/debug/report.xml")),
      Case("unitFlavorDebug", ReportLocation("report.xml", "build/reports/coverage/test/foss/debug/report.xml")),
      Case("androidFlavorConnected", ReportLocation("report.xml", "build/reports/coverage/androidTest/playstore/debug/report.xml")),
    )
  }
}
