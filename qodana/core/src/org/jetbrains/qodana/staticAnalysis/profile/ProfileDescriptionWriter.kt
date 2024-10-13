package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.InspectionsResultUtil
import com.intellij.codeInspection.ex.ReportConverterUtil
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.io.createDirectories
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.OutputFormat
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ProfileDescriptionWriter : QodanaWorkflowExtension {
  override val requireHeadless: Boolean = true

  override suspend fun beforeLaunch(context: QodanaRunContext) {
    val converter = ReportConverterUtil.getReportConverter("json")

    if (converter == null) {
      thisLogger().error("Cannot find json report converter, not writing profiles")
      return
    }
    if (context.config.outputFormat != OutputFormat.INSPECT_SH_FORMAT) return

    val profile = context.qodanaProfile.mainGroup.profile
    val path = context.config.outPath
    runInterruptible(StaticAnalysisDispatchers.IO) {
      val descriptions: Path = path.resolve(InspectionsResultUtil.DESCRIPTIONS + InspectionsResultUtil.XML_EXTENSION)
      descriptions.parent.createDirectories()
      InspectionsResultUtil.describeInspections(descriptions, profile.name, profile)
      converter.convert(path.absolutePathString(), path.absolutePathString(), emptyMap(), listOf(descriptions.toFile()))
      Files.delete(descriptions)
    }
  }
}
