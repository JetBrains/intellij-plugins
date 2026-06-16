package org.jetbrains.qodana.jvm.coverage

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class JvmKoverIcSingleDiscoveryTest(case: Case) : JvmCoverageDiscoveryTest("JvmCoverageInspection", case) {
  @Test fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("koverIcReportsDir", ReportLocation("report.ic", "build/reports/kover/report.ic")),
      Case("koverIcBuildDir", ReportLocation("report.ic", "build/kover/report.ic")),
      Case("koverIcGenericName", ReportLocation("report.ic", "build/kover/snapshot.ic")),
      Case("koverIcMavenSite", ReportLocation("report.ic", "target/site/kover/report.ic")),
    )
  }
}
