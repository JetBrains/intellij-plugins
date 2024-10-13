package org.jetbrains.qodana.go

import com.goide.execution.testing.coverage.GoCoverageEngine
import com.goide.psi.*
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.ProjectData
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

class GoCoverageInspection : CoverageInspectionBase() {
  @Suppress("MemberVisibilityCanBePrivate")
  var fileThreshold = 50

  companion object {
    private val go = Key.create<Lazy<ProjectData?>>("qodana.go.coverage")
    private val normalizedPaths = Key.create<Lazy<Map<String, String>>>("qodana.go.normalizedPaths")
  }

  override fun loadCoverage(globalContext: QodanaGlobalInspectionContext) {
    globalContext.putUserData(go, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      computeCoverageData(globalContext, GoCoverageEngine::class)?.also { loadNormalizedPaths(globalContext, it) }
    })
  }

  override fun checker(file: PsiFile, problemsHolder: ProblemsHolder, globalContext: QodanaGlobalInspectionContext) {
    val report = globalContext.getUserData(go)?.value ?: return
    val pathsMap = globalContext.getUserData(normalizedPaths)?.value ?: return
    val data = getClassData(file, report, pathsMap)
    file.iterateContents(buildVisitor(file as GoFile, problemsHolder, data, globalContext))
  }

  override fun validateFileType(file: PsiFile) = file is GoFile

  override fun cleanup(globalContext: QodanaGlobalInspectionContext) {
    val data = globalContext.getUserData(go)?.value
    if (data != null) {
      saveCoverageData(globalContext,
                       GoCoverageEngine::class.java.simpleName,
                       removePrefixFromCoverage(data, globalContext.config.projectPath))
    }
    globalContext.putUserData(go, null)
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

  private fun buildVisitor(file: GoFile,
                           holder: ProblemsHolder,
                           data: ClassData?,
                           globalContext: QodanaGlobalInspectionContext): PsiElementVisitor {
    loadClassData(data, file.virtualFile, globalContext)
    if (data == null && !warnMissingCoverage || data != null && !reportProblemsNeeded(globalContext)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }
    if (reportProblemsNeeded(globalContext) &&
        issueWithCoverage(data, file, file.textRange, holder.project, fileThreshold, warnMissingCoverage)) {
      reportElement(holder, file.firstChild,
                    QodanaBundle.message("file.coverage.below.threshold", file.virtualFile.presentableName, fileThreshold))
    }
    return object : GoVisitor() {
      override fun visitFunctionOrMethodDeclaration(function: GoFunctionOrMethodDeclaration) {
        val range = computeRealFunctionRange(function)
        val isDataLoaded = data != null || loadMissingData(holder.project, range, file, warnMissingCoverage, globalContext)
        if (!isDataLoaded) return

        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, range, holder.project, methodThreshold, warnMissingCoverage)) {
          val message = if (function is GoMethodDeclaration) {
            val receiverTypeName = function.receiverType?.typeReferenceExpression?.identifier?.text
            if (receiverTypeName != null)
              QodanaBundle.message("method.coverage.below.threshold", "${function.name} for receiver with type ${receiverTypeName}", methodThreshold)
            else
              QodanaBundle.message("method.coverage.below.threshold", function.name, methodThreshold)
          } else {
            QodanaBundle.message("method.coverage.below.threshold", function.name, methodThreshold)
          }
          reportElement(holder, highlightedElement(function), message)
        }
      }

      override fun visitFunctionLit(function: GoFunctionLit) {
        val range = computeRealFunctionLitRange(function)
        val isDataLoaded = data != null || loadMissingData(holder.project, range, file, warnMissingCoverage, globalContext)
        if (!isDataLoaded) return

        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, range, holder.project, methodThreshold, warnMissingCoverage)) {
          val message = QodanaBundle.message("method.coverage.below.threshold", computeAnonymousName(function, file), methodThreshold)
          reportElement(holder, highlightedElement(function), message)
        }
      }
    }
  }

  private fun computeRealFunctionRange(node: GoFunctionOrMethodDeclaration): TextRange {
    val range = (node.block ?: node).textRange
    val children = (node.block ?: node).children
    return computeRealRangeByChildren(children) ?: range
  }

  private fun computeRealFunctionLitRange(node: GoFunctionLit): TextRange {
    val range = (node.block ?: node).textRange
    val children = (node.block ?: node).children
    return computeRealRangeByChildren(children) ?: range
  }

  private fun computeRealRangeByChildren(children: Array<PsiElement>): TextRange? {
    var range: TextRange? = null
    val first = children.firstOrNull { it is GoStatement }
    if (first != null) {
      val last = children.last { it is GoStatement }
      range = TextRange.create(first.textRange.startOffset, last.textRange.endOffset)
    }
    return range
  }

  private fun computeAnonymousName(node: GoFunctionLit, file: GoFile): String {
    var currentElement: PsiElement? = node
    while (currentElement != null) {
      if (currentElement is GoNamedElement && !currentElement.name.isNullOrEmpty()) {
        return "(anonymous function in ${currentElement.name})"
      }
      currentElement = currentElement.parent
    }
    return "(anonymous function in ${file.name})"
  }
}

