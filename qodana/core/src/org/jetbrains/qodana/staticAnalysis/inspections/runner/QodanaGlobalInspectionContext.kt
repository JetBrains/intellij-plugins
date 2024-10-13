package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.ex.EnabledInspectionsProvider
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.Tools
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiFile
import com.intellij.ui.content.ContentManager
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalInspectionToolWrapper
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsConfigurationProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsProvider
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import java.nio.file.Path

/** @param outputPath in the Docker container, this is `/data/results` */
class QodanaGlobalInspectionContext(
  project: Project,
  contentManager: NotNullLazyValue<out ContentManager>,
  val config: QodanaConfig,
  private val outputPath: Path,
  val profile: QodanaProfile,
  val qodanaRunScope: CoroutineScope,
  val coverageStatisticsData: CoverageStatisticsData
) : GlobalInspectionContextImpl(project, contentManager) {

  /** In the Docker container, this is `/data/results`. */
  override fun getOutputPath(): Path = outputPath

  val profileState: QodanaProfile.QodanaProfileState = profile.createState(this)
  val effectiveProfile: QodanaInspectionProfile = profile.effectiveProfile
  val database = QodanaToolResultDatabase.create(outputPath)
  val consumer = QodanaProblemConsumer(project, database, profileState, qodanaRunScope)

  init {
    setProblemConsumer(consumer)
    setExternalProfile(effectiveProfile)
    myViewClosed = false // if closed, will stop running inspections at some point in UI mode
  }

  override fun createEnabledInspectionsProvider(localTools: List<Tools>, globalSimpleTools: List<Tools>, project: Project): EnabledInspectionsProvider {
    return InspectionsByScopesAggregator(localTools, globalSimpleTools, project)
  }

  override fun getWrappersFromTools(
    enabledInspectionsProvider: EnabledInspectionsProvider,
    file: PsiFile,
    includeDoNotShow: Boolean
  ): EnabledInspectionsProvider.ToolWrappers {
    val wrappers = super.getWrappersFromTools(enabledInspectionsProvider, file, includeDoNotShow)
    return EnabledInspectionsProvider.ToolWrappers(
      wrappers.allLocalWrappers.filterNot { profileState.shouldSkip(it.shortName, file, wrappers) },
      wrappers.allGlobalSimpleWrappers.filterNot { profileState.shouldSkip(it.shortName, file, wrappers) },
    )
  }

  override fun runExternalTools() {
    runBlockingCancellable {
      sequenceOf(ExternalToolsConfigurationProvider.runAnnounceJobDescriptors(this@QodanaGlobalInspectionContext),
                 ExternalToolsProvider.runAnnounceJobDescriptors(this@QodanaGlobalInspectionContext))
        .flatMap { it.toList() }
        .forEach { appendJobDescriptor(it) }
      ExternalToolsConfigurationProvider.runPreRunActivities(this@QodanaGlobalInspectionContext, inspectionEventPublisher)
      ExternalToolsProvider.runExternalToolsProviders(this@QodanaGlobalInspectionContext, inspectionEventPublisher)
      ExternalToolsConfigurationProvider.runPostRunActivities(this@QodanaGlobalInspectionContext)
    }
  }

  override fun classifyTool(
    outGlobalTools: MutableList<in Tools>,
    outLocalTools: MutableList<in Tools>,
    outGlobalSimpleTools: MutableList<in Tools>,
    currentTools: Tools,
    toolWrapper: InspectionToolWrapper<*, *>
  ) {
    if (toolWrapper is ExternalInspectionToolWrapper) return
    super.classifyTool(outGlobalTools, outLocalTools, outGlobalSimpleTools, currentTools, toolWrapper)
  }

  suspend fun closeQodanaContext() {
    consumer.close()
    profileState.onFinish()
    profileState.dump()
    database.close()
  }

  fun coverageComputationState() = coverageStatisticsData.coverageComputationState
}