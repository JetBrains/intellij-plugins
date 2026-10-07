package org.jetbrains.qodana.dart

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.DefaultInspectionToolResultExporter.INSPECTION_RESULTS_ID_ATTRIBUTE
import com.intellij.codeInspection.DefaultInspectionToolResultExporter.INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT
import com.intellij.codeInspection.ExportedInspectionsResultModifier
import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.codeInspection.reference.RefElement
import com.intellij.codeInspection.reference.RefEntity
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jdom.Element
import java.nio.file.Path

private const val DART_INSPECTION_ID_PREFIX = "dart_"

internal fun dartInspectionId(inspectionCode: String): String = "$DART_INSPECTION_ID_PREFIX$inspectionCode"

/** Returns the inspection code for a derived inspection ID, or null for another ID. */
internal fun dartInspectionCode(inspectionId: String): String? =
  inspectionId.removePrefix(DART_INSPECTION_ID_PREFIX).takeIf { it != inspectionId }

internal fun interface DartAnalyzeRunnerFactory {
  fun create(): DartAnalyzeRunner
}

internal open class DartAnalyzeGlobalInspection(
  private val runnerFactory: DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory { DartExecutableRunner() },
) : GlobalInspectionTool() {
  private val problemsByReference = mutableMapOf<RefEntity, MutableList<DartProblem>>()

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
    val projectPath = Path.of(globalContext.project.basePath ?: return)
    val analysisDirectory = scope.analysisDirectory()
    if (analysisDirectory == null) {
      LOG.info("Skipping Dart analysis: no in-scope Dart files were found")
      return
    }

    val directoryPath: @NlsSafe String = analysisDirectory.toQodanaLogPath(projectPath)
    val runner = runnerFactory.create()
    val sdkFailure = runBlockingCancellable { runner.checkSdk(projectPath) }
    if (sdkFailure != null) {
      LOG.warn("Dart SDK check failed for '$directoryPath': ${sdkFailure.message}; other inspections will continue")
      registerDartProblem(sdkFailure, projectPath, manager, globalContext, problemDescriptionsProcessor, sanityFailure = true)
      return
    }
    val job = JobDescriptor(QodanaDartBundle.message("inspection.dart.analyze.progress"))
    job.totalAmount = 1

    try {
      LOG.info("Analyzing Dart project at '$directoryPath'")
      val result = runBlockingCancellable { runner.analyze(analysisDirectory) }
      result.problems.forEach { problem ->
        collectProblem(problem, analysisDirectory, manager, globalContext, problemDescriptionsProcessor)
      }
      runReadActionBlocking {
        super.runInspection(scope, manager, globalContext, problemDescriptionsProcessor)
      }
      LOG.info("Finished analyzing Dart project at '$directoryPath'")
      globalContext.incrementJobDoneAmount(job, directoryPath)
    }
    finally {
      problemsByReference.clear()
    }
  }

  override fun checkElement(
    refEntity: RefEntity,
    scope: AnalysisScope,
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
  ): Array<CommonProblemDescriptor>? {
    val problems = problemsByReference[refEntity] ?: return null
    val psiFile = (refEntity as? RefElement)?.psiElement as? PsiFile ?: return null
    val document = PsiDocumentManager.getInstance(globalContext.project).getDocument(psiFile) ?: return null
    return problems.mapNotNull { problem ->
      val location = problem.location ?: return@mapNotNull null
      DartProblemDescriptor(
        psiFile,
        location.rangeIn(document),
        problem.description(),
        problem.severity.toProblemHighlightType(),
        problem.code,
      )
    }.toTypedArray()
  }

  private fun collectProblem(
    problem: DartProblem,
    projectPath: Path,
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    val location = problem.location
    val file = location?.file
    if (location == null || file == null) {
      return registerDartProblem(
        DartAnalyzeRunResult.Failure(problem.description()),
        projectPath,
        manager,
        globalContext,
        problemDescriptionsProcessor,
      )
    }
    val path = Path.of(file).let { if (it.isAbsolute) it else projectPath.resolve(it) }
    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
      ?: run {
        LOG.warn("Unable to find analyzed file: '${path.toQodanaLogPath(projectPath)}'")
        return
      }
    runReadActionBlocking {
      val psiFile = PsiManager.getInstance(globalContext.project).findFile(virtualFile)
        ?: run {
          LOG.warn("Unable to create PSI for analyzed file: '${path.toQodanaLogPath(projectPath)}'")
          return@runReadActionBlocking
        }
      val reference = globalContext.refManager.getReference(psiFile) ?: return@runReadActionBlocking
      problemsByReference.getOrPut(reference) { mutableListOf() }.add(problem)
    }
  }

  companion object {
    private val LOG = logger<DartAnalyzeGlobalInspection>()
  }
}

/** Exports the problem under its derived inspection ID, for example `dart_unused_local_variable`. */
private class DartProblemDescriptor(
  psiFile: PsiFile,
  range: TextRange,
  description: @InspectionMessage String,
  highlightType: ProblemHighlightType,
  private val inspectionCode: String,
) : ProblemDescriptorBase(psiFile, psiFile, description, LocalQuickFix.EMPTY_ARRAY, highlightType, false, range, true, false),
    ExportedInspectionsResultModifier {
  override fun modifyResult(element: Element) {
    element.getChild(INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)
      .setAttribute(INSPECTION_RESULTS_ID_ATTRIBUTE, dartInspectionId(inspectionCode))
  }
}

internal fun registerDartProblem(
  failure: DartAnalyzeRunResult.Failure,
  analysisDirectory: Path,
  manager: InspectionManager,
  globalContext: GlobalInspectionContext,
  problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  sanityFailure: Boolean = false,
) {
  val directoryPath: @NlsSafe String = analysisDirectory.toQodanaLogPath(Path.of(globalContext.project.basePath ?: return))
  runReadActionBlocking {
    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(analysisDirectory) ?: return@runReadActionBlocking
    val directory = PsiManager.getInstance(globalContext.project).findDirectory(virtualFile) ?: return@runReadActionBlocking
    val reference = globalContext.refManager.getReference(directory) ?: return@runReadActionBlocking
    val descriptor = manager.createProblemDescriptor(QodanaDartBundle.message("dart.analyze.error.module", directoryPath, failure.message))
    problemDescriptionsProcessor.addProblemElement(reference, if (sanityFailure) DartSanityProblemDescriptor(descriptor) else descriptor)
  }
}

internal fun AnalysisScope.containsDartFiles(): Boolean = runReadActionBlocking {
  FilenameIndex.getAllFilesByExt(project, "dart", toSearchScope() as GlobalSearchScope).isNotEmpty()
}

internal fun AnalysisScope.analysisDirectory(): Path? = runReadActionBlocking {
  val projectPath = Path.of(project.basePath ?: return@runReadActionBlocking null)
  val files = FilenameIndex.getAllFilesByExt(project, "dart", toSearchScope() as GlobalSearchScope)
    .filter { file -> projectPath.relativize(file.toNioPath()).parent?.none { it.toString().startsWith(".") } != false }
  VfsUtil.getCommonAncestor(files.mapNotNull { it.parent })?.toNioPath()
}

@Suppress("HardCodedStringLiteral")
private fun DartProblem.description(): @InspectionMessage String = buildString {
  append('[').append(code).append("] ").append(message)
  correction?.takeIf(String::isNotBlank)?.let { append('\n').append(it) }
}

private fun String.toProblemHighlightType(): ProblemHighlightType =
  when (uppercase()) {
    "ERROR" -> ProblemHighlightType.ERROR
    "INFO" -> ProblemHighlightType.INFORMATION
    else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
  }
