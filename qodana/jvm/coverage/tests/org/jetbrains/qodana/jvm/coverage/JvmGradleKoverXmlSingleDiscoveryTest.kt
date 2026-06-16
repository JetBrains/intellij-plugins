package org.jetbrains.qodana.jvm.coverage

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class JvmGradleKoverXmlSingleDiscoveryTest(case: Case) : JvmCoverageDiscoveryTest("JvmCoverageInspection", case) {
  @Test fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("koverReport", ReportLocation("report.xml", "build/reports/kover/report.xml")),
      Case("koverXmlSubdir", ReportLocation("report.xml", "build/reports/kover/xml/report.xml")),
      Case("koverProjectXmlDir", ReportLocation("report.xml", "reports/kover/project-xml/report.xml")),
      Case("koverCoverageXmlName", ReportLocation("report.xml", "build/reports/kover/coverage.xml")),
      Case("koverMavenSite", ReportLocation("report.xml", "target/site/kover/report.xml")),
    )
  }
}
