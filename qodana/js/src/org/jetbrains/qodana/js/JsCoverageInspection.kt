package org.jetbrains.qodana.js

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.codeInspection.options.OptPane.number
import com.intellij.javascript.jest.coverage.JestCoverageEngine
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionExpression
import com.intellij.lang.javascript.psi.JSStatement
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.ProjectData
import com.sixrr.inspectjs.BaseInspectionVisitor
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

private class JsCoverageInspection : CoverageInspectionBase() {
  @Suppress("MemberVisibilityCanBePrivate")
  var fileThreshold = 50

  companion object {
    private val jest = Key.create<Lazy<ProjectData?>>("qodana.jest.coverage")
    private val normalizedPaths = Key.create<Lazy<Map<String, String>>>("qodana.js.normalizedPaths")
  }

  override fun loadCoverage(globalContext: QodanaGlobalInspectionContext) {
    globalContext.putUserData(jest, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      computeCoverageData(globalContext, JestCoverageEngine::class)?.also { loadNormalizedPaths(globalContext, it) }
    })
  }

  override fun checker(file: PsiFile, problemsHolder: ProblemsHolder, globalContext: QodanaGlobalInspectionContext) {
    val report = globalContext.getUserData(jest)?.value ?: return
    val pathsMap = globalContext.getUserData(normalizedPaths)?.value ?: return
    val data = getClassData(file, report, pathsMap)
    file.iterateContents(buildVisitor(file as JSFile, problemsHolder, data, globalContext))
  }

  override fun validateFileType(file: PsiFile) = file is JSFile

  override fun cleanup(globalContext: QodanaGlobalInspectionContext) {
    val data = globalContext.getUserData(jest)?.value
    if (data != null) {
      saveCoverageData(globalContext,
                       JestCoverageEngine::class.java.simpleName,
                       removePrefixFromCoverage(data, globalContext.config.projectPath))
    }
    globalContext.putUserData(jest, null)
  }

  override fun getOptionsPane(): OptPane {
    return OptPane.pane(*defaultThresholdOpts()
      .plus(number("fileThreshold", QodanaBundle.message("file.coverage.threshold.value"), 1, 100)),
            missingCoverageOpt())
  }

  private fun loadNormalizedPaths(globalContext: QodanaGlobalInspectionContext, report: ProjectData) {
    globalContext.putUserData(normalizedPaths, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      report.classes.keys.associateBy { normalizeFilePath(it) }
    })
  }

  private fun buildVisitor(file: JSFile,
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
    return object : BaseInspectionVisitor() {
      override fun visitAsFunction(function: JSFunction): Boolean {
        val range = computeRealFunctionRange(function)
        val isDataLoaded = data != null || loadMissingData(holder.project, range, file, warnMissingCoverage, globalContext)
        if (!isDataLoaded) return true

        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, range, holder.project, methodThreshold, warnMissingCoverage)) {
          val type = if (function.isConstructor) "constructor" else "function"
          val message = if (function.isConstructor) {
            val cls = function.parentOfType<JSClass>()
            val fqn = if (cls != null) computeName(cls, type, file) else computeName(function, type, file)
            QodanaBundle.message("constructor.coverage.below.threshold", fqn, methodThreshold)
          } else {
            val fqn = computeName(function, type, file)
            QodanaBundle.message("method.coverage.below.threshold", fqn, methodThreshold)
          }
          reportElement(holder, highlightedElement(function), message)
        }
        return true
      }

      override fun visitJSClass(aClass: JSClass) {
        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, aClass.textRange, holder.project, classThreshold, warnMissingCoverage)) {
          reportElement(holder, highlightedElement(aClass), QodanaBundle.message("class.coverage.below.threshold",
                                                                                 computeName(aClass, "class", file),
                                                                                 classThreshold))
        }
      }

      override fun visitJSFunctionExpression(node: JSFunctionExpression) {
        visitJSFunctionDeclaration(node)
      }
    }
  }

  private fun computeName(node: JSQualifiedNamedElement, type: String, jsFile: JSFile): String {
    val psiName = (node as? PsiNameIdentifierOwner)?.name
    if (psiName != null) {
      return psiName
    }
    var currentElement: PsiElement? = node
    while (currentElement != null) {
      val nameOwner = currentElement as? PsiNameIdentifierOwner
      if (nameOwner?.name != null) return "(anonymous ${type} in ${nameOwner.name})"
      currentElement = currentElement.parent
    }
    return "(anonymous ${type} in ${jsFile.name})"
  }

  private fun computeRealFunctionRange(node: JSFunction): TextRange {
    var range = (node.block ?: node).textRange
    val children = (node.block ?: node).children
    val first = children.firstOrNull { it is JSStatement }
    if (first != null) {
      val last = children.last { it is JSStatement }
      range = TextRange.create(first.textRange.startOffset, last.textRange.endOffset)
    }
    return range
  }
}