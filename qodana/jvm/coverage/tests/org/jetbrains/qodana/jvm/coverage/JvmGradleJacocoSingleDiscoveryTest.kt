package org.jetbrains.qodana.jvm.coverage

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class JvmGradleJacocoSingleDiscoveryTest(case: Case) : JvmCoverageDiscoveryTest("JvmCoverageInspection", case) {
  @Test fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("jacocoTestDir", ReportLocation("report.xml", "build/reports/jacoco/test/jacocoTestReport.xml")),
      Case("jacocoGlobDir", ReportLocation("report.xml", "build/reports/jacoco/integrationTest/jacocoTestReport.xml")),
    )
  }
}
