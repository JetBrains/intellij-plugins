package org.jetbrains.qodana.highlight

import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.report.*
import java.nio.file.Path

class QodanaHighlightedReportServiceTest : QodanaPluginLightTestBase() {
  private val report1 by lazy { reportTestControllerFromSarifFile(sarifTestReports.valid1) }
  private val report2 by lazy { reportTestControllerFromSarifFile(sarifTestReports.valid2) }

  private val reportFailedLoading by lazy {
    ReportTestController(ReportDescriptorMock("failed"), sarifReportToLoad = null, sarifProblems = emptySet())
  }

  private val highlightedReportService get() = QodanaHighlightedReportService.getInstance(project)

  override fun setUp() {
    super.setUp()
    reinstansiateService(project, QodanaHighlightedReportService(project, scope))
  }

  override fun runInDispatchThread(): Boolean = false

  fun `test highlight report`() = runDispatchingOnUi {
    assertNoReportHighlighted()

    report1.submitHighlightLoading()

    dispatchAllTasksOnUi()

    report1.assertIsLoading()

    report1.submitCompleteLoading()

    report1.assertIsHighlighted()
    report1.assertNotAvailableNotificationWasTriggered(times = 0)
  }

  fun `test highlight report start loading unhighlight while loading`() = runDispatchingOnUi {
    report1.submitHighlightLoading()

    report1.assertIsLoading()

    submitUnhighlightReport()

    assertNoReportHighlighted()
    report1.assertNotAvailableNotificationWasTriggered(times = 0)
  }

  fun `test highlight and unhighlight report`() = runDispatchingOnUi {
    report1.completeAndSubmitHighlight()

    report1.assertIsHighlighted()

    submitUnhighlightReport()

    assertNoReportHighlighted()
    report1.assertNotAvailableNotificationWasTriggered(times = 0)
  }

  fun `test highlight not available report`() = runDispatchingOnUi {
    report1.isAvailable = false
    report1.completeAndSubmitHighlight()

    assertNoReportHighlighted()
    report1.assertNotAvailableNotificationWasTriggered(times = 1)
  }

  fun `test highlight then make not available`() = runDispatchingOnUi {
    report1.completeAndSubmitHighlight()

    report1.assertIsHighlighted()

    report1.isAvailable = false

    assertNoReportHighlighted()
    report1.assertNotAvailableNotificationWasTriggered(times = 1)
  }

  fun `test highlight report fail loading`() = runDispatchingOnUi {
    reportFailedLoading.submitHighlightLoading()

    reportFailedLoading.assertIsLoading()

    reportFailedLoading.submitCompleteLoading()

    assertNoReportHighlighted()
    reportFailedLoading.assertNotAvailableNotificationWasTriggered(times = 0)
  }

  fun `test highlight report highlight it again`() = runDispatchingOnUi {
    report1.completeAndSubmitHighlight()

    report1.assertIsHighlighted()

    val reportDescriptorEqualInstance = ReportDescriptorMock(id = report1.reportDescriptor.id)
    highlightedReportService.highlightReport(reportDescriptorEqualInstance)

    report1.assertIsHighlighted()
  }

  fun `test highlight report1 highlight report2`() = runDispatchingOnUi {
    report1.completeAndSubmitHighlight()

    report1.assertIsHighlighted()

    report2.submitHighlightLoading()

    report2.assertIsLoading()

    report2.submitCompleteLoading()

    report2.assertIsHighlighted()
    report2.assertNotAvailableNotificationWasTriggered(times = 0)
    report1.assertNotAvailableNotificationWasTriggered(times = 0)
  }

  fun `test highlight report1 highlight report2 while loading report1`() = runDispatchingOnUi {
    report1.submitHighlightLoading()

    report1.assertIsLoading()

    report2.submitHighlightLoading()

    report2.assertIsLoading()

    report2.submitCompleteLoading()

    report2.assertIsHighlighted()
    report2.assertNotAvailableNotificationWasTriggered(times = 0)
    report1.assertNotAvailableNotificationWasTriggered(times = 0)
  }

  fun `test try highlight report1 which throws exception on load then success highlight report2`() = runDispatchingOnUi {
    report1.submitHighlightLoading()

    report1.assertIsLoading()

    val handledExceptions = allowExceptions {
      report1.submitCompleteLoadingExceptionally()
      assertNoReportHighlighted()
    }

    assertThat(handledExceptions.size).isEqualTo(1)
    assertThat(handledExceptions[0].message).isEqualTo("Expected fail of loading report in tests")

    report2.submitHighlightLoading()

    report2.submitCompleteLoading()

    report2.assertIsHighlighted()
    report2.assertNotAvailableNotificationWasTriggered(times = 0)
    report1.assertNotAvailableNotificationWasTriggered(times = 0)
  }

  fun `test highlighted result`() = runDispatchingOnUi {
    val result = report1.submitHighlightLoading()
    report1.submitCompleteLoading()

    val resultAwaited = result.await()
    assertThat(resultAwaited).isNotNull

    assertThat(resultAwaited!!.highlightedReportData.sourceReportDescriptor == report1.reportDescriptor)
  }

  fun `test highlighted result after error`() = runDispatchingOnUi {
    val result = report1.submitHighlightLoading()

    val handledExceptions = allowExceptions {
      report1.submitCompleteLoadingExceptionally()
      assertNoReportHighlighted()
    }

    assertThat(handledExceptions.size).isEqualTo(1)

    val resultAwaited = result.await()
    assertThat(resultAwaited).isNull()
  }

  fun `test highlighted result two reports`() = runDispatchingOnUi {
    val result1 = report1.submitHighlightLoading()
    report1.submitCompleteLoading()
    dispatchAllTasksOnUi()

    val result2 = report2.submitHighlightLoading()
    report2.submitCompleteLoading()
    dispatchAllTasksOnUi()

    val resultAwaited1 = result1.await()
    assertThat(resultAwaited1).isNotNull
    assertThat(resultAwaited1?.highlightedReportData?.sourceReportDescriptor).isEqualTo(report1.reportDescriptor)

    val resultAwaited2 = result2.await()
    assertThat(resultAwaited2).isNotNull
    assertThat(resultAwaited2?.highlightedReportData?.sourceReportDescriptor).isEqualTo(report2.reportDescriptor)
  }

  fun `test highlighted result already highlighted`() = runDispatchingOnUi {
    val result1 = report1.submitHighlightLoading()
    report1.submitCompleteLoading()
    dispatchAllTasksOnUi()

    val result2 = report1.submitHighlightLoading()
    report1.assertIsHighlighted()

    val resultAwaited1 = result1.await()
    assertThat(resultAwaited1).isNotNull
    assertThat(resultAwaited1?.highlightedReportData?.sourceReportDescriptor).isEqualTo(report1.reportDescriptor)

    val resultAwaited2 = result2.await()
    assertThat(resultAwaited2).isNotNull
    assertThat(resultAwaited2?.highlightedReportData?.sourceReportDescriptor).isEqualTo(report1.reportDescriptor)
  }

  fun `test highlighted result report2 right after report1`() = runDispatchingOnUi {
    val result1 = report1.submitHighlightLoading()

    val result2 = report2.submitHighlightLoading()
    report1.submitCompleteLoading()
    report2.submitCompleteLoading()

    val resultAwaited1 = result1.await()
    assertThat(resultAwaited1).isNull()

    val resultAwaited2 = result2.await()
    assertThat(resultAwaited2).isNotNull
    assertThat(resultAwaited2?.highlightedReportData?.sourceReportDescriptor).isEqualTo(report2.reportDescriptor)
  }

  fun `test highlighted result after unhighlight`() = runDispatchingOnUi {
    report1.submitHighlightLoading()
    report1.submitCompleteLoading()

    val result = submitUnhighlightReport()
    assertThat(result.await()).isNull()
  }

  fun `test highlight requests processed`() = runDispatchingOnUi {
    val result1 = report1.submitHighlightLoading()
    val result2 = report1.submitHighlightLoading()
    val result3 = report1.submitHighlightLoading()
    report1.submitCompleteLoading()

    assertThat(result1.await()).isNull()
    assertThat(result2.await()).isNull()
    assertThat(result3.await()).isNotNull
  }

  fun `test highlight requests processed different reports`() = runDispatchingOnUi {
    val result1 = report1.submitHighlightLoading()
    val result2 = report2.submitHighlightLoading()
    val result3 = report1.submitHighlightLoading()
    report1.submitCompleteLoading()

    assertThat(result1.await()).isNull()
    assertThat(result2.await()).isNull()
    assertThat(result3.await()).isNotNull
  }

  fun `test highlight requests processed different reports second loaded`() = runDispatchingOnUi {
    val result1 = report1.submitHighlightLoading()
    val result2 = report2.submitHighlightLoading()
    report2.submitCompleteLoading()
    dispatchAllTasksOnUi()

    val result3 = report1.submitHighlightLoading()
    report1.submitCompleteLoading()

    assertThat(result1.await()).isNull()
    assertThat(result2.await()).isNotNull
    assertThat(result3.await()).isNotNull
    report1.assertIsHighlighted()
  }

  fun `test highlight requests processed several reports all cancelled`() = runDispatchingOnUi {
    val result1 = report1.submitHighlightLoading()
    val result2 = report2.submitHighlightLoading()
    val result3 = report1.submitHighlightLoading()
    val result4 = submitUnhighlightReport()

    assertThat(result1.await()).isNull()
    assertThat(result2.await()).isNull()
    assertThat(result3.await()).isNull()
    assertThat(result4.await()).isNull()
  }

  fun `test highlight requests processed several reports cancelled first`() = runDispatchingOnUi {
    val result1 = report1.submitHighlightLoading()
    val result2 = report2.submitHighlightLoading()
    val result3 = submitUnhighlightReport()
    val result4 = report1.submitHighlightLoading()
    report1.submitCompleteLoading()

    assertThat(result1.await()).isNull()
    assertThat(result2.await()).isNull()
    assertThat(result3.await()).isNull()
    assertThat(result4.await()).isNotNull
  }

  private fun reportTestControllerFromSarifFile(path: Path): ReportTestController {
    val sarif = SarifUtil.readReport(path)

    // TODO – switch to manual construction of SarifProblems here
    val problems = SarifProblem.fromReport(project, ValidatedSarif(sarif))

    val reportDescriptor = ReportDescriptorMock(path.fileName.toString())
    return ReportTestController(reportDescriptor, sarif, problems.toSet())
  }

  private inner class ReportTestController(
    val reportDescriptor: ReportDescriptorMock,
    val sarifReportToLoad: SarifReport?,
    val sarifProblems: Set<SarifProblem>
  ) {
    fun submitHighlightLoading(): Deferred<HighlightedReportState.Selected?> {
      val result = CompletableDeferred<HighlightedReportState.Selected?>()
      scope.launch(QodanaDispatchers.Default) {
        result.complete(highlightedReportService.highlightReport(reportDescriptor))
      }
      return result
    }

    fun submitCompleteLoading() = reportDescriptor.completeLoadingWith(sarifReportToLoad)

    fun submitCompleteLoadingExceptionally() = reportDescriptor.completeLoadingExceptionally(Exception("Expected fail of loading report in tests"))

    fun completeAndSubmitHighlight() {
      submitCompleteLoading()
      submitHighlightLoading()
    }

    var isAvailable: Boolean
      get() = reportDescriptor.isAvailable
      set(value) {
        reportDescriptor.isAvailable = value
      }

    fun assertIsLoading() {
      dispatchAllTasksOnUi()
      assertThat((highlightedReportService.highlightedReportState.value as HighlightedReportState.Loading).sourceReportDescriptor)
        .isEqualTo(reportDescriptor)
    }

    fun assertIsHighlighted() {
      dispatchAllTasksOnUi()
      val highlightedReportData = highlightedReportService.highlightedReportState.value.highlightedReportDataIfSelected!!
      assertThat(highlightedReportData.sourceReportDescriptor).isEqualTo(reportDescriptor)
      assertThat(highlightedReportData.allProblems).isEqualTo(sarifProblems)
    }

    fun assertNotAvailableNotificationWasTriggered(times: Int) {
      dispatchAllTasksOnUi()
      assertThat(reportDescriptor.timesNotAvailableNotificationWasTriggered).isEqualTo(times)
    }
  }

  private suspend fun submitUnhighlightReport(): Deferred<HighlightedReportState.Selected?> {
    val result = CompletableDeferred<HighlightedReportState.Selected?>()
    scope.launch(QodanaDispatchers.Default) {
      result.complete(highlightedReportService.highlightReport(null))
    }
    return result
  }

  private fun assertNoReportHighlighted() {
    dispatchAllTasksOnUi()
    assertTrue(highlightedReportService.highlightedReportState.value is HighlightedReportState.NotSelected)
  }

  private class ReportDescriptorMock(val id: String, _isAvailable: Boolean = true) : ReportDescriptor {
    private val reportDeferred = CompletableDeferred<SarifReport?>()

    var timesNotAvailableNotificationWasTriggered = 0
      private set

    override suspend fun refreshReport(): ReportDescriptor? = error("must not be invoked")

    fun completeLoadingWith(report: SarifReport?) {
      reportDeferred.complete(report)
    }

    fun completeLoadingExceptionally(exception: Throwable) {
      reportDeferred.completeExceptionally(exception)
    }

    private val _isAvailableFlow: MutableSharedFlow<NotificationCallback?> = MutableSharedFlow(replay = 1)
    var isAvailable = _isAvailable
      set(value) {
        field = value
        if (!value) {
          _isAvailableFlow.tryEmit(null)
          timesNotAvailableNotificationWasTriggered += 1
        }
      }

    override val isReportAvailableFlow: Flow<NotificationCallback?>
      get() = _isAvailableFlow

    override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()

    override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

    override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = emptyFlow()

    override suspend fun loadReport(project: Project): LoadedReport.Sarif? = reportDeferred.await()?.let { LoadedReport.Sarif(ValidatedSarif(it), AggregatedReportMetadata(emptyMap()), "") }

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
      if (other !is ReportDescriptorMock) return false

      return id == other.id
    }

    override fun toString(): String = id
  }
}