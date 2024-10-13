package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.jetbrains.qodana.cloudclient.v1.QDCloudProjectApiV1
import org.jetbrains.qodana.publisher.PublishResult
import org.jetbrains.qodana.publisher.Publisher
import org.jetbrains.qodana.publisher.PublisherParameters
import org.jetbrains.qodana.publisher.QDCloudAwsClient
import org.jetbrains.qodana.publisher.schemas.PublisherReportType
import org.jetbrains.qodana.publisher.schemas.UploadedReport
import org.jetbrains.qodana.run.QodanaConverterInput
import org.jetbrains.qodana.run.runQodanaConverter
import java.net.http.HttpClient


suspend fun runConverterAndPublishToCloud(
  projectApi: QDCloudProjectApiV1,
  qodanaOutput: QodanaConverterInput.FullQodanaOutput
): PublishResult {
  // TODO – is converter needed?
  val converterOutput = runQodanaConverter(qodanaOutput)

  val awsClient = QDCloudAwsClient(HttpClient.newHttpClient())
  return Publisher(projectApi, awsClient).publish(
    PublisherParameters(
      reportPath = converterOutput.path,
      reportType = PublisherReportType.SARIF
    )
  )
}

val UploadedReport.reportId: String
  get() = reportLink.removeSuffix("/").substringAfterLast("/")
