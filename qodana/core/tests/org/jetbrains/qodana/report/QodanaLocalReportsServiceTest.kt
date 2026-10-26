package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.reinstansiateService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class QodanaLocalReportsServiceTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  @BeforeEach
  fun setUp() {
    qodanaFixture.reinstansiateService(qodanaFixture.project, QodanaLocalReportsService(qodanaFixture.project))
  }

  private val localReportsService get() = QodanaLocalReportsService.getInstance(qodanaFixture.project)

  @Test
  fun `test add available report`() {
    val report = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report)

    assertEquals(setOf(report), localReportsService.getReports())
  }

  @Test
  fun `test add unavailable report`() {
    val report = ReportDescriptorMock("report1", _isReportAvailable = false)
    localReportsService.addReport(report)

    assertThat(localReportsService.getReports()).isEmpty()
  }

  @Test
  fun `test add available report then becomes unavailable`() {
    val report = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report)
    report._isReportAvailable = false

    assertThat(localReportsService.getReports()).isEmpty()
  }

  @Test
  fun `test add available report1 add available report2`() {
    val report1 = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report1)

    val report2 = ReportDescriptorMock("report2", _isReportAvailable = true)
    localReportsService.addReport(report2)

    assertEquals(setOf(report1, report2), localReportsService.getReports())
  }

  @Test
  fun `test add available report1 add unavailable report2`() {
    val report1 = ReportDescriptorMock("report1", _isReportAvailable = true)
    localReportsService.addReport(report1)

    val report2 = ReportDescriptorMock("report2", _isReportAvailable = false)
    localReportsService.addReport(report2)

    assertEquals(setOf(report1), localReportsService.getReports())
  }

  @Test
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