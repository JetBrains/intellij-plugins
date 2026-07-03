package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.packageChecker.model.exceptions.AnalysisException
import com.intellij.packageChecker.model.exceptions.PackageCheckerHeadlessAnalysisException
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.qodana.staticAnalysis.packageChecker.PackageCheckerInspectListener

class QodanaAnalysisCancellationTest : BasePlatformTestCase() {
  fun testQodanaAnalysisCancellationHookIsConsumedOnce() {
    val firstCause = Throwable("first")
    val secondCause = Throwable("second")
    val cancellationRequests = mutableListOf<Pair<String, Throwable?>>()

    project.service<QodanaAnalysisCancellationService>().registerHook { message: String, cause: Throwable? ->
      cancellationRequests += message to cause
    }

    try {
      assertTrue(project.service<QodanaAnalysisCancellationService>().requestCancel("first", firstCause))
      assertTrue(project.service<QodanaAnalysisCancellationService>().requestCancel("second", secondCause))
      assertEquals(listOf("first" to firstCause), cancellationRequests)

      project.service<QodanaAnalysisCancellationService>().removeHook()

      assertFalse(project.service<QodanaAnalysisCancellationService>().requestCancel("third"))
    }
    finally {
      project.service<QodanaAnalysisCancellationService>().removeHook()
    }
  }

  fun testPackageCheckerInspectListenerRequestsCancellation() {
    val failure = PackageCheckerHeadlessAnalysisException("failed", Throwable("boom"))
    val cancellationRequests = mutableListOf<Pair<String, Throwable?>>()

    project.service<QodanaAnalysisCancellationService>().registerHook { message: String, cause: Throwable? ->
      cancellationRequests += message to cause
    }

    try {
      PackageCheckerInspectListener().inspectionFailed("toolId", failure, null, project)

      assertEquals(
        listOf(PackageCheckerInspectListener.PACKAGE_CHECKER_QODANA_CANCELLATION_MESSAGE to failure),
        cancellationRequests
      )
    }
    finally {
      project.service<QodanaAnalysisCancellationService>().removeHook()
    }
  }

  fun testPackageCheckerInspectListenerIgnoresGenericAnalysisException() {
    val cancellationRequests = mutableListOf<Pair<String, Throwable?>>()

    project.service<QodanaAnalysisCancellationService>().registerHook { message: String, cause: Throwable? ->
      cancellationRequests += message to cause
    }

    try {
      PackageCheckerInspectListener().inspectionFailed("toolId", AnalysisException("failed"), null, project)

      assertTrue(cancellationRequests.isEmpty())
    }
    finally {
      project.service<QodanaAnalysisCancellationService>().removeHook()
    }
  }

  fun testDirectQodanaCancellationKeepsWrapperForReporting() {
    val cause = PackageCheckerHeadlessAnalysisException("failed", Throwable("boom"))
    val cancellation = QodanaCancellationException(PackageCheckerInspectListener.PACKAGE_CHECKER_QODANA_CANCELLATION_MESSAGE)
    cancellation.initCause(cause)

    assertSame(cancellation, cancellationThrowableToReport(cancellation))
  }

  fun testProcessCanceledExceptionKeepsQodanaCancellationWrapperForReporting() {
    val cause = PackageCheckerHeadlessAnalysisException("failed", Throwable("boom"))
    val cancellation = QodanaCancellationException(PackageCheckerInspectListener.PACKAGE_CHECKER_QODANA_CANCELLATION_MESSAGE)
    cancellation.initCause(cause)
    val exception = ProcessCanceledException(cancellation)

    assertSame(cancellation, cancellationThrowableToReport(exception))
  }
}
