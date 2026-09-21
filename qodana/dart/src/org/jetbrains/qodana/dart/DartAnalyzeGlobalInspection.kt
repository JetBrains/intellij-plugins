package org.jetbrains.qodana.dart

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.codeInspection.ex.GlobalInspectionContextBase
import com.intellij.codeInspection.ex.GlobalInspectionToolWrapper
import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.codeInspection.ex.Tools
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension
import com.intellij.codeInspection.lang.HTMLComposerExtension
import com.intellij.codeInspection.lang.InspectionExtensionsFactory
import com.intellij.codeInspection.lang.RefManagerExtension
import com.intellij.codeInspection.reference.RefManager
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.lang.annotation.ProblemGroup
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalInspectionDescriptor
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolIssue
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsProvider
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer

internal const val DART_ANALYZE_SHORT_NAME = "DartAnalyze"
internal const val DART_ANALYZE_ERROR_SHORT_NAME = "DartAnalyzeError"
internal const val DART_ANALYZE_WARNING_SHORT_NAME = "DartAnalyzeWarning"
internal const val DART_ANALYZE_INFO_SHORT_NAME = "DartAnalyzeInfo"
internal const val DART_PROJECT_SETUP_SHORT_NAME = "DartProjectSetup"

class DartAnalyzeInspectionDescriptorProvider : ExternalToolsProvider {
  override fun describeTools(project: com.intellij.openapi.project.Project?): List<ExternalInspectionDescriptor> = listOf(
    ExternalInspectionDescriptor(
      DART_ANALYZE_SHORT_NAME,
      QodanaDartBundle.message("inspection.dart.analyze.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.description"),
      true,
      HighlightDisplayLevel.WARNING,
    ),
    ExternalInspectionDescriptor(
      DART_ANALYZE_ERROR_SHORT_NAME,
      QodanaDartBundle.message("inspection.dart.analyze.error.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.error.description"),
      true,
      HighlightDisplayLevel.ERROR,
    ),
    ExternalInspectionDescriptor(
      DART_ANALYZE_WARNING_SHORT_NAME,
      QodanaDartBundle.message("inspection.dart.analyze.warning.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.warning.description"),
      true,
      HighlightDisplayLevel.WARNING,
    ),
    ExternalInspectionDescriptor(
      DART_ANALYZE_INFO_SHORT_NAME,
      QodanaDartBundle.message("inspection.dart.analyze.info.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.info.description"),
      true,
      HighlightDisplayLevel.WEAK_WARNING,
    ),
    ExternalInspectionDescriptor(
      DART_PROJECT_SETUP_SHORT_NAME,
      QodanaDartBundle.message("inspection.dart.project.setup.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.analyze.group.name"),
      QodanaDartBundle.message("inspection.dart.project.setup.description"),
      true,
      HighlightDisplayLevel.ERROR,
    ),
  )

  override fun announceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor> = emptyArray()

  override suspend fun runTools(context: QodanaGlobalInspectionContext, consumer: Consumer<ExternalToolIssue>) {
  }
}

internal fun interface DartAnalyzeRunnerFactory {
  fun create(): DartAnalyzeRunner
}

internal open class DartAnalyzeGlobalInspection(
  private val runnerFactory: DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory { DartExecutableRunner() },
) : GlobalInspectionTool() {
  override fun getShortName(): String = SHORT_NAME

  override fun getDisplayName(): String = QodanaDartBundle.message("inspection.dart.analyze.name")

  override fun getGroupDisplayName(): String = QodanaDartBundle.message("inspection.dart.analyze.group.name")

  override fun isReadActionNeeded(): Boolean = false

  override fun isGraphNeeded(): Boolean = false

  @Suppress("HardCodedStringLiteral")
  override fun runInspection(
    scope: AnalysisScope,
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    val qodanaContext = globalContext as? QodanaGlobalInspectionContext ?: return
    if (!qodanaContext.hasEnabledDartAnalyzeTool()) return

    val analysisDirectory = qodanaContext.analysisDirectory()
    val job = JobDescriptor(QodanaDartBundle.message("inspection.dart.analyze.progress"))
    job.totalAmount = 1

    LOG.info("Analyzing Dart project at '${analysisDirectory.toQodanaLogPath(qodanaContext.config.projectPath)}'")
    when (val result = runBlockingCancellable { analyze(analysisDirectory) }) {
      is DartAnalyzeRunResult.Success -> result.diagnostics.forEach { diagnostic ->
        registerProblem(diagnostic, analysisDirectory, manager, globalContext, problemDescriptionsProcessor)
      }
      is DartAnalyzeRunResult.Failure -> {
        if (qodanaContext.isToolEnabled(result.inspectionId)) {
          registerProblem(result, analysisDirectory, qodanaContext)
        }
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
        DartAnalyzeRunResult.Failure(diagnostic.description(), diagnostic.inspectionId()),
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
      descriptor.setProblemGroup(ProblemGroup { diagnostic.inspectionId() })
      val reference = globalContext.refManager.getReference(psiFile)
      problemDescriptionsProcessor.addProblemElement(reference, descriptor)
    }
  }

  companion object {
    const val SHORT_NAME: String = "DartAnalyzeGlobalInspection"

    private val LOG = logger<DartAnalyzeGlobalInspection>()
  }
}

internal class DartAnalyzeGlobalInspectionContextExtension : GlobalInspectionContextExtension<DartAnalyzeGlobalInspectionContextExtension> {
  override fun getID(): Key<DartAnalyzeGlobalInspectionContextExtension> = ID

  override fun performPreInitToolsActivities(usedTools: MutableList<Tools>, context: GlobalInspectionContext) {
    val contextBase = context as? GlobalInspectionContextBase ?: return
    if (usedTools.none { it.shortName.isDartAnalyzeTool() }) return
    ensureGlobalInspectionEnabled(contextBase, usedTools)
  }

  override fun performPreRunActivities(
    globalTools: MutableList<Tools>,
    localTools: MutableList<Tools>,
    context: GlobalInspectionContext,
  ) {
  }

  override fun performPostRunActivities(inspections: MutableList<com.intellij.codeInspection.ex.InspectionToolWrapper<*, *>>, context: GlobalInspectionContext) {
  }

  override fun cleanup() {
  }

  private fun ensureGlobalInspectionEnabled(context: GlobalInspectionContextBase, usedTools: MutableList<Tools>) {
    if (usedTools.any { it.shortName == DartAnalyzeGlobalInspection.SHORT_NAME }) return

    if (context.currentProfile.getInspectionTool(DartAnalyzeGlobalInspection.SHORT_NAME, context.project) == null) {
      context.currentProfile.addTool(context.project, GlobalInspectionToolWrapper(DartAnalyzeGlobalInspection()), null)
    }
    context.currentProfile.enableTool(DartAnalyzeGlobalInspection.SHORT_NAME, context.project)
    usedTools.add(context.currentProfile.getTools(DartAnalyzeGlobalInspection.SHORT_NAME, context.project))
  }

  companion object {
    private val ID = Key.create<DartAnalyzeGlobalInspectionContextExtension>("DartAnalyzeGlobalInspectionContextExtension")
  }
}

internal class DartAnalyzeGlobalInspectionContextFactory : InspectionExtensionsFactory() {
  override fun createGlobalInspectionContextExtension(): GlobalInspectionContextExtension<*> = DartAnalyzeGlobalInspectionContextExtension()

  override fun createRefManagerExtension(refManager: RefManager?): RefManagerExtension<*>? = null

  override fun createHTMLComposerExtension(composer: com.intellij.codeInspection.HTMLComposer?): HTMLComposerExtension<*>? = null

  override fun isToCheckMember(element: PsiElement, id: String): Boolean = false

  override fun getSuppressedInspectionIdsIn(element: PsiElement): String? = null
}

private fun QodanaGlobalInspectionContext.hasEnabledDartAnalyzeTool(): Boolean =
  effectiveProfile.tools.any { it.isEnabled && it.shortName == DART_ANALYZE_SHORT_NAME }

private fun QodanaGlobalInspectionContext.analysisDirectory(): Path {
  val onlyDirectory = config.onlyDirectory ?: return config.projectPath
  val resolved = onlyDirectory.let { if (it.isAbsolute) it else config.projectPath.resolve(it) }
  return resolved.takeIf(Files::isDirectory) ?: config.projectPath
}

private fun QodanaGlobalInspectionContext.isFileInInspectionScope(inspectionId: String, psiFile: PsiFile): Boolean {
  val tools = effectiveProfile.getToolsOrNull(inspectionId, project) ?: return false
  return tools.getEnabledTool(psiFile, true) != null
}

private fun QodanaGlobalInspectionContext.isToolEnabled(shortName: String): Boolean =
  effectiveProfile.tools.any { it.isEnabled && it.shortName == shortName }

internal fun String.isDartAnalyzeTool(): Boolean = this == DART_ANALYZE_SHORT_NAME

private fun DartAnalyzeDiagnostic.inspectionId(): String = when (severity.uppercase()) {
  "ERROR" -> DART_ANALYZE_ERROR_SHORT_NAME
  "WARNING" -> DART_ANALYZE_WARNING_SHORT_NAME
  "INFO" -> DART_ANALYZE_INFO_SHORT_NAME
  else -> DART_ANALYZE_SHORT_NAME
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
