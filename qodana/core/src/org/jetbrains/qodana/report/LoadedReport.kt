package org.jetbrains.qodana.report

import com.intellij.openapi.extensions.ExtensionPointName
import java.nio.file.Path

class AggregatedReportMetadata(val map: Map<String, ReportMetadata>)

interface ReportMetadata {
  val id: String
}

/*
 * Provides the possibility to declare the provider for custom artifacts that should be downloaded from the cloud;
 */
interface ReportMetadataArtifactProvider {
  companion object {
    @JvmField
    val EP_NAME = ExtensionPointName<ReportMetadataArtifactProvider>("org.intellij.qodana.reportMetadataArtifact")
  }

  /*
   * Provider name
   */
  val name: String
  /*
   * Path to the file in the cloud (i.e. folder1/folder2/file)
   */
  val fileName: String
  /*
   * Target file name for flat storage (e.g. without any additional folders).
   */
  val presentableFileName: String

  /*
   * Path to the [presentableFileName] file to be processed.
   */
  suspend fun readReport(path: Path): ReportMetadata?
}