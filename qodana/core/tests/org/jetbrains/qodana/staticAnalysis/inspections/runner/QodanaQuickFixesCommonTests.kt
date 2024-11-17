package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.jetbrains.qodana.license.QodanaLicense
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.junit.Test


/**
 * Common tests of cleanup/apply fixes functionality.
 */
abstract class QodanaQuickFixesCommonTests(strategy: FixesStrategy) : QodanaQuickFixesTestBase(strategy) {
  @Test
  fun testConstantValue() {
    runTest("qodana.single:ConstantValue")
  }

  @Test
  fun testDoesntWorkForCommunity() {
    updateQodanaConfig {
      it.copy(
        license = QodanaLicense(QodanaLicenseType.COMMUNITY, false, null)
      )
    }
    runTest("qodana.single:ConstantValue")
  }

  @Test
  fun testFormattingProblems() {
    runTest("qodana.recommended")
  }

  @Test
  fun testTwoProblemsOnOneRegion() {
    runTest("qodana.recommended")
  }

  @Test
  fun testNestedProblems() {
    runTest("qodana.recommended")
  }

  @Test
  fun testInterferingProblems() {
    runTest("qodana.recommended")
  }

  @Test
  fun testProtectedMemberInFinalClassInspection() {
    runTest("qodana.single:ProtectedMemberInFinalClass")
  }
}
