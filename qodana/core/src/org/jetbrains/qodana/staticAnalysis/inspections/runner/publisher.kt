package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.util.net.PlatformHttpClient
import org.jetbrains.qodana.cloudclient.s3.QDCloudS3Client
import org.jetbrains.qodana.cloudclient.v1.QDCloudProjectApiV1
import org.jetbrains.qodana.publisher.PublishResult
import org.jetbrains.qodana.publisher.Publisher
import org.jetbrains.qodana.publisher.PublisherParameters
import org.jetbrains.qodana.publisher.schemas.PublisherReportType
import org.jetbrains.qodana.publisher.schemas.UploadedReport
import java.nio.file.Path


suspend fun publishToCloud(
  projectApi: QDCloudProjectApiV1,
  reportPath: Path
): PublishResult {
  return PlatformHttpClient.client().use { httpClient ->
    val s3Client = QDCloudS3Client(httpClient)
    Publisher(projectApi, s3Client).publish(
      PublisherParameters(
        reportPath = reportPath,
        reportType = PublisherReportType.SARIF
      )
    )
  }
}

val UploadedReport.reportId: String
  get() = reportLink.removeSuffix("/").substringAfterLast("/")
