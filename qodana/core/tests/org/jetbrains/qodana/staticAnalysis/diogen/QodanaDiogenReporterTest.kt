package org.jetbrains.qodana.staticAnalysis.diogen

import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaCancellationException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.testFramework.QODANA_LOG_CATEGORY
import org.jetbrains.qodana.staticAnalysis.testFramework.logRecordsFrom
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class QodanaDiogenReporterTest {
  @Test
  fun `submits an inspection report with no custom event data`() = runTest {
    val reports = mutableListOf<IdeaLoggingEvent>()
    val reporter = QodanaDiogenReporter(this, dispatcher = null) { event ->
      reports += event
      successfulSubmission()
    }
    val failure = IllegalStateException("broken inspection")

    reporter.start(enabled = true)
    reporter.reportInspectionFailure("SampleInspection", failure)
    runCurrent()
    reporter.stop()

    val report = reports.single()
    assertThat(report.message).isEqualTo("Inspection SampleInspection failed")
    assertThat(report.throwable).isSameAs(failure)
    assertThat(report.attachments).isEmpty()
    assertThat(report.data).isNull()
    assertThat(reporter.statistics).satisfies({ statistics ->
                                                assertThat(statistics.sent.get()).isEqualTo(1)
                                                assertThat(statistics.failures.get()).isZero()
                                              })
  }

  @Test
  fun `sends application crashes with no custom event data`() = runTest {
    val reports = mutableListOf<IdeaLoggingEvent>()
    val reporter = QodanaDiogenReporter(this, dispatcher = null) { event ->
      reports += event
      successfulSubmission()
    }

    reporter.start(enabled = true)
    reporter.reportApplicationCrash(IllegalStateException("broken"))
    runCurrent()
    reporter.stop()

    assertThat(reports).singleElement().satisfies({ report ->
                                                    assertThat(report.message).isEqualTo("Qodana failed with a terminal error")
                                                    assertThat(report.data).isNull()
                                                  })
  }

  @Test
  fun `does not send when disabled and suppresses equal stack trace hashes`() = runTest {
    val reports = mutableListOf<IdeaLoggingEvent>()
    val reporter = QodanaDiogenReporter(this, dispatcher = null) { event ->
      reports += event
      successfulSubmission()
    }
    val first = IllegalStateException("broken inspection")
    val duplicate = IllegalStateException("broken inspection").apply { stackTrace = first.stackTrace }

    reporter.start(enabled = false)
    reporter.reportApplicationCrash(first)
    runCurrent()
    reporter.stop()

    reporter.start(enabled = true)
    reporter.reportInspectionFailure("SampleInspection", first)
    reporter.reportApplicationCrash(duplicate)
    runCurrent()
    reporter.stop()

    assertThat(reports).singleElement().satisfies({ report ->
                                                    assertThat(report.message).isEqualTo("Inspection SampleInspection failed")
                                                  })
    assertThat(reporter.statistics).satisfies({ statistics ->
                                                assertThat(statistics.duplicates.get()).isEqualTo(1)
                                                assertThat(statistics.sent.get()).isEqualTo(1)
                                              })
  }

  @Test
  fun `does not send expected or memory failures`() = runTest {
    val reports = mutableListOf<IdeaLoggingEvent>()
    val reporter = QodanaDiogenReporter(this, dispatcher = null) { event ->
      reports += event
      successfulSubmission()
    }
    val expected = listOf(
      InspectionApplicationException("invalid configuration"),
      ProcessCanceledException(),
      QodanaCancellationException("cancelled"),
      QodanaException("expected Qodana failure"),
      OutOfMemoryError("Java heap space"),
      RuntimeException(OutOfMemoryError("Java heap space")),
    )

    reporter.start(enabled = true)
    expected.forEach(reporter::reportApplicationCrash)
    runCurrent()
    reporter.stop()

    assertThat(reports).isEmpty()
  }

  @Test
  fun `sends stack traces with equal string hash codes`() = runTest {
    val reports = mutableListOf<IdeaLoggingEvent>()
    val reporter = QodanaDiogenReporter(this, dispatcher = null) { event ->
      reports += event
      successfulSubmission()
    }
    val first = IllegalStateException("Aa")
    val second = IllegalStateException("BB").apply { stackTrace = first.stackTrace }

    assertThat(first.stackTraceToString().hashCode()).isEqualTo(second.stackTraceToString().hashCode())

    reporter.start(enabled = true)
    reporter.reportApplicationCrash(first)
    reporter.reportApplicationCrash(second)
    runCurrent()
    reporter.stop()

    assertThat(reports).hasSize(2)
    assertThat(reporter.statistics).satisfies({ statistics ->
                                                assertThat(statistics.duplicates.get()).isZero()
                                                assertThat(statistics.sent.get()).isEqualTo(2)
                                              })
  }

  @Test
  fun `does not retry a failed submission`() = runTest {
    var attempts = 0
    val reporter = QodanaDiogenReporter(this, dispatcher = null) {
      attempts++
      SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED)
    }

    reporter.start(enabled = true)
    reporter.reportApplicationCrash(IllegalStateException("broken"))
    runCurrent()
    advanceTimeBy(10.seconds)
    runCurrent()
    reporter.stop()

    assertThat(attempts).isEqualTo(1)
    assertThat(reporter.statistics).satisfies({ statistics ->
                                                assertThat(statistics.sent.get()).isZero()
                                                assertThat(statistics.failures.get()).isEqualTo(1)
                                              })
  }

  @Test
  fun `limits concurrent submissions to two`() = runTest {
    val firstTwoStarted = CompletableDeferred<Unit>()
    val release = CompletableDeferred<Unit>()
    var started = 0
    val reporter = QodanaDiogenReporter(this, dispatcher = null) { _ ->
      started++
      if (started == 2) firstTwoStarted.complete(Unit)
      release.await()
      successfulSubmission()
    }

    reporter.start(enabled = true)
    reporter.reportApplicationCrash(IllegalStateException("first"))
    reporter.reportApplicationCrash(IllegalStateException("second"))
    reporter.reportApplicationCrash(IllegalStateException("third"))
    runCurrent()
    firstTwoStarted.await()

    assertThat(started).isEqualTo(2)

    release.complete(Unit)
    runCurrent()
    reporter.stop()

    assertThat(started).isEqualTo(3)
  }

  @Test
  fun `does not record an HTTP attempt for a report cancelled while waiting for a permit`() = runTest {
    val firstTwoStarted = CompletableDeferred<Unit>()
    val release = CompletableDeferred<Unit>()
    var started = 0
    val reporter = QodanaDiogenReporter(this, dispatcher = null) { _ ->
      submittedLikeItnReporter {
        started++
        if (started == 2) firstTwoStarted.complete(Unit)
        release.await()
      }
    }

    reporter.start(enabled = true)
    reporter.reportApplicationCrash(IllegalStateException("first"))
    reporter.reportApplicationCrash(IllegalStateException("second"))
    reporter.reportApplicationCrash(IllegalStateException("third"))
    runCurrent()
    firstTwoStarted.await()

    val stopped = async { reporter.stop() }
    advanceTimeBy(DIOGEN_REPORT_DRAIN_TIMEOUT)
    runCurrent()

    assertThat(stopped.isCompleted).isTrue()
    assertThat(started).isEqualTo(2)
    assertThat(reporter.statistics).satisfies({ statistics ->
                                                assertThat(statistics.cancellations.get()).isEqualTo(3)
                                                assertThat(statistics.failures.get()).isZero()
                                              })
  }

  @Test
  fun `cancels the drain after thirty seconds and records the cancellation`() {
    val warnings = logRecordsFrom(QODANA_LOG_CATEGORY) {
      runTest {
        val sending = CompletableDeferred<Unit>()
        val neverCompletes = CompletableDeferred<Unit>()
        val reporter = QodanaDiogenReporter(this, dispatcher = null) { _ ->
          submittedLikeItnReporter {
            sending.complete(Unit)
            neverCompletes.await()
          }
        }

        reporter.start(enabled = true)
        reporter.reportApplicationCrash(IllegalStateException("broken"))
        runCurrent()
        sending.await()

        val stopped = async { reporter.stop() }
        advanceTimeBy(DIOGEN_REPORT_DRAIN_TIMEOUT)
        runCurrent()

        assertThat(stopped.isCompleted).isTrue()
        assertThat(reporter.statistics).satisfies({ statistics ->
                                                    assertThat(statistics.cancellations.get()).isEqualTo(1)
                                                    assertThat(statistics.failures.get()).isZero()
                                                  })
      }
    }

    assertThat(warnings.map { it.first })
      .anyMatch { "cancelling" in it }
      .noneMatch { "returned FAILED" in it }
  }

  private fun successfulSubmission(): SubmittedReportInfo = SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE)

  /** Mirrors `ITNReporter.submitAutomated`: every [Exception], a cancellation included, becomes a failed submission. */
  private suspend fun submittedLikeItnReporter(post: suspend () -> Unit): SubmittedReportInfo =
    try {
      post()
      successfulSubmission()
    }
    catch (@Suppress("unused") e: Exception) {
      SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED)
    }
}
