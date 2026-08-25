package org.jetbrains.qodana.dart

import com.intellij.analysis.AnalysisScope
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.lang.annotation.ProblemGroup
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.Processor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolIssue
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspection
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspectionProvider
import org.jetbrains.qodana.staticAnalysis.profile.DetectedProblemGroup
import java.nio.file.Files
import java.nio.file.Path

internal const val DART_ANALYZE_SHORT_NAME = "DartAnalyze"

class DartAnalyzeDerivedInspectionProvider : DerivedInspectionProvider {
  override fun isApplicableTo(parentInspectionId: String): Boolean = parentInspectionId == DART_ANALYZE_SHORT_NAME

  override fun describeDerivedInspections(
    project: com.intellij.openapi.project.Project?,
    inspections: Collection<DetectedProblemGroup>,
  ): List<DerivedInspection> = inspections.map { inspection ->
      DerivedInspection(
        inspection.problemGroupId,
        DART_ANALYZE_SHORT_NAME,
        QodanaDartBundle.message("inspection.dart.analyze.diagnostic.name", inspection.problemGroupId),
        QodanaDartBundle.message("inspection.dart.analyze.diagnostic.description", inspection.problemGroupId),
      )
    }
    .distinct()
}

internal fun interface DartAnalyzeRunnerFactory {
  fun create(): DartAnalyzeRunner
}

internal open class DartAnalyzeGlobalInspection(
  private val runnerFactory: DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory { DartExecutableRunner() },
) : GlobalInspectionTool() {
  override fun getShortName(): String = SHORT_NAME

  override fun getDisplayName(): String = QodanaDartBundle.message("inspection.dart.analyze.name")

  override fun getGroupDisplayName(): String = QodanaDartBundle.message("inspection.dart.analyze.taxonomy.name")

  override fun getGroupPath(): Array<String> = arrayOf(QodanaDartBundle.message("inspection.dart.analyze.group.name"), groupDisplayName)

  override fun isReadActionNeeded(): Boolean = false

  override fun isGraphNeeded(): Boolean = false

  override fun runInspection(
    scope: AnalysisScope,
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    val qodanaContext = globalContext as? QodanaGlobalInspectionContext ?: return
    if (!qodanaContext.containsDartFiles(scope)) {
      LOG.info("Skipping Dart analysis: no in-scope Dart files were found")
      return
    }

    val analysisDirectory = qodanaContext.analysisDirectory()
    val job = JobDescriptor(QodanaDartBundle.message("inspection.dart.analyze.progress"))
    job.totalAmount = 1

    LOG.info("Analyzing Dart project at '${analysisDirectory.toQodanaLogPath(qodanaContext.config.projectPath)}'")
    when (val result = runBlockingCancellable { analyze(analysisDirectory) }) {
      is DartAnalyzeRunResult.Success -> result.diagnostics.forEach { diagnostic ->
        registerProblem(diagnostic, analysisDirectory, manager, globalContext, problemDescriptionsProcessor)
      }
      is DartAnalyzeRunResult.Failure -> {
        LOG.warn("Dart analysis failed for '${analysisDirectory.toQodanaLogPath(qodanaContext.config.projectPath)}': ${result.message}; other inspections will continue")
        registerProblem(result, analysisDirectory, qodanaContext)
      }
    }
    LOG.info("Finished analyzing Dart project at '${analysisDirectory.toQodanaLogPath(qodanaContext.config.projectPath)}'")
    val directoryPath: @NlsSafe String = analysisDirectory.toQodanaLogPath(qodanaContext.config.projectPath)
    globalContext.incrementJobDoneAmount(job, directoryPath)
  }

  private suspend fun analyze(analysisDirectory: Path): DartAnalyzeRunResult = runnerFactory.create().analyze(analysisDirectory)

  private fun registerProblem(
    failure: DartAnalyzeRunResult.Failure,
    analysisDirectory: Path,
    qodanaContext: QodanaGlobalInspectionContext,
  ) {
    val directoryPath: @NlsSafe String = analysisDirectory.toQodanaLogPath(qodanaContext.config.projectPath)
    val issue = ExternalToolIssue(
      failure.inspectionId,
      HighlightDisplayLevel.ERROR,
      QodanaDartBundle.message("dart.analyze.error.module", directoryPath, failure.message),
      module = directoryPath,
    )
    qodanaContext.consumer.consume(listOf(issue.toElement()), failure.inspectionId)
  }

  private fun registerProblem(
    diagnostic: DartAnalyzeDiagnostic,
    projectPath: Path,
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    val location = diagnostic.location
    val file = location?.file
    if (location == null || file == null) {
      return registerProblem(
        DartAnalyzeRunResult.Failure(diagnostic.description(), DART_ANALYZE_SHORT_NAME),
        projectPath,
        globalContext as? QodanaGlobalInspectionContext ?: return,
      )
    }
    val path = Path.of(file).let { if (it.isAbsolute) it else projectPath.resolve(it) }
    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
      ?: run {
        LOG.warn("Unable to find Dart analyzer diagnostic file '${path.toQodanaLogPath(projectPath)}'")
        return
      }
    runReadActionBlocking {
      val psiFile = PsiManager.getInstance(globalContext.project).findFile(virtualFile)
        ?: run {
          LOG.warn("Unable to create PSI file for Dart analyzer diagnostic file '${path.toQodanaLogPath(projectPath)}'")
          return@runReadActionBlocking
        }
      val qodanaContext = globalContext as? QodanaGlobalInspectionContext ?: return@runReadActionBlocking
      if (!qodanaContext.isFileInInspectionScope(DART_ANALYZE_SHORT_NAME, psiFile)) return@runReadActionBlocking
      val descriptor = manager.createProblemDescriptor(
        psiFile,
        location.rangeIn(PsiDocumentManager.getInstance(globalContext.project).getDocument(psiFile) ?: return@runReadActionBlocking),
        diagnostic.description(),
        diagnostic.severity.toProblemHighlightType(),
        false,
      )
      descriptor.setProblemGroup(ProblemGroup { diagnostic.code })
      val reference = globalContext.refManager.getReference(psiFile)
      problemDescriptionsProcessor.addProblemElement(reference, descriptor)
    }
  }

  companion object {
    const val SHORT_NAME: String = DART_ANALYZE_SHORT_NAME

    private val LOG = logger<DartAnalyzeGlobalInspection>()
  }
}

internal fun QodanaGlobalInspectionContext.containsDartFiles(scope: AnalysisScope): Boolean {
  val psiManager = PsiManager.getInstance(project)
  var foundDartFile = false
  scope.accept(Processor { file ->
    if (file.isDirectory || file.extension != "dart") return@Processor true
    runReadActionBlocking {
      val psiFile = psiManager.findFile(file) ?: return@runReadActionBlocking true
      if (isFileInInspectionScope(DART_ANALYZE_SHORT_NAME, psiFile)) {
        foundDartFile = true
        false
      }
      else true
    }
  })
  return foundDartFile
}

internal fun QodanaGlobalInspectionContext.analysisDirectory(): Path {
  val onlyDirectory = config.onlyDirectory ?: return config.projectPath
  val resolved = onlyDirectory.let { if (it.isAbsolute) it else config.projectPath.resolve(it) }
  return resolved.takeIf(Files::isDirectory) ?: config.projectPath
}

private fun QodanaGlobalInspectionContext.isFileInInspectionScope(inspectionId: String, psiFile: PsiFile): Boolean {
  val tools = effectiveProfile.getToolsOrNull(inspectionId, project) ?: return false
  return tools.getEnabledTool(psiFile, true) != null
}

@Suppress("HardCodedStringLiteral")
private fun DartAnalyzeDiagnostic.description(): @InspectionMessage String = buildString {
  append('[').append(code).append("] ").append(message)
  correction?.takeIf(String::isNotBlank)?.let { append('\n').append(it) }
}

private fun String.toProblemHighlightType(): ProblemHighlightType =
  when (uppercase()) {
    "ERROR" -> ProblemHighlightType.ERROR
    "INFO" -> ProblemHighlightType.INFORMATION
    else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
  }
