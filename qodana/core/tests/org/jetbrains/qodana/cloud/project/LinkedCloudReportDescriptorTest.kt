package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.project.Project
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.qodana.sarif.SarifUtil
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.assertNoNotifications
import org.jetbrains.qodana.assertReportIsAvailableSignalsCount
import org.jetbrains.qodana.assertSingleNotificationWithMessage
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloud.testutils.doInitialTransitionToAuthorized
import org.jetbrains.qodana.cloud.testutils.doTransitionToLinked
import org.jetbrains.qodana.cloud.testutils.respondReportFiles
import org.jetbrains.qodana.cloudclient.MockQDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudException
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.reinstansiateService
import org.jetbrains.qodana.report.LoadedReport
import org.jetbrains.qodana.runDispatchingOnUi
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

@TestApplication
class LinkedCloudReportDescriptorTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private lateinit var authorized: UserState.Authorized
  private lateinit var linked: LinkState.Linked

  @BeforeEach
  fun setUp() {
    runDispatchingOnUi {
      qodanaFixture.reinstansiateService(qodanaFixture.project, QodanaCloudProjectLinkService(qodanaFixture.project, qodanaFixture.scope))

      authorized = qodanaFixture.doInitialTransitionToAuthorized(qodanaFixture.testRootDisposable)
      val projectId = "cloud_project_id"
      linked = doTransitionToLinked(qodanaFixture.project, authorized, projectId, "organization_id")

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

  @Test
  fun `test load valid cloud report`(): Unit = runDispatchingOnUi {
    val reportPath = qodanaFixture.sarifTestReports.valid1
    mockQDCloudHttpClient.respondQodanaSarifJson(reportPath)

    val actualReport = SarifUtil.readReport(reportPath)
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()

    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      qodanaFixture.assertNoNotifications {
        val loadedSarif = reportDescriptor.loadSarifReport(qodanaFixture.project)?.validatedSarif?.sarif
        assertThat(loadedSarif).isEqualTo(actualReport)
      }
    }
  }

  @Test
  fun `test load report cloud response error`(): Unit = runDispatchingOnUi {
    mockQDCloudHttpClient.respond("reports/*/files") {
      QDCloudResponse.Error.ResponseFailure(QDCloudException.Error("expected tests error", 404))
    }
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()

    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      qodanaFixture.assertSingleNotificationWithMessage(
        "Status code: 404 expected tests error"
      ) {
        val loadedReport = reportDescriptor.loadSarifReport(qodanaFixture.project)
        assertNull(loadedReport)
      }
    }
  }

  @Test
  fun `test load report cloud offline`(): Unit = runDispatchingOnUi {
    mockQDCloudHttpClient.respond("reports/*/files") {
      QDCloudResponse.Error.Offline(QDCloudException.Offline())
    }
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()

    assertReportIsAvailableSignalsCount(reportDescriptor, 0) {
      qodanaFixture.assertSingleNotificationWithMessage(LinkedCloudReportDescriptor.FailMessagesProvider.getQodanaCloudOfflineMessage()) {
        val loadedReport = reportDescriptor.loadSarifReport(qodanaFixture.project)
        assertNull(loadedReport)
      }
    }
  }

  @Test
  fun `test load report sarif no files`(): Unit = runDispatchingOnUi {
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
      qodanaFixture.assertSingleNotificationWithMessage(LinkedCloudReportDescriptor.FailMessagesProvider.getCloudReportNotFoundMessage()) {
        val loadedReport = reportDescriptor.loadSarifReport(qodanaFixture.project)
        assertNull(loadedReport)
      }
    }
  }

  @Test
  fun `test not available when not linked`(): Unit = runDispatchingOnUi {
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
