package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.codeInspection.*
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
import org.jetbrains.qodana.coverage.CoverageLanguage
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.precomputedCoverageFiles
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.stat.CoverageFeatureEventsCollector.INSPECTION_LOADED_COVERAGE
import java.io.File
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isRegularFile
import kotlin.io.path.walk
import kotlin.reflect.KClass

internal val logger = logger<CoverageInspectionBase>()

abstract class CoverageInspectionBase: GlobalSimpleInspectionTool() {
  @Suppress("MemberVisibilityCanBePrivate")
  var methodThreshold = 50

  @Suppress("MemberVisibilityCanBePrivate")
  var classThreshold = 50

  @Suppress("MemberVisibilityCanBePrivate")
  var warnMissingCoverage = false

  abstract fun loadCoverage(globalContext: QodanaGlobalInspectionContext)
  abstract fun checker(file: PsiFile, problemsHolder: ProblemsHolder, globalContext: QodanaGlobalInspectionContext)
  abstract fun validateFileType(file: PsiFile): Boolean
  abstract fun cleanup(globalContext: QodanaGlobalInspectionContext)

  override fun inspectionStarted(manager: InspectionManager,
                                 globalContext: GlobalInspectionContext,
                                 problemDescriptionsProcessor: ProblemDescriptionsProcessor) {
    if (globalContext !is QodanaGlobalInspectionContext
        || isUnderLocalChangesOnOldCode(globalContext)) return
    loadCoverage(globalContext)
  }

  override fun checkFile(file: PsiFile,
                         manager: InspectionManager,
                         problemsHolder: ProblemsHolder,
                         globalContext: GlobalInspectionContext,
                         problemDescriptionsProcessor: ProblemDescriptionsProcessor) {
    if (globalContext !is QodanaGlobalInspectionContext
        || isUnderLocalChangesOnOldCode(globalContext)
        || !validateFileType(file)
        || TestSourcesFilter.isTestSources(file.virtualFile, globalContext.project)) return
    checker(file, problemsHolder, globalContext)
  }

  override fun inspectionFinished(manager: InspectionManager,
                                  globalContext: GlobalInspectionContext,
                                  problemDescriptionsProcessor: ProblemDescriptionsProcessor) {
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

  protected fun missingCoverageOpt() = checkbox("warnMissingCoverage", QodanaBundle.message("missing.coverage.tracking.message"))

  protected fun getClassData(
    file: PsiFile,
    report: ProjectData,
    pathsMap: Map<String, String>
  ): ClassData? {
    val normalizedPath = normalizeFilePath(file.virtualFile.path)
    val path = pathsMap.getOrDefault(normalizedPath, normalizedPath)
    return report.getClassData(path)
  }

  protected fun saveCoverageData(context: QodanaGlobalInspectionContext,
                                 engine: String,
                                 data: ProjectData) {
    val fileData = context.config.coverage.coveragePath.resolve(engine)
    val fileSourceMap = context.config.coverage.coveragePath.resolve("$engine.sourceMap")
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

  fun computeCoverageData(globalContext: QodanaGlobalInspectionContext, engineType : KClass<out CoverageEngine>): ProjectData? {
    val coverageFiles = provideCoverageFiles(globalContext)
    logger.info("Coverage for ${engineType.java.simpleName} - provided ${coverageFiles.size} files")
    if (!coverageFiles.isEmpty()) {
      val engine = CoverageEngine.EP_NAME.findExtensionOrFail(engineType.java)
      val data = retrieveCoverageData(engine, coverageFiles, globalContext)
      if (data != null) {
        if (isLocalChanges(globalContext)) {
          processReportData(data, globalContext)
        }
        INSPECTION_LOADED_COVERAGE.log(globalContext.project, CoverageLanguage.mapEngine(engineType.java.simpleName))
        return data
      }
    }
    return null
  }

  @OptIn(ExperimentalPathApi::class)
  protected fun provideCoverageFiles(globalContext: QodanaGlobalInspectionContext): List<File> = synchronized(globalContext) {
    globalContext.getUserData(precomputedCoverageFiles)?.let { return@synchronized it }
    val coverageFiles = sequenceOf(reportsInExternalPath(), reportsInProjectPath(globalContext.project))
      .filterNotNull()
      .flatMap { it.walk().filter { f -> f.isRegularFile() }.map { f -> f.toFile() } }
      .toList()

    globalContext.putUserData(precomputedCoverageFiles, coverageFiles)
    return coverageFiles
  }

  protected fun highlightedElement(element: PsiElement): PsiElement {
    return (element as? PsiNameIdentifierOwner)?.nameIdentifier ?: element
  }
}