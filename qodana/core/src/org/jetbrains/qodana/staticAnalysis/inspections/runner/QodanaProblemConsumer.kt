package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.ex.InspectionProblemConsumer
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderEx
import com.intellij.platform.util.coroutines.childScope
import com.jetbrains.qodana.sarif.model.Fix
import kotlinx.coroutines.CoroutineScope
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.problemDescriptors.MetricCodeDescriptor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile.QodanaProfileState
import java.nio.file.Path

class QodanaProblemConsumer(
  private val project: Project,
  private val database: QodanaToolResultDatabase,
  private val profileState: QodanaProfileState,
  qodanaRunScope: CoroutineScope
) : InspectionProblemConsumer {
  private val macroManager = PathMacroManager.getInstance(project)
  private val writer = AsyncInspectionToolResultWriter(
    qodanaRunScope.childScope(),
    project,
    database,
    profileState,
    macroManager
  )

  private val databaseMetricWriter = AsyncDatabaseMetricWriter(
    qodanaRunScope.childScope(),
    project,
    database
  )

  override fun consume(element: Element, descriptor: CommonProblemDescriptor, toolWrapper: InspectionToolWrapper<*, *>) {
    if (descriptor is MetricCodeDescriptor) {
      consumeCodeQualityMetricsInfo(descriptor, toolWrapper)
      return
    }

    val fileUri = element.getChildText("file")
    val problem = XmlProblem(
      element,
      buildFixes(macroManager.collapsePath(fileUri), descriptor),
      descriptor as? UserDataHolderEx
    )

    consume(listOf(problem), toolWrapper.shortName)
  }

  private fun consumeCodeQualityMetricsInfo(descriptor: MetricCodeDescriptor, toolWrapper: InspectionToolWrapper<*, *>) {
    databaseMetricWriter.consume(descriptor.fileData)
  }

  private fun buildFixes(fileUri: String, descriptor: CommonProblemDescriptor): Set<Fix> {
    return emptySet()
  }

  suspend fun consumeGlobalOutput(paths: List<Path>) {
    GlobalOutputConsumer.runConsumers(profileState, paths, database, project, ::consume)
  }

  // Rider compatibility
  @Suppress("unused")
  fun consume(elements: List<Element>, inspectionId: String) {
    consume(elements.map { XmlProblem(it) }, inspectionId)
  }

  internal fun consume(problems: List<Problem>, inspectionId: String) {
    profileState.onReceive(inspectionId, problems.size)
    val profileGroupState = profileState.stateByInspectionId[inspectionId] ?: return
    writer.batchConsume(problems) { problem ->
      val relativePath = problem.getFile()
      val module = problem.getModule()

      return@batchConsume profileGroupState.onConsumeProblem(inspectionId, relativePath, module)
    }
  }

  suspend fun close() {
    writer.close()
    databaseMetricWriter.close()
  }
}