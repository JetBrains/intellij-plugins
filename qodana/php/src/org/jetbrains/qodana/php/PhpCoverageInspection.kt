package org.jetbrains.qodana.php

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.codeInspection.options.OptPane.number
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.ProjectData
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.impl.FunctionImpl
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor
import com.jetbrains.php.phpunit.coverage.PhpUnitCoverageEngine
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

private class PhpCoverageInspection : CoverageInspectionBase() {
  @Suppress("MemberVisibilityCanBePrivate")
  var fileThreshold = 50

  companion object {
    private val phpunit = Key.create<Lazy<ProjectData?>>("qodana.phpunit.coverage")
    private val normalizedPaths = Key.create<Lazy<Map<String, String>>>("qodana.php.normalizedPaths")
  }

  override fun loadCoverage(globalContext: QodanaGlobalInspectionContext) {
    globalContext.putUserData(phpunit, lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
      computeCoverageData(globalContext, PhpUnitCoverageEngine::class)?.also { loadNormalizedPaths(globalContext, it) }
    })
  }

  override fun checker(file: PsiFile, problemsHolder: ProblemsHolder, globalContext: QodanaGlobalInspectionContext) {
    val report = globalContext.getUserData(phpunit)?.value ?: return
    val pathsMap = globalContext.getUserData(normalizedPaths)?.value ?: return
    val data = getClassData(file, report, pathsMap)
    file.iterateContents(buildVisitor(file as PhpFile, problemsHolder, data, globalContext))
  }

  override fun validateFileType(file: PsiFile) = file is PhpFile

  override fun cleanup(globalContext: QodanaGlobalInspectionContext) {
    val data = globalContext.getUserData(phpunit)?.value
    if (data != null) {
      saveCoverageData(globalContext, PhpUnitCoverageEngine::class.java.simpleName, removePrefixFromCoverage(data, globalContext.config.projectPath))
    }
    globalContext.putUserData(phpunit, null)
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

  private fun buildVisitor(file: PhpFile,
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
    return object : PhpElementVisitor() {
      override fun visitPhpFunction(function: Function) {
        val range = computeRealFunctionRange(function)
        val isDataLoaded = data != null || loadMissingData(holder.project, range, file, warnMissingCoverage, globalContext)
        if (!isDataLoaded) return

        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, range, holder.project, methodThreshold, warnMissingCoverage)) {
          if (function is Method && function.getMethodType(false) == Method.MethodType.CONSTRUCTOR) {
            val clazz = function.containingClass
            val fqn = if (clazz != null) {
              if (!clazz.isAnonymous) clazz.name else computeAnonymousName(clazz, "class", file)
            } else {
              "__constructor"
            }
            reportElement(holder, highlightedElement(function), QodanaBundle.message("constructor.coverage.below.threshold", fqn, methodThreshold))
          } else {
            val fqn = if (!function.isClosure) function.name else computeAnonymousName(function, "function", file)
            reportElement(holder, highlightedElement(function), QodanaBundle.message("method.coverage.below.threshold", fqn, methodThreshold))
          }
        }
      }

      override fun visitPhpClass(clazz: PhpClass) {
        if (reportProblemsNeeded(globalContext) &&
            issueWithCoverage(data, file, clazz.textRange, holder.project, classThreshold, warnMissingCoverage)) {
          val fqn = if (!clazz.isAnonymous) clazz.name else computeAnonymousName(clazz, "class", file)
          reportElement(holder, highlightedElement(clazz), QodanaBundle.message("class.coverage.below.threshold", fqn, classThreshold))
        }
      }

      override fun visitPhpMethod(method: Method) {
        visitPhpFunction(method)
      }
    }
  }

  private fun computeRealFunctionRange(node: Function): TextRange {
    if (FunctionImpl.isShortArrowFunction(node)) {
      val expr = node.children.firstOrNull { it is PhpExpression }
      if (expr != null) {
        return TextRange.create(expr.textRange.startOffset, expr.textRange.endOffset)
      }
    } else {
      val groupStatement = node.children.firstOrNull { it is GroupStatement }
      if (groupStatement is GroupStatement) {
        val first = groupStatement.statements.firstOrNull()
        if (first != null) {
          val last = groupStatement.statements.last()
          return TextRange.create(first.textRange.startOffset, last.textRange.endOffset)
        }
      }
    }
    return node.textRange // empty method
  }

  private fun computeAnonymousName(node: PhpNamedElement, type: String, file: PhpFile): String {
    var currentElement: PsiElement? = node

    while (currentElement != null) {
      if (currentElement is PhpNamedElement && !currentElement.name.isNullOrEmpty()) {
        return "(anonymous ${type} in ${currentElement.name})"
      }
      currentElement = currentElement.parent
    }
    return "(anonymous ${type} in ${file.name})"
  }
}