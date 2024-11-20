package org.jetbrains.qodana.jvm.coverage

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.JavaCoverageEngine
import com.intellij.coverage.xml.XMLReportEngine
import com.intellij.coverage.xml.XMLReportSuite
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.report.XMLProjectData
import com.intellij.uast.UastHintedVisitorAdapter
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coverage.CoverageLanguage
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.stat.CoverageFeatureEventsCollector
import org.jetbrains.uast.*
import org.jetbrains.uast.java.UastAnonymousClassUtil
import org.jetbrains.uast.visitor.AbstractUastNonRecursiveVisitor
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

internal val logger = logger<JvmCoverageInspection>()

class JvmCoverageInspection : CoverageInspectionBase() {
  private val languages = setOf("Java", "Kotlin")

  companion object {
    private val javacov = Key.create<Lazy<ProjectData?>>("qodana.javacov.coverage")
    private val xmlcov = Key.create<Lazy<XMLProjectData?>>("qodana.xml.coverage")
  }

  override fun loadCoverage(globalContext: QodanaGlobalInspectionContext) {
    globalContext.putUserData(javacov, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      computeCoverageData(globalContext, JavaCoverageEngine::class)
    })
    globalContext.putUserData(xmlcov, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      val data = computeXmlCoverageData(globalContext, XMLReportEngine::class)
      if (data != null) {
        CoverageFeatureEventsCollector.INSPECTION_LOADED_COVERAGE.log(globalContext.project, CoverageLanguage.JVM)
      }
      data
    })
  }

  override fun processReportData(data: ProjectData, globalContext: QodanaGlobalInspectionContext) {
    val stat = globalContext.coverageStatisticsData
    val searchScope = GlobalSearchScope.projectScope(globalContext.project)
    data.classes.filter {
      JavaPsiFacade.getInstance(globalContext.project).findClass(it.value.name, searchScope) != null
    }.forEach { x -> stat.processReportClassData(x.value) }
  }

  override fun checker(file: PsiFile, problemsHolder: ProblemsHolder, globalContext: QodanaGlobalInspectionContext) {
    val report = globalContext.getUserData(javacov)?.value
    val xmlReport = globalContext.getUserData(xmlcov)?.value
    if (report == null && xmlReport == null) return
    file.iterateContents(UastHintedVisitorAdapter.create(
      file.language,
      JVMDefVisitor(globalContext, problemsHolder, methodThreshold, classThreshold, warnMissingCoverage, report, xmlReport, ::highlightedElement),
      arrayOf(UClass::class.java, UMethod::class.java),
      true
    ))
  }

  override fun validateFileType(file: PsiFile) = languages.contains(file.language.displayName)

  override fun cleanup(globalContext: QodanaGlobalInspectionContext) {
    val data = globalContext.getUserData(javacov)?.value
    if (data != null) {
      saveCoverageData(globalContext, JavaCoverageEngine::class.java.simpleName, data)
    }
    globalContext.putUserData(javacov, null)
    globalContext.putUserData(xmlcov, null)
  }

  private class JVMDefVisitor(private val globalContext: QodanaGlobalInspectionContext,
                              private val problemsHolder: ProblemsHolder,
                              private val methodThreshold: Int,
                              private val classThreshold: Int,
                              private val warnMissingCoverage: Boolean,
                              private val report: ProjectData?,
                              private val xmlReport: XMLProjectData?,
                              private val highlightedElement: (PsiElement) -> PsiElement) : AbstractUastNonRecursiveVisitor() {
    private val anonymousClasses = mutableSetOf<UClass>()
    private val visitedMethods = mutableSetOf<UMethod>()
    private val xmlFileDataLoaded = AtomicBoolean(false)

    override fun visitClass(node: UClass): Boolean {
      val sourcePsi = node.sourcePsi ?: return true
      val fqn = getUClassFqn(node) ?: return true // we won't find any data in such a case
      if (report != null) {
        val data = report.getClassData(fqn)
        val file = sourcePsi.containingFile
        loadClassData(data, file.virtualFile, globalContext)
        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, file.textRange, problemsHolder.project, classThreshold, warnMissingCoverage)) {
          reportElement(problemsHolder, highlightedElement(sourcePsi),
                        QodanaBundle.message("class.coverage.below.threshold", classNameForReports(node), classThreshold))
        }
      }
      else if (xmlReport != null) {
        val (packageName, fileName) = sourcePsi.containingFile.packageAndFileName() ?: return true
        val fileData = xmlReport.getFile(XMLReportSuite.getPath(packageName, fileName))
        loadXmlFileData(fileData, sourcePsi.containingFile.virtualFile, globalContext)

        if (reportProblemsNeeded(globalContext)) {
          val data = xmlReport.getClass(fqn)
          if (data == null && warnMissingCoverage ||
              data != null && data.coveredLines + data.missedLines != 0 && ((data.coveredLines * 100 / (data.coveredLines + data.missedLines)) < classThreshold)) {
            reportElement(problemsHolder, highlightedElement(sourcePsi),
                          QodanaBundle.message("class.coverage.below.threshold", classNameForReports(node), classThreshold))
          }
        }
      }

      // we need to visit declarations explicitly, say hi to Kotlin and init inside companion objects
      for (method in node.uastDeclarations.filterIsInstance<UMethod>()) {
        if (method.javaPsi.isPhysical || method.isConstructor) {
          visitMethod(method)
        }
      }

      return true
    }

    override fun visitMethod(node: UMethod): Boolean {
      if (!visitedMethods.add(node)) return true
      val sourcePsi = node.sourcePsi ?: return true
      val containingClass = node.getContainingUClass() ?: return true
      val fqn = getUClassFqn(containingClass) ?: return true // we won't find any data in such a case
      if (report != null) {
        val data = report.getClassData(fqn)
        // Load class data for the implicit anonymous classes
        if (containingClass.sourcePsi == null && anonymousClasses.add(containingClass)) {
          loadClassData(data, sourcePsi.containingFile.virtualFile, globalContext)
        }
        val isDataLoaded =
          data != null || loadMissingData(problemsHolder.project, sourcePsi.textRange, sourcePsi.containingFile, warnMissingCoverage, globalContext)
        if (!isDataLoaded) return true

        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, sourcePsi.containingFile, sourcePsi.textRange, problemsHolder.project, methodThreshold, warnMissingCoverage)) {
          reportMethodCoverage(node, sourcePsi)
        }
      }
      else if (xmlReport != null) {
        val (packageName, fileName) = sourcePsi.containingFile.packageAndFileName() ?: return true
        val data = xmlReport.getFile(XMLReportSuite.getPath(packageName, fileName))
        loadXmlFileData(data, sourcePsi.containingFile.virtualFile, globalContext)

        val isDataLoaded =
          data != null || loadMissingData(problemsHolder.project, sourcePsi.textRange, sourcePsi.containingFile, warnMissingCoverage, globalContext)
        if (!isDataLoaded) return true

        if (reportProblemsNeeded(globalContext) && issueWithXmlMethodCoverage(data, sourcePsi.containingFile, sourcePsi.textRange, problemsHolder.project, methodThreshold)) {
          reportMethodCoverage(node, sourcePsi)
        }
      }
      return true
    }

    private fun loadXmlFileData(fileInfo: XMLProjectData.FileInfo?, virtualFile: VirtualFile, globalContext: QodanaGlobalInspectionContext) {
      if (fileInfo == null) return
      if (xmlFileDataLoaded.getAndSet(true)) return
      globalContext.coverageStatisticsData.loadXmlLineData(fileInfo, virtualFile)
    }

    private fun reportMethodCoverage(node: UMethod, sourcePsi: PsiElement) {
      val isConstructor = node.isConstructor
      val signature = methodNameForReports(node, isConstructor)
      if (isConstructor) {
        reportElement(problemsHolder, highlightedElement(sourcePsi),
                      QodanaBundle.message("constructor.coverage.below.threshold", signature, methodThreshold))
      }
      else {
        reportElement(problemsHolder, highlightedElement(sourcePsi),
                      QodanaBundle.message("method.coverage.below.threshold", signature, methodThreshold))
      }
    }

    private fun computeXmlCoverage(data: XMLProjectData.FileInfo, document: Document, textRange: TextRange): Int {
      val startOffset = textRange.startOffset
      val endOffset = textRange.endOffset
      val startLineNumber = document.getLineNumber(startOffset) + 1
      val endLineNumber = document.getLineNumber(endOffset) + 1
      var totalLines = 0
      var coveredLines = 0
      for (i in startLineNumber..endLineNumber) {
        val lineData = data.lines.firstOrNull { it.lineNumber == i }
        if (lineData != null) {
          totalLines++
          if (lineData.coveredInstructions != 0 || lineData.coveredBranches != 0) {
            coveredLines++
          }
        }
      }

      if (totalLines == 0) return 100
      return coveredLines * 100 / totalLines
    }

    private fun PsiFile.packageAndFileName(): Pair<String, String>? {
      if (this !is PsiClassOwner) return null
      val packageName = runReadAction { packageName } ?: return null
      return packageName to name
    }

    private fun issueWithXmlMethodCoverage(
      data: XMLProjectData.FileInfo?,
      psiFile: PsiFile,
      textRange: TextRange,
      project: Project,
      threshold: Int
    ): Boolean {
      if (data == null) {
        return warnMissingCoverage
      }
      val doc = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return false
      return computeXmlCoverage(data, doc, textRange) < threshold
    }

    private fun getUClassFqn(uClass: UClass): String? {
      val name = when (uClass) {
        is UAnonymousClass -> {
          val anonymousName = UastAnonymousClassUtil.getName(uClass.javaPsi as PsiAnonymousClass)
          val containingClass = uClass.getContainingUClass() ?: return anonymousName
          val parentName = getUClassFqn(containingClass)
          return "$parentName$anonymousName"
        }
        else -> uClass.qualifiedName
      }

      if (name != null && name.endsWith(".Companion") && uClass.javaPsi.nameIdentifier?.textRange?.isEmpty == true) {
        return """${name.removeSuffix(".Companion")}${"$"}Companion"""
      }
      return name
    }

    private fun methodNameForReports(node: UMethod, isConstructor: Boolean) =
      computeName(node, if (isConstructor) "constructor" else "function", node.javaPsi.containingFile)

    private fun classNameForReports(node: UClass) =
      computeName(node, "class", node.javaPsi.containingFile)

    private fun computeName(node: UDeclaration, type: String, file: PsiFile): String {
      val psiName = (node.javaPsi as? PsiNameIdentifierOwner)?.name
      if (psiName != null) {
        return "$psiName"
      }
      var currentClass = node.getContainingUClass()
      while (currentClass != null) {
        val nameOwner = currentClass.javaPsi as? PsiNameIdentifierOwner
        if (nameOwner?.name != null) return "(anonymous $type in ${nameOwner.name})"
        currentClass = currentClass.getContainingUClass()
      }
      return "(anonymous $type in ${file.name})"
    }
  }

  private fun computeXmlCoverageData(globalContext: QodanaGlobalInspectionContext, engineType : KClass<out CoverageEngine>): XMLProjectData? {
    val coverageFiles = provideCoverageFiles(globalContext)
    logger.info("Coverage for ${engineType.java.simpleName} - provided ${coverageFiles.size} files")
    if (coverageFiles.isEmpty()) return null
    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(engineType.java)
    val suites = computeSuites(engine, coverageFiles, globalContext.project)
    if (suites.any()) {
      val firstSuite = suites.first()
      val report = (firstSuite as? XMLReportSuite)?.getReportData() ?: throw QodanaException("JaCoCo suite ${firstSuite.presentableName} is missing report data")
      for (suite in suites.drop(1)) {
        val add = (suite as? XMLReportSuite)?.getReportData() ?: throw QodanaException("JaCoCo suite ${suite.presentableName} is missing report data")
        report.merge(add)
      }
      if (globalContext.coverageComputationState().isIncrementalAnalysis()) {
        val stat = globalContext.coverageStatisticsData
        report.files.filter { fileInfo ->
          val possibleFiles = ModuleManager.getInstance(globalContext.project).modules.flatMap {
            ModuleRootManager.getInstance(it).sourceRoots.mapNotNull { root ->
              val filePath = root.toNioPathOrNull()?.resolve(fileInfo.path)
              filePath?.let { VirtualFileManager.getInstance().findFileByNioPath(filePath) }
            }
          }
          possibleFiles.any { file ->
            GlobalSearchScope.allScope(globalContext.project).contains(file)
          }
        }.forEach {
          x -> stat.processReportXmlData(x)
        }
      }
      return report
    }
    return null
  }
}