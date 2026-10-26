package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.assertNoNotifications
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloud.testutils.doInitialTransitionToAuthorized
import org.jetbrains.qodana.cloud.testutils.respondReportFiles
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.reinstansiateService
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.runDispatchingOnUi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.invariantSeparatorsPathString

@TestApplication
class QodanaArtifactsDownloaderTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private lateinit var authorized: UserState.Authorized

  private val reportDownloader get() = QodanaReportDownloader.getInstance(qodanaFixture.project)

  private val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "core", "test-data")
  private val testDataBasePath: Path get() = Path.of(javaClass.simpleName, qodanaFixture.getTestName(true).trim())
  private val projectId = "projectIdValid1"

  @BeforeEach
  fun setUp() {
    runDispatchingOnUi {
      qodanaFixture.reinstansiateService(qodanaFixture.project, QodanaReportDownloader(qodanaFixture.project, qodanaFixture.scope))
      reportDownloader.noStateLoaded()

      authorized = qodanaFixture.doInitialTransitionToAuthorized(qodanaFixture.testRootDisposable)
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
      qodanaFixture.testRootDisposable
    )
    ReportMetadataArtifactProvider.EP_NAME.point.registerExtension(
      TestArtifact("two", "test.artifact", "testArtifact2"),
      LoadingOrder.FIRST,
      qodanaFixture.testRootDisposable
    )
  }

  @Test
  fun `test all artifacts loaded`(): Unit = runDispatchingOnUi {
    qodanaFixture.assertNoNotifications {
      val report = reportDownloader.getReport(authorized, "1", projectId, doDownload = true)
      assertThat(report).isNotNull
      assertThat(report!!.aggregatedReportMetadata.map).hasSize(2)
    }
  }

  @Test
  fun `test one artifact is missing`(): Unit = runDispatchingOnUi {
    qodanaFixture.assertNoNotifications {
      val report = reportDownloader.getReport(authorized, "2", projectId, doDownload = true)
      assertThat(report).isNotNull
      assertThat(report!!.aggregatedReportMetadata.map).hasSize(1)
    }
  }

  private data class TestArtifact(override val name: String,
                                  override val fileName: String,
                                  override val presentableFileName: String) : ReportMetadataArtifactProvider {
    override suspend fun readReport(path: Path): ReportMetadata {
      return object : ReportMetadata {
        override val id: String
          get() = name
      }
    }
  }
}