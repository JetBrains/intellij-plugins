package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.copy
import com.intellij.util.io.delete
import com.jetbrains.qodana.sarif.SarifUtil
import kotlinx.coroutines.cancel
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.*
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.invariantSeparatorsPathString

class QodanaReportDownloaderTest : QodanaPluginLightTestBase() {
  private lateinit var authorized: UserState.Authorized

  private val reportDownloader get() = QodanaReportDownloader.getInstance(project)

  private val requestsCount = mutableMapOf<String, Int>()

  override fun runInDispatchThread() = false

  override fun setUp() {
    super.setUp()
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
      reinstansiateService(project, QodanaReportDownloader(project, scope))
      reportDownloader.noStateLoaded()

      authorized = doInitialTransitionToAuthorized(testRootDisposable)
    }
  }

  override fun tearDown() {
    try {
      runDispatchingOnUi {
        val state = reportDownloader.state
        for ((_, savedReportInfo) in state.loadedReportsPaths) {
          val path = savedReportInfo.path ?: continue
          try {
            Paths.get(path).delete()
          } catch (ignored: Exception) {}
        }
        scope.cancel()
      }
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }


  fun `test load valid cloud report`() = runDispatchingOnUi {
    val reportPath = getTmpPath(sarifTestReports.valid1, "tmp1")
    val projectId = "projectIdValid1"
    val actualReport = SarifUtil.readReport(reportPath)
    mockReportDownloadPath(reportPath, projectId)

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertThat(requestsCount[projectId]).isEqualTo(1)
  }

  fun `test double load one request`() = runDispatchingOnUi {
    val reportPath = getTmpPath(sarifTestReports.valid1, "tmp1")
    val projectId = "projectIdValid1"
    val actualReport = SarifUtil.readReport(reportPath)
    mockReportDownloadPath(reportPath, projectId)

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertThat(requestsCount[projectId]).isEqualTo(1)
  }

  fun `test double load different reports two requests`() = runDispatchingOnUi {
    val reportPath1 = getTmpPath(sarifTestReports.valid1, "tmp1")
    val projectId = "projectIdValid1"
    val actualReport1 = SarifUtil.readReport(reportPath1)
    mockReportDownloadPath(reportPath1, projectId)

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport1)
    }

    val reportPath2 = getTmpPath(sarifTestReports.valid2, "tmp2")
    val actualReport2 = SarifUtil.readReport(reportPath2)
    mockReportDownloadPath(reportPath2, projectId)

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "2", projectId, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport2)
    }

    assertThat(requestsCount[projectId]).isEqualTo(2)
  }

  fun `test double load different projects two requests`() = runDispatchingOnUi {
    val reportPath1 = getTmpPath(sarifTestReports.valid1, "tmp1")
    val projectId1 = "projectIdValid1"
    val actualReport = SarifUtil.readReport(reportPath1)
    mockReportDownloadPath(reportPath1, projectId1)

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId1, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    val reportPath2 = getTmpPath(sarifTestReports.valid1, "tmp1")
    val projectId2 = "projectIdValid2"
    mockReportDownloadPath(reportPath2, projectId2)

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId2, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertThat(requestsCount[projectId1]).isEqualTo(1)
    assertThat(requestsCount[projectId2]).isEqualTo(1)
  }

  fun `test load different projects no requests`() = runDispatchingOnUi{
    val reportPath1 = getTmpPath(sarifTestReports.valid1, "tmp1")
    val projectId1 = "projectIdValid1"
    val reportPath2 = getTmpPath(sarifTestReports.valid1, "tmp1")
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

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId1, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    mockReportDownloadPath(reportPath2, projectId2)

    assertNoNotifications {
      val loadedSarif = reportDownloader.getReport(authorized, "1", projectId2, doDownload = true)?.validatedSarif?.sarif
      assertThat(loadedSarif).isEqualTo(actualReport)
    }

    assertTrue(projectId1 !in requestsCount)
    assertThat(requestsCount[projectId2]).isEqualTo(1)
  }

  fun `test add published request`() = runDispatchingOnUi {
    val reportPath = getTmpPath(sarifTestReports.valid1, "tmp1")
    val projectId = "projectIdValid1"

    mockReportDownloadPath(reportPath, projectId)
    val actualReport = SarifUtil.readReport(reportPath)
    assertNoNotifications {
      reportDownloader.addPublishedReport(reportPath, projectId, "1")
    }

    assertNoNotifications {
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
  private fun getTmpPath(reportPath:Path, tmpName: String): Path {
    val tmpFile = FileUtil.createTempFile(tmpName, null, true).toPath()
    return reportPath.copy(tmpFile)
  }
}