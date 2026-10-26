package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.util.io.copy
import com.intellij.util.io.delete
import com.jetbrains.qodana.sarif.SarifUtil
import kotlinx.coroutines.cancel
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.assertNoNotifications
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloud.testutils.doInitialTransitionToAuthorized
import org.jetbrains.qodana.cloud.testutils.respondReportFiles
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.reinstansiateService
import org.jetbrains.qodana.runDispatchingOnUi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.invariantSeparatorsPathString

@TestApplication
class QodanaReportDownloaderTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private lateinit var authorized: UserState.Authorized

  private val reportDownloader get() = QodanaReportDownloader.getInstance(qodanaFixture.project)

  private val requestsCount = mutableMapOf<String, Int>()

  @BeforeEach
  fun setUp() {
    runDispatchingOnUi {
      mockQDCloudHttpClient.respond("projects/*") {
        @Language("JSON")
        val response = """
          {
            "id": "project.id",
            "organizationId": "org.id",
            "name": "project.name"
          }
        """.trimIndent()
        qodanaCloudResponse {
          response
        }
      }
      qodanaFixture.reinstansiateService(qodanaFixture.project, QodanaReportDownloader(qodanaFixture.project, qodanaFixture.scope))
      reportDownloader.noStateLoaded()

      authorized = qodanaFixture.doInitialTransitionToAuthorized(qodanaFixture.testRootDisposable)
    }
  }

  @AfterEach
  fun tearDown() {
    runDispatchingOnUi {
      val state = reportDownloader.state
      for ((_, savedReportInfo) in state.loadedReportsPaths) {
        val path = savedReportInfo.path ?: continue
        try {
          Paths.get(path).delete()
        }
        catch (ignored: Exception) {
        }
      }
      qodanaFixture.scope.cancel()
    }
  }


  @Test
  fun `test load valid cloud report`(): Unit = runDispatchingOnUi {
    val reportPath = getTmpPath(qodanaFixture.sarifTestReports.valid1, "tmp1")
    val projectId = "projectIdValid1"
    val actualReport = SarifUtil.readReport(reportPath)
    mockReportDownloadPath(reportPath, projectId)

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertThat(requestsCount[projectId]).isEqualTo(1)
  }

  @Test
  fun `test double load one request`(): Unit = runDispatchingOnUi {
    val reportPath = getTmpPath(qodanaFixture.sarifTestReports.valid1, "tmp1")
    val projectId = "projectIdValid1"
    val actualReport = SarifUtil.readReport(reportPath)
    mockReportDownloadPath(reportPath, projectId)

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertThat(requestsCount[projectId]).isEqualTo(1)
  }

  @Test
  fun `test double load different reports two requests`(): Unit = runDispatchingOnUi {
    val reportPath1 = getTmpPath(qodanaFixture.sarifTestReports.valid1, "tmp1")
    val projectId = "projectIdValid1"
    val actualReport1 = SarifUtil.readReport(reportPath1)
    mockReportDownloadPath(reportPath1, projectId)

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport1)
    }

    val reportPath2 = getTmpPath(qodanaFixture.sarifTestReports.valid2, "tmp2")
    val actualReport2 = SarifUtil.readReport(reportPath2)
    mockReportDownloadPath(reportPath2, projectId)

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "2", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport2)
    }

    assertThat(requestsCount[projectId]).isEqualTo(2)
  }

  @Test
  fun `test double load different projects two requests`(): Unit = runDispatchingOnUi {
    val reportPath1 = getTmpPath(qodanaFixture.sarifTestReports.valid1, "tmp1")
    val projectId1 = "projectIdValid1"
    val actualReport = SarifUtil.readReport(reportPath1)
    mockReportDownloadPath(reportPath1, projectId1)

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId1, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    val reportPath2 = getTmpPath(qodanaFixture.sarifTestReports.valid1, "tmp1")
    val projectId2 = "projectIdValid2"
    mockReportDownloadPath(reportPath2, projectId2)

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId2, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertThat(requestsCount[projectId1]).isEqualTo(1)
    assertThat(requestsCount[projectId2]).isEqualTo(1)
  }

  @Test
  fun `test load different projects no requests`(): Unit = runDispatchingOnUi {
    val reportPath1 = getTmpPath(qodanaFixture.sarifTestReports.valid1, "tmp1")
    val projectId1 = "projectIdValid1"
    val reportPath2 = getTmpPath(qodanaFixture.sarifTestReports.valid1, "tmp1")
    val projectId2 = "projectIdValid2"

    mockReportDownloadPath(reportPath1, projectId1)
    val actualReport = SarifUtil.readReport(reportPath1)

    reportDownloader.loadState(ReportDownloaderState().apply {
      loadedReportsPaths = mutableMapOf(
        Pair(
          projectId1, DownloadedReportInfo().apply {
          reportId = "1"
          path = reportPath1.toString()
        }),
        Pair(
          projectId2, DownloadedReportInfo().apply {
          reportId = "2"
          path = reportPath2.toString()
        })
      )
    })
    dispatchAllTasksOnUi()

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId1, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    mockReportDownloadPath(reportPath2, projectId2)

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId2, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertTrue(projectId1 !in requestsCount)
    assertThat(requestsCount[projectId2]).isEqualTo(1)
  }

  @Test
  fun `test add published request`(): Unit = runDispatchingOnUi {
    val reportPath = getTmpPath(qodanaFixture.sarifTestReports.valid1, "tmp1")
    val projectId = "projectIdValid1"

    mockReportDownloadPath(reportPath, projectId)
    val actualReport = SarifUtil.readReport(reportPath)
    qodanaFixture.assertNoNotifications {
      reportDownloader.addPublishedReport(reportPath, projectId, "1")
    }

    qodanaFixture.assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertTrue(projectId !in requestsCount)
  }

  private fun mockReportDownloadPath(reportPath: Path, projectId: String) {
    mockQDCloudHttpClient.respondReportFiles("*") { files, _ ->
      if (files.single() == "qodana.sarif.json") {
        requestsCount[projectId] = requestsCount.getOrDefault(projectId, 0) + 1
      }
      @Language("JSON")
      val response = """
        {
          "files": [
            {
              "file": "${files.single()}",
              "url": "file://${reportPath.invariantSeparatorsPathString}"
            }
          ]
        }
      """.trimIndent()
      qodanaCloudResponse {
        response
      }
    }
  }

  /**
   * Inside [QodanaReportDownloader] we may delete some reports. Because of inner method, which is responsible for reports downloading,
   * already existing files are not copied to tmp directories, so we need to create temporary copies for them.
   * Use this function each time you want to mock downloading report.
   */
  private fun getTmpPath(reportPath: Path, tmpName: String): Path {
    val tmpFile = FileUtil.createTempFile(tmpName, null, true).toPath()
    return reportPath.copy(tmpFile)
  }
}