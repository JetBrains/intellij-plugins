package org.jetbrains.qodana.coverage

import org.jetbrains.qodana.report.ReportMetadata
import java.io.File

/*
 * Class to wrap every coverage artifact obtained from the cloud
 */
data class CoverageMetaDataArtifact(override val id: String, val path: File): ReportMetadata