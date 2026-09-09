package org.jetbrains.qodana.cpp

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.DefaultCoverageFileProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.intellij.rt.coverage.data.ProjectData
import com.jetbrains.cidr.cpp.execution.coverage.CMakeCoverageEngine
import com.jetbrains.cidr.execution.coverage.CidrCoverageEngine
import com.jetbrains.cidr.execution.coverage.CidrCoverageSuite
import com.jetbrains.cidr.execution.coverage.gcov.GCovCoverageRunner
import com.jetbrains.cidr.execution.coverage.llvm.LLVMCoverageRunner
import com.jetbrains.cidr.lang.CLanguageKind
import com.jetbrains.cidr.lang.OCFileTypeHelpers
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.CoverageInspectionBase
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.issueWithCoverage
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.loadClassData
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.loadMissingData
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.normalizeFilePath
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.removePrefixFromCoverage
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverage
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.reportElement
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.reportProblemsNeeded
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name

class CppCoverageInspection : CoverageInspectionBase() {
  @Suppress("MemberVisibilityCanBePrivate")
  var fileThreshold: Int = 50

  override fun loadCoverage(globalContext: QodanaGlobalInspectionContext) {
    globalContext.putUserData(cppCoverage, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      computeCoverageData(globalContext, CMakeCoverageEngine::class, CppCoverageFileProvider)?.let {
        remapCoverage(globalContext.project, it)
      }?.also { report ->
        globalContext.putUserData(normalizedPaths, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
          report.classes.keys.associateBy { normalizeFilePath(it) }
        })
      }
    })
  }

  override fun loadReportForIncrementalAnalysis(globalContext: QodanaGlobalInspectionContext) {
    globalContext.getUserData(cppCoverage)?.value?.let { loadReportData(globalContext, it) }
  }

  override fun retrieveCoverageDataForInspection(
    engine: CoverageEngine,
    coverageFiles: List<Path>,
    globalContext: QodanaGlobalInspectionContext,
  ): ProjectData? {
    val cidrEngine = engine as? CidrCoverageEngine ?: return null
    return retrieveCppCoverageData(cidrEngine, coverageFiles, globalContext.project)
  }

  override fun checker(file: PsiFile, problemsHolder: ProblemsHolder, globalContext: QodanaGlobalInspectionContext) {
    val report = globalContext.getUserData(cppCoverage)?.value ?: return
    val pathsMap = globalContext.getUserData(normalizedPaths)?.value ?: return
    val data = getClassData(file, report, pathsMap)
    loadClassData(data, file.virtualFile, globalContext)
    if (!reportProblemsNeeded(globalContext, file, file.textRange)) return
    val isDataLoaded = data != null || loadMissingData(
      problemsHolder.project,
      file.textRange,
      file,
      warnMissingCoverage,
      globalContext,
    )
    if (isDataLoaded && issueWithCoverage(data, file, file.textRange, problemsHolder.project, fileThreshold, warnMissingCoverage)) {
      reportElement(
        problemsHolder,
        file.firstChild ?: file,
        QodanaBundle.message("file.coverage.below.threshold", file.virtualFile.presentableName, fileThreshold),
      )
    }
  }

  override fun validateFileType(file: PsiFile): Boolean {
    return OCFileTypeHelpers.getLanguageKind(file.name) in setOf(CLanguageKind.C, CLanguageKind.CPP)
  }

  override fun cleanup(globalContext: QodanaGlobalInspectionContext) {
    globalContext.getUserData(cppCoverage)?.value?.let { data ->
      saveCoverageData(
        globalContext,
        getCoverageDirectory(CMakeCoverageEngine::class.java),
        removePrefixFromCoverage(data, globalContext.config.projectPath),
      )
    }
    globalContext.putUserData(cppCoverage, null)
    globalContext.putUserData(normalizedPaths, null)
  }

  override fun getOptionsPane(): OptPane {
    return OptPane.pane(
      OptPane.number("fileThreshold", QodanaBundle.message("file.coverage.threshold.value"), 1, 100),
      missingCoverageOpt(),
    )
  }
}

private val cppCoverage = Key.create<Lazy<ProjectData?>>("qodana.cpp.coverage")
private val normalizedPaths = Key.create<Lazy<Map<String, String>>>("qodana.cpp.normalizedPaths")

internal fun retrieveCppCoverageData(
  engine: CidrCoverageEngine,
  coverageFiles: List<Path>,
  project: Project,
): ProjectData? {
  val suites = coverageFiles.mapNotNull { coverageFile ->
    val runner = when (coverageFile.extension) {
      "gcov" -> GCovCoverageRunner()
      "json" -> LLVMCoverageRunner()
      else -> return@mapNotNull null
    }
    if (!runner.canBeLoaded(coverageFile) || !runner.acceptsCoverageEngine(engine)) return@mapNotNull null
    CidrCoverageSuite(engine, coverageFile.name, project, runner, DefaultCoverageFileProvider(coverageFile), -1)
  }
  if (suites.isEmpty()) return null
  return CoverageSuitesBundle(suites.toTypedArray()).coverageData
}
