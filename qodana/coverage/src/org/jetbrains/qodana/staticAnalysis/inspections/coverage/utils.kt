package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.coverage.*
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineCoverage
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.XMLProjectData
import com.intellij.util.containers.ComparatorUtil.min
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.COVERAGE_DATA
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists


internal val CLASS_ENGINES = setOf("JavaCoverageEngine", "XMLReportEngine")
internal val COVERAGE_INSPECTIONS_NAMES = setOf("JsCoverageInspection", "JvmCoverageInspection", "PhpCoverageInspection",
                                       "PyCoverageInspection", "GoCoverageInspection", "NetCoverageInspection")

fun normalizeFilePath(path: String): String {
  var filePath = path
  if (SystemInfo.isWindows) {
    filePath = StringUtil.toLowerCase(filePath)
  }
  return FileUtil.toSystemIndependentName(filePath)
}

fun PsiFile.iterateContents(visitor: PsiElementVisitor) {
  this.accept(object : PsiRecursiveElementVisitor() {
    override fun visitElement(element: PsiElement) {
      ProgressManager.checkCanceled()
      var child = element.firstChild
      while (child != null) {
        child.accept(visitor)
        child.accept(this)
        child = child.nextSibling
      }
    }
  })
}

fun reportElement(problemsHolder: ProblemsHolder, element: PsiElement, @InspectionMessage problem: String) {
  problemsHolder.registerProblem(computeProblemDescriptor(element, problem))
}

fun issueWithCoverage(data: ClassData?, psiFile: PsiFile, textRange: TextRange, project: Project, threshold: Int,
                      warnMissingCoverage: Boolean, fullScanNeeded: Boolean = false): Boolean {
  if (data == null) {
    return warnMissingCoverage
  }
  val doc = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return false
  return computeCoverage(data, doc, textRange, fullScanNeeded) < threshold
}

fun loadClassData(data: ClassData?, virtualFile: VirtualFile, globalContext: QodanaGlobalInspectionContext) {
  if (data == null) return
  val stat = globalContext.coverageStatisticsData
  stat.loadClassData(data, virtualFile)
}

internal fun computeCoverage(data: ClassData, document: Document, textRange: TextRange, fullScanNeeded: Boolean = false): Int {
  val startOffset = textRange.startOffset
  val endOffset = textRange.endOffset
  val startLineNumber = document.getLineNumber(startOffset) + 1
  val endLineNumber = document.getLineNumber(endOffset) + 1
  val maxStoredLine = data.lines.size - 1
  var totalLines = 0
  var coveredLines = 0
  var firstLine = true
  for (i in startLineNumber..min(maxStoredLine, endLineNumber)) {
    val lineData = data.getLineData(i)
    if (lineData != null) {
      totalLines++
      if (lineData.status.toByte() != LineCoverage.NONE) {
        coveredLines++
      } else if (firstLine && !fullScanNeeded) { //if first line is not covered then no need to look further
        return 0
      }
      firstLine = false
    }
  }

  if (totalLines == 0) return 100
  return coveredLines * 100 / totalLines
}

internal fun retrieveCoverageData(engine: CoverageEngine, coverageFiles: List<File>, globalContext: QodanaGlobalInspectionContext): ProjectData? {
  val suites = computeSuites(engine, coverageFiles, globalContext.project)
  if (!suites.any()) return null
  val bundle = CoverageSuitesBundle(suites.toTypedArray())
  if (bundle.coverageData == null) return null
  if (!CLASS_ENGINES.contains(engine.javaClass.simpleName)) {
    if (bundle.coverageData!!.classes.isEmpty()) {
      logger.error("Coverage reports were empty or not loaded correctly")
      return null
    }
    // it's a file based coverage, we need to fix the
    remapCoverage(globalContext.project, bundle)
  }
  return bundle.coverageData
}

fun computeSuites(engine: CoverageEngine, coverageFiles: List<File>, project: Project): List<CoverageSuite> {
  val runnersAndCovers = mutableListOf<Pair<CoverageRunner, File>>()
  for (coverageFile in coverageFiles) {
    val runner = getCoverageRunner(coverageFile, engine) ?: continue
    if (runner.acceptsCoverageEngine(engine)) runnersAndCovers.add(Pair(runner, coverageFile))
  }
  logger.info("Engine ${engine.presentableText} - accepted ${runnersAndCovers.size} files")
  if (runnersAndCovers.any() && System.getProperty("qodana.coverage.debug.info", "false").toBoolean()) {
    logger.info("Engine ${engine.presentableText} - accepted : " + runnersAndCovers.joinToString(", ") { "${it.first.presentableName} - ${it.second.path}" })
  }
  val suites = mutableListOf<CoverageSuite>()
  for ((runner, cover) in runnersAndCovers) {
    val fileProvider: CoverageFileProvider = DefaultCoverageFileProvider(cover.path)
    val suite = engine.createCoverageSuite(cover.name, project, runner, fileProvider, -1) ?: continue
    suites.add(suite)
  }
  logger.info("Engine ${engine.presentableText} - loaded ${suites.size} coverage suites")
  return suites
}

fun removePrefixFromCoverage(data: ProjectData, prefix: Path): ProjectData {
  val newData = ProjectData()
  data.classes.forEach { (name: String, oldClass: ClassData) ->
    val newClass = newData.getOrCreateClassData(prefix.relativize(Path.of(name)).toString())
    newClass.setLines(oldClass.lines as Array<LineData?>)
  }
  return newData
}

fun remapCoverageFromCloud(bundle: CoverageSuitesBundle): CoverageSuitesBundle? {
  val projectDir = bundle.project.guessProjectDir() ?: return null
  applyNewDataToBundle(bundle, remapDataToProjectDir(bundle.coverageData!!, projectDir))
  return bundle
}

fun reportProblemsNeeded(globalContext: QodanaGlobalInspectionContext): Boolean {
  return !isLocalChanges(globalContext) && globalContext.config.coverage.reportProblems
}

// used to skip coverage inspection run on old code state
fun isUnderLocalChangesOnOldCode(context: QodanaGlobalInspectionContext): Boolean {
  return context.coverageComputationState().isFirstStage()
}

fun isLocalChanges(context: QodanaGlobalInspectionContext): Boolean {
  return context.coverageComputationState().isIncrementalAnalysis()
}

// SCL-23064 - deprecated API method
@Deprecated("Use isLocalChanges(context: QodanaGlobalInspectionContext) instead", ReplaceWith("LocalChangesService.getInstance(project).isIncrementalAnalysis.toString()"))
fun isLocalChanges(project: Project): Boolean {
  return false
}

fun loadMissingData(project: Project, textRange: TextRange, psiFile: PsiFile, warnMissingCoverage: Boolean,
                    globalContext: QodanaGlobalInspectionContext): Boolean {
  // if the warnMissingCoverage flag is not set, it is not needed to load missing data
  if (warnMissingCoverage) {
    val doc = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return false
    globalContext.coverageStatisticsData.loadMissingData(doc, textRange, psiFile.virtualFile)
    return true
  }
  return false
}

@Suppress("unused") //used in Scala plugin
fun loadXmlLineData(fileInfo: XMLProjectData.FileInfo, virtualFile: VirtualFile, globalContext: QodanaGlobalInspectionContext) {
  val stat = globalContext.coverageStatisticsData
  stat.loadXmlLineData(fileInfo, virtualFile)
}

internal fun reportsInProjectPath(project: Project): Path? {
  val projectPath = project.guessProjectDir()?.toNioPath() ?: return null
  val reportsPath = projectPath.resolve(".qodana").resolve("code-coverage")
  if (reportsPath.exists()) return reportsPath.toAbsolutePath()
  return null
}

internal fun reportsInExternalPath(): Path? {
  val externalPath = System.getProperty(COVERAGE_DATA) ?: return null
  return Path.of(externalPath)
}

private fun computeProblemDescriptor(element: PsiElement, @InspectionMessage descriptionTemplate: String): ProblemDescriptor {
  val textContent = element.text
  val endOffsetFirstLine = textContent.indexOf('\n').let { if (it == -1) textContent.length else it }
  return InspectionManager.getInstance(element.project)
    .createProblemDescriptor(element, TextRange(0, endOffsetFirstLine), descriptionTemplate, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true)
}

private fun getCoverageRunner(file: File, engine: CoverageEngine): CoverageRunner? {
  for (runner in CoverageRunner.EP_NAME.extensionList) {
    for (extension in runner.dataFileExtensions) {
      if (Comparing.strEqual(file.extension, extension) && runner.canBeLoaded(file) && runner.acceptsCoverageEngine(engine)) return runner
    }
  }
  return null
}

private fun remapCoverage(project: Project, bundle: CoverageSuitesBundle) {
  var data = bundle.coverageData!!
  if (isRelativePath(data.classes.keys.minOf { it })) {
    val projectDir = bundle.project.guessProjectDir() ?: return
    data = remapDataToProjectDir(data, projectDir)
    applyNewDataToBundle(bundle, data)
  } else {
    val rootManager = ProjectRootManager.getInstance(project)
    val roots = ReadAction.compute<Array<VirtualFile>, Throwable> { rootManager.contentRoots }
    for (contentRoot in roots) {
      val files: Set<String> = data.classes.keys
      val pathToRemap = findPathToRemap(files, contentRoot)
      if (pathToRemap != null) {
        // just replace paths for every file
        data = remapData(data, pathToRemap, contentRoot.path)
        applyNewDataToBundle(bundle, data)
      }
    }
  }
}

private fun applyNewDataToBundle(bundle: CoverageSuitesBundle, data: ProjectData) {
  bundle.coverageData = data
  for (suiteSuite in bundle.suites) {
    suiteSuite.setCoverageData(data)
  }
}

fun findPathToRemap(files: Set<String>, contentRoot: VirtualFile): String? {
  val comparator = Comparator.comparingInt { o: String? ->
    StringUtil.countChars(o!!, '/')
  }
  val sortedFiles = ContainerUtil.reverse(ContainerUtil.sorted(files, comparator))
  val suspects: List<String> = sortedFiles.subList(0, Math.min(3, sortedFiles.size))
  var pathToRemap: String? = null
  for (suspect in suspects) {
    val pathParts = suspect.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    var path = ""
    val oldValue = pathToRemap
    for (part in pathParts) {
      path += "$part/"
      val relPath = StringUtil.substringAfter(suspect, path)
      if (relPath != null && contentRoot.findFileByRelativePath(relPath) != null) {
        pathToRemap = StringUtil.substringBeforeLast(path, "/")
        break
      }
    }
    if (oldValue != null && oldValue != pathToRemap) {
      pathToRemap = null
      break
    }
  }
  return pathToRemap
}

private fun remapData(oldData: ProjectData, oldPath: String, newPath: String): ProjectData {
  val newData = ProjectData()
  oldData.classes.forEach { (name: String, oldClass: ClassData) ->
    val newClass = newData.getOrCreateClassData(name.replace(oldPath, newPath))
    newClass.setLines(oldClass.lines as Array<LineData?>)
  }
  return newData
}

private fun isRelativePath(pathString: String): Boolean {
  // Check if the path starts with a Unix root (/) or a Windows root (e.g., C:\ or C:/)
  val unixAbsolute = pathString.startsWith("/")
  val windowsAbsolute = "^[A-Za-z]:[\\\\/].*".toRegex().matches(pathString)

  return !(unixAbsolute || windowsAbsolute)
}

private fun remapDataToProjectDir(oldData: ProjectData, projectPath: VirtualFile): ProjectData {
  val newData = ProjectData()
  oldData.classes.forEach { (name: String, oldClass: ClassData) ->
    val fixedPathFile = projectPath.findFileByRelativePath(FileUtil.toSystemIndependentName(name))
    if (fixedPathFile != null) {
      val newClass = newData.getOrCreateClassData(fixedPathFile.path)
      newClass.setLines(oldClass.lines as Array<LineData?>)
    }
  }
  return newData
}