package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.codeInspection.ex.InspectionProblemConsumer
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.modcommand.ModCommandQuickFix
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderEx
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.problemDescriptors.MetricCodeDescriptor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile.QodanaProfileState
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.areStructuralFingerprintsEnabled
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor
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

    val userData = descriptor as? UserDataHolderEx
    val structuralFingerprintSignals = getStructuralSignals(descriptor)

    val problem = XmlProblem(
      element,
      toolWrapper.isCleanupTool,
      descriptor.fixes?.any { it is ModCommandQuickFix } == true,
      userData,
      structuralFingerprintSignals
    )

    consume(listOf(problem), toolWrapper.shortName)
  }

  private fun getStructuralSignals(descriptor: CommonProblemDescriptor): StructuralFingerprintSignals? {
    if (!areStructuralFingerprintsEnabled) return null

    val baseDescriptor = descriptor as? ProblemDescriptorBase ?: return null

    return runReadActionBlocking {
      val element = baseDescriptor.psiElement
      if (element != null && element.isValid) {
        PsiSignalExtractor.extractSignals(element)
      } else {
        null
      }
    }
  }

  private fun consumeCodeQualityMetricsInfo(descriptor: MetricCodeDescriptor, toolWrapper: InspectionToolWrapper<*, *>) {
    databaseMetricWriter.consume(descriptor.fileData)
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