package org.jetbrains.qodana.dart

import com.intellij.codeInspection.DefaultInspectionToolResultExporter
import com.intellij.codeInspection.InspectionsResultUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Run
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.SarifReportContributor
import java.nio.file.Path

internal class DartAnalyzeGlobalInspectionOutputConsumer : GlobalOutputConsumer, SarifReportContributor {
  override fun ownedFiles(paths: List<Path>): List<Path> {
    val resultsPath = paths.singleOrNull { FileUtil.getNameWithoutExtension(it.fileName.toString()) == DART_ANALYZE_SHORT_NAME }
    val aggregateResultsPath = paths.singleOrNull {
      FileUtil.getNameWithoutExtension(it.fileName.toString()) == DART_ANALYZE_SHORT_NAME + InspectionsResultUtil.AGGREGATE
    }
    return listOfNotNull(aggregateResultsPath, resultsPath)
  }

  override suspend fun consumeOwnedFiles(
    profileState: QodanaProfile.QodanaProfileState,
    paths: List<Path>,
    database: QodanaToolResultDatabase,
    project: Project,
    consumer: (List<Problem>, String) -> Unit
  ) {
    if (!GlobalOutputConsumer.reportingInspectionAllowed(profileState, DART_ANALYZE_SHORT_NAME)) return
    if (paths.size != 2) {
      LOG.warn("Expected the $DART_ANALYZE_SHORT_NAME result file and the aggregate file, but got: $paths")
      return
    }
    GlobalOutputConsumer.consumeOutputXmlFile(paths.first()) { _, root ->
      root.getChildren("problem")
        .groupBy { it.getInspectionId() ?: DART_ANALYZE_SHORT_NAME }
        .forEach { (inspectionId, problems) ->
          consumer(problems.map { GlobalOutputConsumer.createXmlProblem(it) }, inspectionId)
        }
    }
  }

  override fun contribute(run: Run, project: Project, config: QodanaConfig) {
    val components = listOfNotNull(run.tool.driver) + run.tool.extensions
    val dartRuleIds = components.asSequence()
      .flatMap { it.rules.orEmpty().asSequence() }
      .filter { it.properties?.get("parentRuleId") == DART_ANALYZE_SHORT_NAME }
      .mapNotNull { it.id }
      .toSet()
    run.results.orEmpty().filter { it.ruleId in dartRuleIds }.forEach { result ->
      val properties = result.properties ?: PropertyBag()
      properties["parentRuleId"] = DART_ANALYZE_SHORT_NAME
      result.properties = properties
    }
  }
}

private val LOG = logger<DartAnalyzeGlobalInspectionOutputConsumer>()

private fun Element.getInspectionId(): String? {
  val problemClassElement = getChild(DefaultInspectionToolResultExporter.INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)
  return problemClassElement?.getAttributeValue(DefaultInspectionToolResultExporter.INSPECTION_RESULTS_ID_ATTRIBUTE)
}
