package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.project.Project
import kotlinx.coroutines.awaitCancellation
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.MockQDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudRequest
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse

fun doTransitionToLinked(
  project: Project,
  authorized: UserState.Authorized,
  projectId: String,
  organizationId: String,
): LinkState.Linked {
  val projectLinkService = QodanaCloudProjectLinkService.getInstance(project)
  val notLinked = projectLinkService.linkState.value as LinkState.NotLinked
  val cloudProjectData = CloudProjectData(
    CloudProjectPrimaryData(projectId, CloudOrganizationPrimaryData(organizationId)),
    CloudProjectProperties(name = null)
  )
  notLinked.linkWithQodanaCloudProject(authorized, cloudProjectData)

  mockQDCloudHttpClient.respond("projects/$projectId") {
    qodanaCloudResponse {
      @Language("JSON")
      val response = """
        {
          "id": "project.id",
          "organizationId": "org.id",
          "name": "project.name"
        }
      """.trimIndent()
      response
    }
  }
  mockQDCloudHttpClient.respond("projects/$projectId/timeline") {
    awaitCancellation()
  }
  return QodanaCloudProjectLinkService.getInstance(project).linkState.value as LinkState.Linked
}

fun MockQDCloudHttpClient.respondReportFiles(
  reportId: String,
  handler: suspend (files: List<String>, request: QDCloudRequest) -> QDCloudResponse<String>?) {
  respond("reports/$reportId/files") { request ->
    val files = request.parameters["paths"]?.split(",") ?: emptyList()
    handler.invoke(files, request)
  }
}