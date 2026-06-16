package org.jetbrains.qodana.jvm.coverage

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class JvmMavenJacocoSingleDiscoveryTest(case: Case) : JvmCoverageDiscoveryTest("JvmCoverageInspection", case) {
  @Test fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("jacocoSite", ReportLocation("report.xml", "target/site/jacoco/jacoco.xml")),
      Case("jacocoIt", ReportLocation("report.xml", "target/site/jacoco-it/jacoco.xml")),
      Case("jacocoAggregate", ReportLocation("report.xml", "target/site/jacoco-aggregate/jacoco.xml")),
    )
  }
}
