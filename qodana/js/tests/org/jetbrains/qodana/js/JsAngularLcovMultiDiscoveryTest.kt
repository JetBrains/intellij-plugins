package org.jetbrains.qodana.js

import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class JsAngularLcovMultiDiscoveryTest(case: Case) : QodanaCoverageDiscoveryTest("JsCoverageInspection", case) {
  @Test
  fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case(
        "multiProject",
        ReportLocation("report-app.info", "coverage/app/lcov.info"),
        ReportLocation("report-lib.info", "coverage/lib/lcov.info"),
      ),
    )
  }
}
