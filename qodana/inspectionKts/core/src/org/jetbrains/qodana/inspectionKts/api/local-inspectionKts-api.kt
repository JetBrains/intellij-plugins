@file:Suppress("unused")

package org.jetbrains.qodana.inspectionKts.api

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun localInspection(checker: (PsiFile, LocalInspection) -> Unit): LocalInspectionTool {
  return LocalKtsInspectionTool(checker)
}

@Suppress("InspectionDescriptionNotFoundInspection")
internal class LocalKtsInspectionTool(val checker: (PsiFile, LocalInspection) -> Unit) : LocalInspectionTool()


/**
 * DO NOT CHANGE THE SIGNATURE OF THIS INTERFACE IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
interface LocalInspection : Inspection {
  val currentFile: PsiFile

  fun registerProblem(psiElement: PsiElement?, message: String, vararg fixes: LocalQuickFix)
}

internal class LocalInspectionImpl(
  private val problemsHolder: ProblemsHolder
) : LocalInspection {
  override val project: Project
    get() = problemsHolder.project

  override val currentFile: PsiFile
    get() = problemsHolder.file

  override fun registerProblem(psiElement: PsiElement?, message: String) {
    @Suppress("HardCodedStringLiteral")
    problemsHolder.registerProblem(psiElement ?: currentFile, message)
  }

  override fun registerProblem(psiElement: PsiElement?, message: String, vararg fixes: LocalQuickFix) {
    @Suppress("HardCodedStringLiteral")
    problemsHolder.registerProblem(psiElement ?: currentFile, message, *fixes)
  }
}