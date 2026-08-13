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
import com.intellij.lang.annotation.ProblemGroup
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import java.nio.file.Path
import kotlin.math.min

internal fun interface DartAnalyzeRunnerFactory {
  fun create(moduleType: DartModuleType): DartAnalyzeRunner
}

internal open class DartAnalyzeGlobalInspection(
  private val runnerFactory: DartAnalyzeRunnerFactory = DartAnalyzeRunnerFactory { moduleType ->
    when (moduleType) {
      DartModuleType.DART -> DartExecutableRunner()
      DartModuleType.FLUTTER -> FlutterExecutableRunner()
    }
  },
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

    val modules = findDartModules(qodanaContext.config.projectPath)
    val reportedDiagnostics = HashSet<String>()
    val job = JobDescriptor(QodanaDartBundle.message("inspection.dart.analyze.progress"))
    job.totalAmount = modules.size

    for (module in modules) {
      LOG.info("Found ${module.type.name} module at '${module.path.toQodanaLogPath(qodanaContext.config.projectPath)}'")
      val preparation = runBlockingCancellable { prepare(module) }
      if (preparation is DartPreparationResult.Failure) {
        registerProblem(preparation.failure, module, manager, globalContext, problemDescriptionsProcessor)
        val modulePath: @NlsSafe String = module.path.toQodanaLogPath(qodanaContext.config.projectPath)
        globalContext.incrementJobDoneAmount(job, modulePath)
        continue
      }

      LOG.info("Analyzing ${module.type.name} module '${module.path.toQodanaLogPath(qodanaContext.config.projectPath)}'")
      when (val result = runBlockingCancellable { analyze(module) }) {
        is DartAnalyzeRunResult.Success -> {
          result.diagnostics.forEach { diagnostic ->
            if (diagnostic.belongsTo(module, modules) && qodanaContext.isToolEnabled(DartAnalyzeInspectionCatalog.inspectionIdFor(diagnostic.code))) {
              val location = diagnostic.location
              val key = listOf(
                DartAnalyzeInspectionCatalog.inspectionIdFor(diagnostic.code),
                location?.file,
                location?.offset,
                location?.length,
              ).joinToString("\u0000")
              if (reportedDiagnostics.add(key)) {
                registerProblem(diagnostic, module.path, manager, globalContext, problemDescriptionsProcessor)
              }
            }
          }
        }
        is DartAnalyzeRunResult.Failure -> {
          if (qodanaContext.isToolEnabled(result.inspectionId)) {
            registerProblem(result, module, manager, globalContext, problemDescriptionsProcessor)
          }
        }
      }
      LOG.info("Finished analyzing ${module.type.name} module '${module.path.toQodanaLogPath(qodanaContext.config.projectPath)}'")
      val modulePath: @NlsSafe String = module.path.toQodanaLogPath(qodanaContext.config.projectPath)
      globalContext.incrementJobDoneAmount(job, modulePath)
    }
  }

  private suspend fun prepare(module: DartModule): DartPreparationResult = when (module.type) {
    DartModuleType.DART, DartModuleType.FLUTTER -> runnerFactory.create(module.type).prepare(module.path)
  }

  private suspend fun analyze(module: DartModule): DartAnalyzeRunResult = runnerFactory.create(module.type).analyze(module.path)

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

  @Suppress("HardCodedStringLiteral")
  private fun registerProblem(
    failure: DartAnalyzeRunResult.Failure,
    module: DartModule,
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    runReadActionBlocking {
      val psiFile = findPsiFile(module.path.resolve("pubspec.yaml"), globalContext)
      if (psiFile == null) {
        return@runReadActionBlocking
      }
      val message: @InspectionMessage String = failure.message
      val descriptor = manager.createProblemDescriptor(
        psiFile,
        TextRange(0, min(1, psiFile.textLength)),
        message,
        ProblemHighlightType.ERROR,
        false,
      )
      descriptor.setProblemGroup(ProblemGroup { failure.inspectionId })
      // Setup failures are project-level problems when the anchor file is not part of the reference graph.
      val reference = globalContext.refManager.getReference(psiFile) ?: globalContext.refManager.refProject
      problemDescriptionsProcessor.addProblemElement(reference, descriptor)
    }
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
        manager,
        globalContext,
        problemDescriptionsProcessor,
      )
    }
    val path = Path.of(file).let { if (it.isAbsolute) it else projectPath.resolve(it) }
    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
      ?: run {
        LOG.error("Unable to find Dart analyzer diagnostic file '${path.toQodanaLogPath(projectPath)}'")
        return
      }
    runReadActionBlocking {
      val psiFile = PsiManager.getInstance(globalContext.project).findFile(virtualFile)
        ?: run {
          LOG.error("Unable to create PSI file for Dart analyzer diagnostic file '${path.toQodanaLogPath(projectPath)}'")
          return@runReadActionBlocking
        }
      val qodanaContext = globalContext as? QodanaGlobalInspectionContext ?: return@runReadActionBlocking
      if (!qodanaContext.isFileInInspectionScope(DartAnalyzeInspectionCatalog.inspectionIdFor(diagnostic.code), psiFile)) return@runReadActionBlocking
      val descriptor = manager.createProblemDescriptor(
        psiFile,
        location.rangeIn(psiFile.textLength),
        diagnostic.description(),
        diagnostic.severity.toProblemHighlightType(),
        false,
      )
      descriptor.setProblemGroup(ProblemGroup { DartAnalyzeInspectionCatalog.inspectionIdFor(diagnostic.code) })
      val reference = globalContext.refManager.getReference(psiFile)
      problemDescriptionsProcessor.addProblemElement(reference, descriptor)
    }
  }

  private fun findPsiFile(path: Path, globalContext: GlobalInspectionContext) = run {
    LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)
      ?.let { PsiManager.getInstance(globalContext.project).findFile(it) }
  }

  private fun DartAnalyzeLocation.rangeIn(textLength: Int): TextRange {
    val start = offset?.coerceIn(0, textLength) ?: 0
    val end = min(start + (length ?: 0), textLength)
    return if (start == end && textLength > 0) TextRange(0, 1) else TextRange(start, end)
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
  if (config.exclude.isEmpty()) return true
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
