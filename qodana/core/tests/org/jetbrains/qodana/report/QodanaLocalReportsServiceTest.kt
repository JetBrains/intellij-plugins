package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.reinstansiateService

class QodanaLocalReportsServiceTest : QodanaPluginLightTestBase() {
  override fun setUp() {
    super.setUp()
    reinstansiateService(project, QodanaLocalReportsService(project))
  }

  private val localReportsService get() = QodanaLocalReportsService.getInstance(project)

  fun `test add available report`() {
    val report = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report)

    assertEquals(setOf(report), localReportsService.getReports())
  }

  fun `test add unavailable report`() {
    val report = ReportDescriptorMock("report1", _isReportAvailable = false)
    localReportsService.addReport(report)

    assertEmpty(localReportsService.getReports())
  }

  fun `test add available report then becomes unavailable`() {
    val report = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report)
    report._isReportAvailable = false

    assertEmpty(localReportsService.getReports())
  }

  fun `test add available report1 add available report2`() {
    val report1 = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report1)

    val report2 = ReportDescriptorMock("report2", _isReportAvailable = true)
    localReportsService.addReport(report2)

    assertEquals(setOf(report1, report2), localReportsService.getReports())
  }

  fun `test add available report1 add unavailable report2`() {
    val report1 = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report1)

    val report2 = ReportDescriptorMock("report2", _isReportAvailable = false)
    localReportsService.addReport(report2)

    assertEquals(setOf(report1), localReportsService.getReports())
  }

  fun `test add available report1 add available report2 then report1 becomes unavailable`() {
    val report1 = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report1)

    val report2 = ReportDescriptorMock("report2", _isReportAvailable = true)
    localReportsService.addReport(report2)

    assertEquals(setOf(report1, report2), localReportsService.getReports())

    report1._isReportAvailable = false

    assertEquals(setOf(report2), localReportsService.getReports())
  }

  private class ReportDescriptorMock(private val id: String, var _isReportAvailable: Boolean) : LocalReportDescriptor {

    override val isReportAvailableFlow: Flow<NotificationCallback?> = emptyFlow()

    override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()

    override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

    override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = emptyFlow()

    override suspend fun refreshReport(): ReportDescriptor = error("must not be invoked")

    override fun checkAvailability(): Boolean {
      return _isReportAvailable
    }

    override fun markAsUnavailable() {
    }

    override fun getName(highlightedState: LocalReportDescriptor.HighlightedState) = id

    override suspend fun loadReport(project: Project) = error("Local reports service must not attempt to load the report")

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
      if (other !is ReportDescriptorMock) return false

      return this.id === other.id
    }

    override fun toString(): String = id
  }
}