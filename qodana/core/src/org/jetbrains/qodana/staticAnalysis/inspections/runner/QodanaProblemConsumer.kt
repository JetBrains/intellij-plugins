package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.codeInspection.ex.InspectionProblemConsumer
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.modcommand.ModCommandQuickFix
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderEx
import com.intellij.platform.util.coroutines.childScope
import com.intellij.psi.PsiElement
import kotlinx.coroutines.CoroutineScope
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.problemDescriptors.MetricCodeDescriptor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile.QodanaProfileState
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintDebugLog
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.DomProblemFingerprintPsiResolver
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.FingerprintSource
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiFingerprintData
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiFingerprintDataExtractor
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.structuralSignalsDebugSummary
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

class QodanaProblemConsumer(
  private val project: Project,
  private val database: QodanaToolResultDatabase,
  private val profileState: QodanaProfileState,
  qodanaRunScope: CoroutineScope,
) : InspectionProblemConsumer {
  companion object {
    private val LOG = logger<QodanaProblemConsumer>()
  }

  private val macroManager = PathMacroManager.getInstance(project)
  private val domFingerprintFallbackLogged = AtomicBoolean()
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
    var descriptorPsiSummary = "<not ProblemDescriptorBase>"
    val fingerprintData = getFingerprintData(descriptor) { descriptorPsiSummary = it }
    logDomFingerprintFallbackIfNeeded(fingerprintData)
    StructuralFingerprintDebugLog.log(
      "QodanaProblemConsumer.consume inspection=${toolWrapper.shortName}, " +
      "descriptor=${descriptor.javaClass.name}, descriptorPsi=$descriptorPsiSummary, " +
      "fingerprintData=${fingerprintData != null}, source=${fingerprintData?.source}, " +
      "signals=${fingerprintData?.structuralSignals?.structuralSignalsDebugSummary() ?: "<none>"}, " +
      "additionalFingerprintData=${fingerprintData?.additionalFingerprintData?.isEmpty() == false}"
    )

    val problem = XmlProblem(
      element,
      toolWrapper.isCleanupTool,
      descriptor.fixes?.any { it is ModCommandQuickFix } == true,
      userData,
      fingerprintData
    )

    consume(listOf(problem), toolWrapper.shortName)
  }

  private fun getFingerprintData(descriptor: CommonProblemDescriptor, onDescriptorPsi: (String) -> Unit): PsiFingerprintData? {
    return runReadActionBlocking {
      val descriptorPsi = (descriptor as? ProblemDescriptorBase)?.let { baseDescriptor ->
        baseDescriptor.psiElement.also { onDescriptorPsi(it?.debugSummary() ?: "<null>") }
      }
      descriptorPsi?.takeIf { it.isValid }?.let { psiElement ->
        PsiFingerprintDataExtractor.extract(psiElement, FingerprintSource.PROBLEM_DESCRIPTOR_PSI)
      } ?: DomProblemFingerprintPsiResolver.resolve(descriptor)?.takeIf { it.isValid }?.let { psiElement ->
        PsiFingerprintDataExtractor.extract(psiElement, FingerprintSource.DOM_PROBLEM_DESCRIPTOR)
      }
    }
  }

  private fun consumeCodeQualityMetricsInfo(descriptor: MetricCodeDescriptor, toolWrapper: InspectionToolWrapper<*, *>) {
    databaseMetricWriter.consume(descriptor.fileData)
  }

  private fun logDomFingerprintFallbackIfNeeded(fingerprintData: PsiFingerprintData?) {
    if (fingerprintData?.source != FingerprintSource.DOM_PROBLEM_DESCRIPTOR) return
    if (fingerprintData.additionalFingerprintData.xmlSemanticIdentity == null) return
    if (!domFingerprintFallbackLogged.compareAndSet(false, true)) return

    LOG.warn(
      "Applied DOM XML fingerprint fallback. Matching SARIF results will include " +
      "qodanaXmlSemanticFingerprintApplied=true and qodanaFingerprintSource=domProblemDescriptor"
    )
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
    val profileGroupState = profileState.getInspectionState(inspectionId) ?: return
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

  private fun PsiElement.debugSummary(): String =
    "${javaClass.name}(language=${language.id}, valid=$isValid, textRange=$textRange)"
}
