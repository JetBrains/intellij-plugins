package org.jetbrains.qodana

import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.MockQDCloudHttpClient
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse

fun MockQDCloudHttpClient.respond200PublishReport(
  host: String,
  projectId: String,
  reportId: String,
) {
  respond("reports/") {
    @Language("JSON")
    val response = """
      {
        "reportId": "$reportId",
        "fileLinks": {},
        "langsRequired": false
      }
    """.trimIndent()
    qodanaCloudResponse {
      response
    }
  }
  respond("reports/$reportId/finish/") {
    @Language("JSON")
    val response = """
      {
        "token": "xxx",
        "url": "$host/projects/$projectId/reports/$reportId"
      }
    """.trimIndent()
    qodanaCloudResponse {
      response
    }
  }
}