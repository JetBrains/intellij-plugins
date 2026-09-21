package org.jetbrains.qodana.inspectionKts.core

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.InspectionEngine
import com.intellij.codeInspection.InspectionManagerBase
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.QuickFix
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.annotation.ProblemGroup
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

suspend fun runInspectionOnPsiFile(
  tool: LocalInspectionTool,
  psiFile: PsiFile,
): List<ProblemDescriptor> {
  val holder = ProblemsHolder(VerificationInspectionManager(psiFile.getProject()), psiFile, false)

  readAction {
    InspectionEngine.withSession(psiFile, psiFile.textRange, psiFile.textRange, HighlightSeverity.INFORMATION, false, null) { session ->
      val visitor = tool.buildVisitor(holder, false, session)
      visitor.visitFile(psiFile)
    }
  }

  return holder.results
}


private class VerificationInspectionManager(project: Project) : InspectionManagerBase(project) {
  @Suppress("OVERRIDE_DEPRECATION", "removal")
  override fun createNewGlobalContext(reuse: Boolean): GlobalInspectionContext {
    throw IllegalStateException("Not supported")
  }

  override fun createNewGlobalContext(): GlobalInspectionContext {
    throw IllegalStateException("Not supported")
  }

  override fun createProblemDescriptor(
    psiElement: PsiElement,
    descriptionTemplate: String,
    fix: LocalQuickFix?,
    highlightType: ProblemHighlightType,
    onTheFly: Boolean,
  ): ProblemDescriptor {
    return VerificationProblemDescriptor(
      element = psiElement,
      description = descriptionTemplate,
      highlight = highlightType,
      fixes = listOfNotNull(fix),
      lineNumber = computeOneBasedLine(psiElement),
    )
  }

  override fun createProblemDescriptor(
    psiElement: PsiElement,
    descriptionTemplate: String,
    onTheFly: Boolean,
    fixes: Array<out LocalQuickFix>?,
    highlightType: ProblemHighlightType,
  ): ProblemDescriptor {
    return VerificationProblemDescriptor(
      element = psiElement,
      description = descriptionTemplate,
      highlight = highlightType,
      fixes = fixes.orEmpty().toList(),
      lineNumber = computeOneBasedLine(psiElement),
    )
  }

  private fun computeOneBasedLine(element: PsiElement): Int {
    val doc = element.containingFile?.viewProvider?.document
    val start = element.textRange?.startOffset ?: return -1
    val zeroBased = doc?.getLineNumber(start) ?: return -1
    return zeroBased + 1
  }
}

private class VerificationProblemDescriptor(
  private val element: PsiElement,
  private val description: String,
  private val highlight: ProblemHighlightType,
  private val fixes: List<LocalQuickFix>?,
  private val lineNumber: Int,
) : ProblemDescriptor {

  override fun getPsiElement(): PsiElement = element

  override fun getStartElement(): PsiElement = element

  override fun getEndElement(): PsiElement = element

  override fun getTextRangeInElement(): TextRange? = null

  override fun getLineNumber(): Int = lineNumber

  override fun getHighlightType(): ProblemHighlightType = highlight

  override fun isAfterEndOfLine(): Boolean = false

  override fun setTextAttributes(key: TextAttributesKey?) {}

  override fun getProblemGroup(): ProblemGroup? = null

  override fun setProblemGroup(problemGroup: ProblemGroup?) {}

  override fun showTooltip(): Boolean = false

  @Suppress("HardCodedStringLiteral")
  override fun getDescriptionTemplate(): String = description

  @Suppress("UNCHECKED_CAST")
  override fun getFixes(): Array<QuickFix<*>> = fixes.orEmpty().toTypedArray() as Array<QuickFix<*>>
}