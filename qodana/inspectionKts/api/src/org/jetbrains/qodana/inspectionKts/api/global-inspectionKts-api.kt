@file:Suppress("unused")

package org.jetbrains.qodana.inspectionKts.api

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun globalInspection(checker: (GlobalInspection) -> Unit): GlobalInspectionTool {
  return object : GlobalInspectionTool() {
    override fun runInspection(
      scope: AnalysisScope,
      manager: InspectionManager,
      globalContext: GlobalInspectionContext,
      problemDescriptionsProcessor: ProblemDescriptionsProcessor
    ) {
      val inspection = GlobalInspectionImpl(scope, manager, globalContext, problemDescriptionsProcessor)
      checker.invoke(inspection)
    }

    override fun isGraphNeeded(): Boolean = false
  }
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS INTERFACE IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
interface GlobalInspection : Inspection {
  val analysisScope: AnalysisScope

  fun registerProblem(message: String) = registerProblem(null, message)
}

private class GlobalInspectionImpl(
  override val analysisScope: AnalysisScope,
  private val manager: InspectionManager,
  private val globalContext: GlobalInspectionContext,
  private val problemDescriptionsProcessor: ProblemDescriptionsProcessor
) : GlobalInspection {
  override val project: Project
    get() = manager.project

  @Suppress("HardCodedStringLiteral")
  override fun registerProblem(psiElement: PsiElement?, message: String) {
    val refManager = globalContext.refManager
    val psiElementRef = psiElement?.let { refManager.getReference(psiElement) }
    val problemRef = if (psiElementRef == null) {
      // TODO – RefProject currently is supported by Global analysis (the problem is converted to empty xml),
      //  for now use project dir
      val projectDir = project.guessProjectDir()?.let { refManager.psiManager.findDirectory(it) }
      val projectDirRef = projectDir?.let { refManager.getReference(projectDir) } ?: refManager.refProject
      projectDirRef
    } else {
      psiElementRef
    }
    val problemDescriptor = if (psiElement == null) {
      manager.createProblemDescriptor(message)
    } else {
      manager.createProblemDescriptor(psiElement, message, false, emptyArray(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
    }
    problemDescriptionsProcessor.addProblemElement(problemRef, problemDescriptor)
  }
}