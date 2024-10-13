package org.jetbrains.qodana.cloud.project

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.*
import org.jetbrains.qodana.cloud.RefreshableProperty
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.MockQDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

class QodanaCloudProjectLinkServiceTest : QodanaPluginLightTestBase() {
  private lateinit var authorized: UserState.Authorized

  override fun runInDispatchThread() = false

  private val cloudProjectLinkService get() = QodanaCloudProjectLinkService.getInstance(project)

  private val linkStateValue get() = cloudProjectLinkService.linkState.value

  private val cloudProjectPrimaryData = CloudProjectPrimaryData(
    "project_id",
    CloudOrganizationPrimaryData("cloud_organization")
  )
  private val emptyCloudProjectProperties = CloudProjectProperties(name = null)


  override fun setUp() {
    super.setUp()
    runDispatchingOnUi {
      reinstansiateService(project, QodanaCloudProjectLinkService(project, scope))
      reinstansiateService(project, QodanaHighlightedReportService(project, scope))
      cloudProjectLinkService.refreshedReportTimeoutBeforeNotification = 1.seconds
      authorized = doInitialTransitionToAuthorized(testRootDisposable)
      mockQDCloudHttpClient.apply {
        respond("projects/*") {
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
        respondTimeline {
          awaitCancellation()
        }
      }
    }
  }

  fun `test successfully link`(): Unit = runDispatchingOnUi {
    val notLinked = linkStateValue as LinkState.NotLinked

    notLinked.linkWithQodanaCloudProject(authorized, CloudProjectData(cloudProjectPrimaryData, emptyCloudProjectProperties))

    assertTrue(linkStateValue is LinkState.Linked)
  }

  fun `test successfully link and unlink`(): Unit = runDispatchingOnUi {
    val notLinked = linkStateValue as LinkState.NotLinked
    notLinked.linkWithQodanaCloudProject(authorized, CloudProjectData(cloudProjectPrimaryData, emptyCloudProjectProperties))
    val linked = linkStateValue as LinkState.Linked

    linked.unlink()

    assertTrue(linkStateValue is LinkState.NotLinked)
  }

  fun `test start project info refresh when linked`(): Unit = runDispatchingOnUi {
    val cloudProjectInfoWithNameFromResponse = CloudProjectProperties("project_name")

    mockQDCloudHttpClient.apply {
      respond("projects/*") {
        @Language("JSON")
        val response = """
          {
            "id": "project_id",
            "organizationId": "organization_id",
            "name": "project_name"
          }
        """.trimIndent()
        qodanaCloudResponse {
          response
        }
      }
      respondTimeline {
        throw CancellationException()
      }
    }

    val notLinked = linkStateValue as LinkState.NotLinked
    notLinked.linkWithQodanaCloudProject(authorized, CloudProjectData(cloudProjectPrimaryData, emptyCloudProjectProperties))
    val linked = linkStateValue as LinkState.Linked

    assertThat(linked.projectDataProvider.projectProperties.value)
      .isEqualTo(RefreshableProperty.PropertyState(QDCloudResponse.Success(emptyCloudProjectProperties), isRefreshing = false))
    dispatchAllTasksOnUi()

    assertThat(linked.projectDataProvider.projectProperties.value)
      .isEqualTo(RefreshableProperty.PropertyState(QDCloudResponse.Success(cloudProjectInfoWithNameFromResponse), isRefreshing = false))
  }

  fun `test unlink when logged out`(): Unit = runDispatchingOnUi {
    mockQDCloudHttpClient.apply {
      respond("projects/*") {
        throw CancellationException()
      }
      respond("projects/*/timeline") {
        throw CancellationException()
      }
    }

    val notLinked = linkStateValue as LinkState.NotLinked
    notLinked.linkWithQodanaCloudProject(authorized, CloudProjectData(cloudProjectPrimaryData, emptyCloudProjectProperties))

    dispatchAllTasksOnUi()

    authorized.logOut()
    dispatchAllTasksOnUi()

    assertTrue(linkStateValue is LinkState.NotLinked)
  }

  fun `test no new report notification when linked because of auto loading cloud report`(): Unit = runDispatchingOnUi {
    mockQDCloudHttpClient.apply {
      respondQodanaSarifJson(sarifTestReports.valid1)
      respondTimeline {
        "report_1"
      }
    }

    val notLinked = linkStateValue as LinkState.NotLinked
    notLinked.linkWithQodanaCloudProject(authorized, CloudProjectData(cloudProjectPrimaryData, emptyCloudProjectProperties))

    val linked = linkStateValue as LinkState.Linked

    assertNoNotifications {
      dispatchAllTasksOnUi()

      linked.cloudReportDescriptorBuilder.createReportDescriptor().loadSarifReport(project)
      dispatchAllTasksOnUi()
      delay(cloudProjectLinkService.refreshedReportTimeoutBeforeNotification)
      dispatchAllTasksOnUi()
    }
  }

  fun `test new report notification when later new report arrived`(): Unit = runDispatchingOnUi {
    mockQDCloudHttpClient.apply {
      respond("projects/*") {
        throw CancellationException()
      }
      respondTimeline {
        "report_1"
      }
      respondQodanaSarifJson(sarifTestReports.valid1)
    }

    val notLinked = linkStateValue as LinkState.NotLinked
    notLinked.linkWithQodanaCloudProject(authorized, CloudProjectData(cloudProjectPrimaryData, emptyCloudProjectProperties))

    val linked = linkStateValue as LinkState.Linked

    assertNoNotifications {
      dispatchAllTasksOnUi()
    }

    mockQDCloudHttpClient.respondTimeline {
      "report_2"
    }

    linked.projectDataProvider.refreshLatestReportIdWithNotification()
    dispatchAllTasksOnUi()
    assertSingleNotificationWithMessage(QodanaBundle.message("qodana.cloud.available.report.text")) {
      delay(cloudProjectLinkService.refreshedReportTimeoutBeforeNotification)
      dispatchAllTasksOnUi()
    }
  }

  fun `test no new report notification because when later new report arrived report managed to load in time`(): Unit = runDispatchingOnUi {
    mockQDCloudHttpClient.apply {
      respond("projects/*") {
        throw CancellationException()
      }
      respondReportFiles("*") { _, _ ->
        throw CancellationException()
      }
      respondTimeline {
        "report_1"
      }
    }

    val notLinked = linkStateValue as LinkState.NotLinked
    notLinked.linkWithQodanaCloudProject(authorized, CloudProjectData(cloudProjectPrimaryData, emptyCloudProjectProperties))

    val linked = linkStateValue as LinkState.Linked

    assertNoNotifications {
      dispatchAllTasksOnUi()
    }

    mockQDCloudHttpClient.respondTimeline {
      "report_2"
    }

    linked.projectDataProvider.refreshLatestReportIdWithNotification()
    dispatchAllTasksOnUi()
    assertNoNotifications {
      delay(cloudProjectLinkService.refreshedReportTimeoutBeforeNotification / 2)
      dispatchAllTasksOnUi()
      runCatching { linked.cloudReportDescriptorBuilder.createReportDescriptor().loadSarifReport(project) }
      dispatchAllTasksOnUi()
      delay(cloudProjectLinkService.refreshedReportTimeoutBeforeNotification)
      dispatchAllTasksOnUi()
    }
  }
}

private fun MockQDCloudHttpClient.respondTimeline(projectId: String = "*", handler: suspend () -> String) {
  respond("projects/$projectId/timeline") {
    @Language("JSON")
    val response = """
      {
        "items": [
          {
            "reportId": "${handler.invoke()}"
          }
        ]
      }
    """.trimIndent()
    qodanaCloudResponse {
      response
    }
  }
}