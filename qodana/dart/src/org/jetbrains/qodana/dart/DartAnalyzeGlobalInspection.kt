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
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolIssue
import java.nio.file.Path

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

    val allModules = findDartModules(qodanaContext.config.projectPath)
    val modules = allModules.filter { it.intersects(scope, allModules) }
    val reportedDiagnostics = HashSet<String>()
    val job = JobDescriptor(QodanaDartBundle.message("inspection.dart.analyze.progress"))
    job.totalAmount = modules.size

    for (module in modules) {
      LOG.info("Found ${module.type.name} module at '${module.path.toQodanaLogPath(qodanaContext.config.projectPath)}'")
      LOG.info("Analyzing ${module.type.name} module '${module.path.toQodanaLogPath(qodanaContext.config.projectPath)}'")
      when (val result = runBlockingCancellable { analyze(module) }) {
        is DartAnalyzeRunResult.Success -> {
          result.diagnostics.forEach { diagnostic ->
            if (
              diagnostic.belongsTo(module, allModules) &&
              qodanaContext.isToolEnabled(DartAnalyzeInspectionCatalog.inspectionIdFor(diagnostic.code))
            ) {
              val key = diagnostic.deduplicationKey(module.path)
              if (reportedDiagnostics.add(key)) {
                registerProblem(diagnostic, module.path, manager, globalContext, problemDescriptionsProcessor)
              }
            }
          }
        }
        is DartAnalyzeRunResult.Failure -> {
          if (qodanaContext.isToolEnabled(result.inspectionId)) {
            registerProblem(result, module, qodanaContext)
          }
        }
      }
      LOG.info("Finished analyzing ${module.type.name} module '${module.path.toQodanaLogPath(qodanaContext.config.projectPath)}'")
      val modulePath: @NlsSafe String = module.path.toQodanaLogPath(qodanaContext.config.projectPath)
      globalContext.incrementJobDoneAmount(job, modulePath)
    }
  }

  private suspend fun analyze(module: DartModule): DartAnalyzeRunResult = runnerFactory.create().analyze(module.path)

  private fun DartModule.intersects(scope: AnalysisScope, modules: List<DartModule>): Boolean {
    val moduleRoot = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path) ?: return false
    val nestedModuleRoots = modules.asSequence()
      .filter { it != this && it.path.startsWith(path) }
      .mapNotNull { LocalFileSystem.getInstance().refreshAndFindFileByNioFile(it.path) }
      .toSet()
    var intersects = false
    VfsUtilCore.iterateChildrenRecursively(moduleRoot, { file ->
      !file.isDirectory || file == moduleRoot || file !in nestedModuleRoots
    }) { file ->
      if (!file.isDirectory && scope.contains(file)) {
        intersects = true
        false
      }
      else {
        true
      }
    }
    return intersects
  }

  private fun DartAnalyzeDiagnostic.belongsTo(module: DartModule, modules: List<DartModule>): Boolean {
    val file = location?.file ?: return true
    val filePath = runCatching {
      Path.of(file).let { if (it.isAbsolute) it else module.path.resolve(it) }.toAbsolutePath().normalize()
    }.getOrNull() ?: return true
    val owner = modules
      .map { it.path.toAbsolutePath().normalize() }
      .filter { filePath.startsWith(it) }
      .maxByOrNull(Path::getNameCount)
    return owner == null || owner == module.path.toAbsolutePath().normalize()
  }

  private fun DartAnalyzeDiagnostic.deduplicationKey(modulePath: Path): String {
    val filePath = location?.file?.let { file ->
      runCatching {
        Path.of(file).let { if (it.isAbsolute) it else modulePath.resolve(it) }.toAbsolutePath().normalize()
      }.getOrNull()
    } ?: modulePath.toAbsolutePath().normalize()
    return listOf(code, filePath, location?.offset, location?.length).joinToString("\u0000")
  }

  private fun registerProblem(
    failure: DartAnalyzeRunResult.Failure,
    module: DartModule,
    qodanaContext: QodanaGlobalInspectionContext,
  ) {
    val modulePath: @NlsSafe String = module.path.toQodanaLogPath(qodanaContext.config.projectPath)
    val issue = ExternalToolIssue(
      failure.inspectionId,
      HighlightDisplayLevel.ERROR,
      QodanaDartBundle.message("dart.analyze.error.module", modulePath, failure.message),
      module = modulePath,
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
        DartAnalyzeRunResult.Failure(diagnostic.description(), DartAnalyzeInspectionCatalog.inspectionIdFor(diagnostic.code)),
        DartModule(projectPath, DartModuleType.DART),
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
      if (!qodanaContext.isFileInInspectionScope(DartAnalyzeInspectionCatalog.inspectionIdFor(diagnostic.code), psiFile)) return@runReadActionBlocking
      val descriptor = manager.createProblemDescriptor(
        psiFile,
        location.rangeIn(PsiDocumentManager.getInstance(globalContext.project).getDocument(psiFile) ?: return@runReadActionBlocking),
        diagnostic.description(),
        diagnostic.severity.toProblemHighlightType(),
        false,
      )
      descriptor.setProblemGroup(ProblemGroup { DartAnalyzeInspectionCatalog.inspectionIdFor(diagnostic.code) })
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

private fun QodanaGlobalInspectionContext.hasEnabledDartAnalyzeTool(): Boolean = effectiveProfile.tools.any { it.isEnabled && it.shortName.isDartAnalyzeTool() }

private fun QodanaGlobalInspectionContext.isFileInInspectionScope(inspectionId: String, psiFile: PsiFile): Boolean {
  val tools = effectiveProfile.getToolsOrNull(inspectionId, project) ?: return false
  return tools.getEnabledTool(psiFile, true) != null
}

private fun QodanaGlobalInspectionContext.isToolEnabled(shortName: String): Boolean =
  effectiveProfile.tools.any { it.isEnabled && it.shortName == shortName }

internal fun String.isDartAnalyzeTool(): Boolean =
  this == DART_PROJECT_SETUP_SHORT_NAME || this == DART_ANALYZE_UNCATEGORIZED_SHORT_NAME || startsWith("$DART_ANALYZE_SHORT_NAME.")

@Suppress("HardCodedStringLiteral")
private fun DartAnalyzeDiagnostic.description(): @InspectionMessage String = buildString {
  append('[').append(code).append("] ").append(message)
  correction?.takeIf(String::isNotBlank)?.let { append('\n').append(it) }
}

private fun String.toProblemHighlightType(): ProblemHighlightType =
  if (equals("ERROR", ignoreCase = true)) ProblemHighlightType.ERROR else ProblemHighlightType.GENERIC_ERROR_OR_WARNING
