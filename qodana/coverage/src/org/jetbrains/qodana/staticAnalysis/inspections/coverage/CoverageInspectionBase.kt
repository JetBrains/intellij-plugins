package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalSimpleInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptCheckbox
import com.intellij.codeInspection.options.OptPane
import com.intellij.codeInspection.options.OptPane.checkbox
import com.intellij.codeInspection.options.OptPane.number
import com.intellij.codeInspection.options.OptRegularComponent
import com.intellij.coverage.CoverageEngine
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.roots.TestSourcesFilter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.instrumentation.InstrumentationOptions
import com.intellij.rt.coverage.util.CoverageReport
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.coverage.CoverageLanguage
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.precomputedCoverageFiles
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.stat.CoverageFeatureEventsCollector.COVERAGE_LANGUAGE_FIELD
import org.jetbrains.qodana.staticAnalysis.stat.CoverageFeatureEventsCollector.INPUT_COVERAGE_LOADED
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk
import kotlin.reflect.KClass

internal val logger = logger<CoverageInspectionBase>()

private const val COLLECTED_COVERAGE_REPORTS_FILE = "collected_coverage_reports.txt"

abstract class CoverageInspectionBase : GlobalSimpleInspectionTool() {
  @Suppress("MemberVisibilityCanBePrivate")
  var methodThreshold: Int = 50
  @Suppress("MemberVisibilityCanBePrivate")
  var classThreshold: Int = 50
  @Suppress("MemberVisibilityCanBePrivate")
  var warnMissingCoverage: Boolean = false

  abstract fun loadCoverage(globalContext: QodanaGlobalInspectionContext)

  open fun loadReportForIncrementalAnalysis(globalContext: QodanaGlobalInspectionContext) {}

  protected fun loadReportData(globalContext: QodanaGlobalInspectionContext, data: ProjectData) {
    // TODO: check supported languages?
    if (isLocalChanges(globalContext)) {
      processReportData(data, globalContext)
    }
  }
  abstract fun checker(file: PsiFile, problemsHolder: ProblemsHolder, globalContext: QodanaGlobalInspectionContext)
  abstract fun validateFileType(file: PsiFile): Boolean
  abstract fun cleanup(globalContext: QodanaGlobalInspectionContext)

  override fun inspectionStarted(
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    if (globalContext !is QodanaGlobalInspectionContext
        || isUnderLocalChangesOnOldCode(globalContext)) return
    loadCoverage(globalContext)
    loadReportForIncrementalAnalysis(globalContext)
  }

  override fun checkFile(
    psiFile: PsiFile,
    manager: InspectionManager,
    problemsHolder: ProblemsHolder,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    if (globalContext !is QodanaGlobalInspectionContext
        || isUnderLocalChangesOnOldCode(globalContext)
        || !validateFileType(psiFile)
        || TestSourcesFilter.isTestSources(psiFile.virtualFile, globalContext.project)) return
    checker(psiFile, problemsHolder, globalContext)
  }

  override fun inspectionFinished(
    manager: InspectionManager,
    globalContext: GlobalInspectionContext,
    problemDescriptionsProcessor: ProblemDescriptionsProcessor,
  ) {
    if (globalContext !is QodanaGlobalInspectionContext) return
    cleanup(globalContext)
  }

  override fun getOptionsPane(): OptPane {
    return OptPane.pane(*defaultThresholdOpts().plus(missingCoverageOpt()))
  }

  protected fun defaultThresholdOpts(): Array<OptRegularComponent> {
    return arrayOf(
      number("classThreshold", QodanaBundle.message("class.coverage.threshold.value"), 1, 100),
      number("methodThreshold", QodanaBundle.message("method.coverage.threshold.value"), 1, 100),
    )
  }

  protected fun missingCoverageOpt(): OptCheckbox = checkbox("warnMissingCoverage", QodanaBundle.message("missing.coverage.tracking.message"))

  protected fun getClassData(
    file: PsiFile,
    report: ProjectData,
    pathsMap: Map<String, String>
  ): ClassData? {
    val normalizedPath = normalizeFilePath(file.virtualFile.path)
    val path = pathsMap.getOrDefault(normalizedPath, normalizedPath)
    return report.getClassData(path)
  }

  protected open fun saveCoverageData(
    context: QodanaGlobalInspectionContext,
    fileName: String,
    data: ProjectData,
  ) {
    val fileData = context.config.coverage.coveragePath.resolve(fileName)
    val fileSourceMap = context.config.coverage.coveragePath.resolve("$fileName.sourceMap")
    Files.createDirectories(fileData.parent)
    data.classes.forEach { c ->
      c.value.lines.forEach { l ->
        val ld = l as? LineData
        if (ld != null && ld.methodSignature == null) ld.methodSignature = "()V"
      }
    }
    val options = InstrumentationOptions.Builder().setDataFile(fileData.toFile()).setSourceMapFile(fileSourceMap.toFile()).build()
    CoverageReport.save(data, options)
  }

  protected open fun processReportData(data: ProjectData, globalContext: QodanaGlobalInspectionContext) {
    val stat = globalContext.coverageStatisticsData
    data.classes.forEach { x -> stat.processReportClassData(x.value) }
  }

  fun computeCoverageData(
    globalContext: QodanaGlobalInspectionContext,
    engineType: KClass<out CoverageEngine>,
    coverageFileProvider: QodanaCoverageFileProvider
  ): ProjectData? {
    val coverageFiles = provideCoverageFilesWithDiscovery(globalContext, coverageFileProvider)
    logger.info("Coverage for ${engineType.java.simpleName} - provided ${coverageFiles.size} files")
    if (coverageFiles.isEmpty()) return null
    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(engineType.java)
    val data = retrieveCoverageData(engine, coverageFiles, globalContext) ?: return null
    if (data.classes.isEmpty()) {
      logger.error("The engine ${engine.javaClass.simpleName} couldn't load coverage files")
      return null
    }
    INPUT_COVERAGE_LOADED.log(globalContext.project,
                              COVERAGE_LANGUAGE_FIELD.with(CoverageLanguage.mapEngine(engineType.java.simpleName)))
    return data
  }

  protected fun provideCoverageFilesWithDiscovery(
    globalContext: QodanaGlobalInspectionContext,
    coverageFileProvider: QodanaCoverageFileProvider = EmptyQodanaCoverageFileProvider(),
  ): List<Path> {
    val cache = globalContext.putUserDataIfAbsent(precomputedCoverageFiles, ConcurrentHashMap())
    return cache.computeIfAbsent(coverageFileProvider.engineType) {
      lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        // files stored in .qodana/code-coverage or in directory specified via system variable
        val explicitFiles = explicitCoverageFiles(globalContext)
        if (explicitFiles.isNotEmpty()) {
          reportCollectedReports(globalContext, coverageFileProvider.engineType, explicitFiles)
          return@lazy explicitFiles
        }
        // files found via discovery in common locations
        val filesToCopies = coverageFileProvider.getCoverageFiles(globalContext.project)
        reportCollectedReports(globalContext, coverageFileProvider.engineType, filesToCopies.keys.toList())
        filesToCopies.values.toList()
      }
    }.value
  }

  @OptIn(ExperimentalPathApi::class)
  private fun explicitCoverageFiles(globalContext: QodanaGlobalInspectionContext): List<Path> {
    return sequenceOf(reportsInExternalPath(), reportsInProjectPath(globalContext.project))
      .filterNotNull()
      .flatMap { it.walk().filter { f -> f.isRegularFile() } }
      .toList()
  }

  @Suppress("IO_FILE_USAGE")
  @Deprecated(
    message = "Use provideCoverageFilesWithDiscovery(QodanaGlobalInspectionContext, QodanaCoverageFileProvider) instead",
    replaceWith = ReplaceWith("provideCoverageFilesWithDiscovery(globalContext)")
  )
  protected fun provideCoverageFiles(globalContext: QodanaGlobalInspectionContext): List<java.io.File> {
    return explicitCoverageFiles(globalContext).map { it.toFile() }
  }

  protected fun highlightedElement(element: PsiElement): PsiElement {
    return (element as? PsiNameIdentifierOwner)?.nameIdentifier ?: element
  }

  companion object {

    /**
     * Returns the directory name for the given coverage engine class without file extension
     */
    fun getCoverageDirectory(coverageEngine: Class<out CoverageEngine>): String = coverageEngine.simpleName
  }
}

/**
 * Records every coverage report used for [engineType] into a user-visible artifact
 * ([COLLECTED_COVERAGE_REPORTS_FILE] under the run output directory) and echoes the first paths to stdout
 */
private fun reportCollectedReports(
  globalContext: QodanaGlobalInspectionContext,
  engineType: CoverageEngineType,
  files: List<Path>,
) {
  if (files.isEmpty()) return
  try {
    val reportFile = globalContext.getOutputPath().resolve(COLLECTED_COVERAGE_REPORTS_FILE)
    reportFile.parent?.createDirectories()
    Files.write(reportFile, files.map { "$engineType\t$it" }, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  }
  catch (e: IOException) {
    logger.warn("Failed to write $COLLECTED_COVERAGE_REPORTS_FILE", e)
  }
  val reporter = QodanaMessageReporter.DEFAULT
  reporter.reportMessage(1, "Collected ${files.size} coverage report(s) for $engineType:")
  files.take(10).forEach { reporter.reportMessage(1, "  $it") }
  if (files.size > 10) reporter.reportMessage(1, "  ... and ${files.size - 10} more")
}