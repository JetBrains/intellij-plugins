package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.SarifUtil
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.*
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.MockQDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudException
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.report.LoadedReport
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

class LinkedCloudReportDescriptorTest : QodanaPluginLightTestBase() {
  private lateinit var authorized: UserState.Authorized
  private lateinit var linked: LinkState.Linked

  override fun runInDispatchThread() = false

  override fun setUp() {
    super.setUp()
    runDispatchingOnUi {
      reinstansiateService(project, QodanaCloudProjectLinkService(project, scope))

      authorized = doInitialTransitionToAuthorized(testRootDisposable)
      val projectId = "cloud_project_id"
      linked = doTransitionToLinked(project, authorized, projectId, "organization_id")

      mockQDCloudHttpClient.respond("projects/$projectId/timeline") {
        @Language("JSON")
        val response = """
          {
            "items": [
              {
                "reportId": "report_id"
              }
            ] 
          }
        """.trimIndent()
        qodanaCloudResponse {
          response
        }
      }
    }
  }

  fun `test load valid cloud report`() = runDispatchingOnUi {
    val reportPath = sarifTestReports.valid1
    mockQDCloudHttpClient.respondQodanaSarifJson(reportPath)

    val actualReport = SarifUtil.readReport(reportPath)
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()

    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      assertNoNotifications {
        val loadedSarif = reportDescriptor.loadSarifReport(project)?.validatedSarif?.sarif
        assertThat(loadedSarif).isEqualTo(actualReport)
      }
    }
  }

  fun `test load report cloud response error`() = runDispatchingOnUi {
    mockQDCloudHttpClient.respond("reports/*/files") {
      QDCloudResponse.Error.ResponseFailure(QDCloudException.Error("expected tests error", 404))
    }
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()

    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      assertSingleNotificationWithMessage(
        "Status code: 404 expected tests error"
      ) {
        val loadedReport = reportDescriptor.loadSarifReport(project)
        assertNull(loadedReport)
      }
    }
  }

  fun `test load report cloud offline`() = runDispatchingOnUi {
    mockQDCloudHttpClient.respond("reports/*/files") {
      QDCloudResponse.Error.Offline(QDCloudException.Offline())
    }
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()

    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      assertSingleNotificationWithMessage(LinkedCloudReportDescriptor.FailMessagesProvider.getQodanaCloudOfflineMessage()) {
        val loadedReport = reportDescriptor.loadSarifReport(project)
        assertNull(loadedReport)
      }
    }
  }

  fun `test load report sarif no files`() = runDispatchingOnUi {
    mockQDCloudHttpClient.respond("reports/*/files") {
      @Language("JSON")
      val response = """
      {
        "files": []
      }
    """.trimIndent()
      qodanaCloudResponse {
        response
      }
    }
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()

    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      assertSingleNotificationWithMessage(LinkedCloudReportDescriptor.FailMessagesProvider.getCloudReportNotFoundMessage()) {
        val loadedReport = reportDescriptor.loadSarifReport(project)
        assertNull(loadedReport)
      }
    }
  }

  fun `test not available when not linked`() = runDispatchingOnUi {
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()

    assertReportIsAvailableSignalsCount(reportDescriptor, 1) {
      linked.unlink()
    }
  }
}

fun MockQDCloudHttpClient.respondQodanaSarifJson(reportPath: Path) {
  @Suppress("JsonStandardCompliance")
  val fileUrl = "file://${reportPath.invariantSeparatorsPathString}"
  respondReportFiles("*") { _, _ ->
    @Language("JSON")
    val response = """
      {
        "files": [
          {
            "file": "qodana.sarif.json",
            "url": "$fileUrl"
          }
        ]
      }
    """.trimIndent()
    qodanaCloudResponse {
      response
    }
  }
}

suspend fun LinkedLatestCloudReportDescriptor.loadSarifReport(project: Project): LoadedReport.Sarif? {
  return loadReport(project)?.reportDescriptorDelegate?.loadReport(project) as? LoadedReport.Sarif
}
