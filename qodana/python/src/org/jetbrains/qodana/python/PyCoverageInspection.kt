package org.jetbrains.qodana.python

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.python.pro.coverage.PyCoverageEngine
import com.intellij.rt.coverage.data.ClassData
import com.jetbrains.python.psi.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

class PyCoverageInspection : CoverageInspectionBase() {
  @Suppress("MemberVisibilityCanBePrivate")
  var fileThreshold = 50

  companion object {
    private val py = Key.create<Lazy<ProjectData?>>("qodana.python.coverage")
    private val normalizedPaths = Key.create<Lazy<Map<String, String>>>("qodana.python.normalizedPaths")
  }

  override fun loadCoverage(globalContext: QodanaGlobalInspectionContext) {
    globalContext.putUserData(py, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      computeCoverageData(globalContext, PyCoverageEngine::class)?.also { loadNormalizedPaths(globalContext, it) }
    })
  }

  override fun checker(file: PsiFile, problemsHolder: ProblemsHolder, globalContext: QodanaGlobalInspectionContext) {
    val report = globalContext.getUserData(py)?.value ?: return
    val pathsMap = globalContext.getUserData(normalizedPaths)?.value ?: return
    val data = getClassData(file, report, pathsMap)
    file.iterateContents(buildVisitor(file as PyFile, problemsHolder, data, globalContext))
  }

  override fun validateFileType(file: PsiFile) = file is PyFile

  override fun cleanup(globalContext: QodanaGlobalInspectionContext) {
    val data = globalContext.getUserData(py)?.value
    if (data != null) {
      saveCoverageData(globalContext,
                       PyCoverageEngine::class.java.simpleName,
                       removePrefixFromCoverage(data, globalContext.config.projectPath))
    }
    globalContext.putUserData(py, null)
  }

  override fun getOptionsPane(): OptPane {
    return OptPane.pane(*defaultThresholdOpts()
      .plus(OptPane.number("fileThreshold", QodanaBundle.message("file.coverage.threshold.value"), 1, 100)),
                        missingCoverageOpt())
  }

  private fun loadNormalizedPaths(globalContext: QodanaGlobalInspectionContext, report: ProjectData) {
    globalContext.putUserData(normalizedPaths, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      report.classes.keys.associateBy { normalizeFilePath(it) }
    })
  }

  private fun buildVisitor(file: PyFile,
                           holder: ProblemsHolder,
                           data: ClassData?,
                           globalContext: QodanaGlobalInspectionContext): PsiElementVisitor {
    loadClassData(data, file.virtualFile, globalContext)
    if (data == null && !warnMissingCoverage || data != null && !reportProblemsNeeded(globalContext)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }
    if (reportProblemsNeeded(globalContext) &&
        issueWithCoverage(data, file, file.textRange, holder.project, fileThreshold, warnMissingCoverage)) {
      reportElement(holder, file.firstChild, QodanaBundle.message("file.coverage.below.threshold", file.virtualFile.presentableName, fileThreshold))
    }
    return object : PyElementVisitor() {
      override fun visitPyFunction(function: PyFunction) {
        val range = computeRealFunctionRange(function)
        val isDataLoaded = data != null || loadMissingData(holder.project, range, file, warnMissingCoverage, globalContext)
        if (!isDataLoaded) return
        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, range, holder.project, methodThreshold, warnMissingCoverage)) {
          val message = if (function.name == "__init__")  {
            val clazz = function.containingClass
            val fqn = if (clazz != null) {
              clazz.name
            } else {
              "__init__"
            }
            QodanaBundle.message("constructor.coverage.below.threshold", fqn, methodThreshold)
          } else {
            QodanaBundle.message("method.coverage.below.threshold", function.name, methodThreshold)
          }
          reportElement(holder, highlightedElement(function), message)
        }
      }

      override fun visitPyClass(aClass: PyClass) {
        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, aClass.textRange, holder.project, classThreshold, warnMissingCoverage)) {
          reportElement(holder, highlightedElement(aClass), QodanaBundle.message("class.coverage.below.threshold",
                                                                                 aClass.name,
                                                                                 classThreshold))
        }
      }
    }
  }

  private fun computeRealFunctionRange(node: PyFunction): TextRange {
    var range = node.textRange
    val children = node.children
    val first = children.firstOrNull { it is PyStatement }
    if (first != null) {
      val last = children.last { it is PyStatement }
      range = TextRange.create(first.textRange.startOffset, last.textRange.endOffset)
    }
    return range
  }
}