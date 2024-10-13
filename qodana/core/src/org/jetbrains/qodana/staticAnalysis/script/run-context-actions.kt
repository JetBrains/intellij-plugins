package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.codeInspection.InspectionApplicationBase
import com.intellij.codeInspection.InspectionsBundle
import com.intellij.codeInspection.ex.GlobalInspectionContextUtil
import com.intellij.configurationStore.JbXmlOutputter
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.jobToIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.GeneratedSourcesFilter
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ex.ProgressIndicatorEx
import com.intellij.profile.ProfileEx
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.util.io.createDirectories
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.job
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jdom.Element
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_EXTENSION
import org.jetbrains.qodana.runActivityWithTiming
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.profile.NamedInspectionGroup
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber
import org.jetbrains.qodana.staticAnalysis.sarif.automationDetails
import org.jetbrains.qodana.staticAnalysis.sarif.configProfile
import org.jetbrains.qodana.staticAnalysis.sarif.getOrAssignProperties
import org.jetbrains.qodana.staticAnalysis.sarif.resultsFlowByGroup
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.stat.InspectionEventsCollector.QodanaActivityKind
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists


fun QodanaRunContext.appendRunDetails(run: Run, analysisKind: AnalysisKind) {
  run.automationDetails = automationDetails(project, analysisKind)
  val (key, profile) = configProfile(loadedProfile)
  run.getOrAssignProperties()[key] = profile
}


suspend fun QodanaRunContext.writeProjectDescriptionBeforeWork(projectStructureParentPath: Path = config.outPath) {
  val outputPath = projectStructureParentPath.resolve(InspectionApplicationBase.PROJECT_STRUCTURE_DIR)
  runInterruptible(StaticAnalysisDispatchers.IO) {
    outputPath.toFile().mkdir()
  }
  QodanaProjectDescriber.runDescribers(outputPath, project)
}

suspend fun QodanaRunContext.writeProjectDescriptionAfterWork(projectStructureParentPath: Path = config.outPath) {
  val projectStructure = projectStructureParentPath.resolve(InspectionApplicationBase.PROJECT_STRUCTURE_DIR)
  runInterruptible(StaticAnalysisDispatchers.IO) {
    projectStructure.toFile().mkdirs()
  }
  QodanaProjectDescriber.runDescribersAfterWork(projectStructure, project)
}

suspend fun QodanaRunContext.createGlobalInspectionContext(
  outputPath: Path = config.resultsStorage,
  profile: QodanaProfile = qodanaProfile,
  coverageComputationState: QodanaCoverageComputationState = QodanaCoverageComputationState.DEFAULT
): QodanaGlobalInspectionContext {
  val contentManagerProvider = NotNullLazyValue.lazy {
    val mockContentManager = ToolWindowHeadlessManagerImpl.MockToolWindow(project).contentManager
    mockContentManager
  }
  return withContext(StaticAnalysisDispatchers.IO) {
    QodanaGlobalInspectionContext(
      project,
      contentManagerProvider,
      config,
      outputPath,
      profile,
      runCoroutineScope,
      CoverageStatisticsData(coverageComputationState, project, changes)
    )
  }
}

suspend fun QodanaRunContext.runAnalysis(
  scope: QodanaAnalysisScope = this.scope,
  context: QodanaGlobalInspectionContext,
  progressIndicator: ProgressIndicatorEx = QodanaProgressIndicator(messageReporter),
  isOffline: Boolean = true
) {
  scope.patchToNotAnalyzeGeneratedCode(project)
  if (!GlobalInspectionContextUtil.canRunInspections(project, false) {}) {
    throw QodanaException(InspectionsBundle.message("inspection.application.cannot.configure.project.to.run.inspections"))
  }

  val inspectionsResults = mutableListOf<Path>()
  runActivityWithTiming(QodanaActivityKind.PROJECT_ANALYSIS) {
    withContext(StaticAnalysisDispatchers.IO) {
      jobToIndicator(coroutineContext.job, progressIndicator) {
        context.performInspectionsWithProgressAndExportResults(
          scope,
          false,
          isOffline,
          context.outputPath,
          inspectionsResults
        )
      }
    }
  }
  dumpGlobalOutput(context, inspectionsResults)
  context.consumer.consumeGlobalOutput(inspectionsResults)
}

suspend fun QodanaRunContext.getResultsForInspectionGroup(
  context: QodanaGlobalInspectionContext,
  inspectionGroupState: NamedInspectionGroup.State = context.profileState.mainState
): List<Result> {
  val consumer = context.consumer
  consumer.close()
  return context.database.resultsFlowByGroup(inspectionGroupState.inspectionGroup.name, messageReporter).toList()
}

suspend fun QodanaRunContext.writeProfiles(profile: QodanaProfile) {
  runInterruptible(StaticAnalysisDispatchers.IO) {
    val logPath = Paths.get(PathManager.getLogPath())

    if (!logPath.exists()) logPath.createDirectories()
    profile.allGroups.forEach {
      val name = it.name.ifEmpty { "main" }
      val path = logPath.resolve("$name.profile.xml")
      writeProfile(it.profile, path, project)
    }
    writeProfile(profile.effectiveProfile, logPath.resolve("effective.profile.xml"), project)
  }
}

private fun writeProfile(profile: QodanaInspectionProfile, path: Path, project: Project) {
  val profileElement = Element(ProfileEx.PROFILE)
  profile.writeExternal(profileElement)
  profileElement.setAttribute("version", "1.0")

  val rootElement = Element("component")
    .setAttribute("name", "InspectionProjectProfileManager")
    .addContent(profileElement)

  Files.newBufferedWriter(path).use { writer ->
    JbXmlOutputter.collapseMacrosAndWrite(element = rootElement, project = project, writer = writer)
  }
}

private suspend fun QodanaRunContext.dumpGlobalOutput(context: QodanaGlobalInspectionContext, inspectionResults: List<Path>) {
  if (System.getProperty("qodana.default.copy.global.output", "false").toBoolean()) {
    runInterruptible(StaticAnalysisDispatchers.IO) {
      try {
        val outputPath = context.config.outPath.resolve("globalOutput/")
        outputPath.toFile().mkdir()
        for (path in inspectionResults) {
          Files.copy(path, outputPath.resolve(path.fileName))
        }
      }
      catch (e: Exception) {
        thisLogger().warn("Exception while copying global output", e)
      }
    }
  }
}

private val isFlexInspectIgnoredInQodana = !java.lang.Boolean.getBoolean("qodana.analyze.inspectionKts")

private fun QodanaAnalysisScope.patchToNotAnalyzeGeneratedCode(project: Project) {
  setFilter(object : GlobalSearchScope() {
    override fun contains(file: VirtualFile): Boolean {
      if (file.name.endsWith(INSPECTIONS_KTS_EXTENSION) && isFlexInspectIgnoredInQodana) {
        return false
      }
      return !GeneratedSourcesFilter.isGeneratedSourceByAnyFilter(file, project)
    }

    override fun isSearchInModuleContent(aModule: Module): Boolean {
      return true
    }

    override fun isSearchInLibraries(): Boolean {
      return true
    }
  })
}

internal suspend fun QodanaRunContext.applyExternalFileScope(
  paths: Iterable<Path>,
  onFileIncluded: ((VirtualFile) -> Unit)? = null,
  onFileExcluded: ((VirtualFile) -> Unit)? = null
): QodanaRunContext {
  val fs = LocalFileSystem.getInstance()
  val files = runInterruptible(StaticAnalysisDispatchers.IO) {
    paths.asSequence()
      .map { if (it.isAbsolute) it else config.projectPath.resolve(it) }
      .mapNotNull(fs::findFileByNioFile)
      .toList()
  }
  return QodanaRunContext(
    project,
    loadedProfile,
    externalFileScope(files, onFileIncluded, onFileExcluded),
    qodanaProfile,
    config,
    runCoroutineScope,
    messageReporter,
    changes
  )
}

internal suspend fun QodanaRunContext.externalFileScope(
  files: Iterable<VirtualFile>,
  onFileIncluded: ((VirtualFile) -> Unit)? = null,
  onFileExcluded: ((VirtualFile) -> Unit)? = null
): QodanaAnalysisScope {
  val (included, excluded) = readAction { files.partition(scope::contains) }
  onFileIncluded?.let(included::forEach)
  onFileExcluded?.let(excluded::forEach)
  return QodanaAnalysisScope(project, included)
}
