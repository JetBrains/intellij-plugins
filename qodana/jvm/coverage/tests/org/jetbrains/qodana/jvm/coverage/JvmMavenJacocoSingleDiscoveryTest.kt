package org.jetbrains.qodana.jvm.coverage

import org.junit.Test

class JvmMavenJacocoSingleDiscoveryTest : JvmCoverageDiscoveryTest("JvmCoverageInspection") {
  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "jacocoSite" -> listOf(ReportLocation("report.xml", "target/site/jacoco/jacoco.xml"))
    "jacocoAggregate" -> listOf(ReportLocation("report.xml", "target/site/jacoco-aggregate/jacoco.xml"))
    else -> error("Unknown test method: $testName")
  }

  @Test fun jacocoSite() = runDiscovery()
  @Test fun jacocoAggregate() = runDiscovery()
}
