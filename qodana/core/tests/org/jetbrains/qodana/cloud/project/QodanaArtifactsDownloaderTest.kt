package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.extensions.LoadingOrder
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.assertNoNotifications
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.reinstansiateService
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.runDispatchingOnUi
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.invariantSeparatorsPathString

class QodanaArtifactsDownloaderTest : QodanaPluginLightTestBase() {
  private lateinit var authorized: UserState.Authorized

  private val reportDownloader get() = QodanaReportDownloader.getInstance(project)

  private val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "core", "test-data")
  private val testDataBasePath: Path get() = Path.of(javaClass.simpleName, getTestName(true).trim())
  private val projectId = "projectIdValid1"

  override fun runInDispatchThread() = false

  override fun setUp() {
    super.setUp()
    runDispatchingOnUi {
      reinstansiateService(project, QodanaReportDownloader(project, scope))
      reportDownloader.noStateLoaded()

      authorized = doInitialTransitionToAuthorized(testRootDisposable)
      mockQDCloudHttpClient.apply {
        respondReportFiles("*") { files, _ ->
          val fileName = files.single()
          val path = testData.resolve(testDataBasePath).resolve(fileName)
          @Language("JSON")
          val response = """
            {
              "files": [
                {
                  "file": "$fileName",
                  "url": "file://${path.invariantSeparatorsPathString}"
                }
              ]
            }
          """.trimIndent()
          qodanaCloudResponse {
            response
          }
        }
        respond("projects/*") {
          @Language("JSON")
          val response = """
            {
              "id": "project_id",
              "organizationId": "org.id",
              "name": "project.name"
            }
          """.trimIndent()
          qodanaCloudResponse {
            response
          }
        }
      }
    }
    ReportMetadataArtifactProvider.EP_NAME.point.registerExtension(
      TestArtifact("one", "folder/test.artifact", "testArtifact"),
      LoadingOrder.FIRST,
      testRootDisposable
    )
    ReportMetadataArtifactProvider.EP_NAME.point.registerExtension(
      TestArtifact("two", "test.artifact", "testArtifact2"),
      LoadingOrder.FIRST,
      testRootDisposable
    )
  }

  fun `test all artifacts loaded`(): Unit = runDispatchingOnUi {
    assertNoNotifications {
      val report = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)
      assertThat(report).isNotNull
      assertThat(report!!.aggregatedReportMetadata.map).hasSize(2)
    }
  }

  fun `test one artifact is missing`(): Unit = runDispatchingOnUi {
    assertNoNotifications {
      val report = reportDownloader.getReport(authorized, "2", projectId, doDownload = true)
      assertThat(report).isNotNull
      assertThat(report!!.aggregatedReportMetadata.map).hasSize(1)
    }
  }

  private data class TestArtifact(override val name: String,
                                  override val fileName: String,
                                  override val presentableFileName: String): ReportMetadataArtifactProvider {
    override suspend fun readReport(path: Path): ReportMetadata {
      return object : ReportMetadata {
        override val id: String
          get() = name
      }
    }
  }
}