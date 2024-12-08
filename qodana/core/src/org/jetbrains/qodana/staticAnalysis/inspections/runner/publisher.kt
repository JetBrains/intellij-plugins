package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.jetbrains.qodana.cloudclient.v1.QDCloudProjectApiV1
import org.jetbrains.qodana.publisher.PublishResult
import org.jetbrains.qodana.publisher.Publisher
import org.jetbrains.qodana.publisher.PublisherParameters
import org.jetbrains.qodana.publisher.QDCloudAwsClient
import org.jetbrains.qodana.publisher.schemas.PublisherReportType
import org.jetbrains.qodana.publisher.schemas.UploadedReport
import java.net.http.HttpClient
import java.nio.file.Path


suspend fun publishToCloud(
  projectApi: QDCloudProjectApiV1,
  reportPath: Path
): PublishResult {

  val awsClient = QDCloudAwsClient(HttpClient.newHttpClient())
  return Publisher(projectApi, awsClient).publish(
    PublisherParameters(
      reportPath = reportPath,
      reportType = PublisherReportType.SARIF
    )
  )
}

val UploadedReport.reportId: String
  get() = reportLink.removeSuffix("/").substringAfterLast("/")
