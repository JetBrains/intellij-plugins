package org.jetbrains.qodana.jvm.kotlin

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.compiler.CompilerConfiguration
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.qodana.QodanaBundle

private class QodanaKotlinSanityInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (isOnTheFly) {
      return PsiElementVisitor.EMPTY_VISITOR
    }
    val file = holder.file
    if (InjectedLanguageManager.getInstance(file.project).isInjectedFragment(file)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }
    val virtualFile = PsiUtilCore.getVirtualFile(file)
    if (virtualFile == null ||
        CompilerConfiguration.getInstance(holder.project).isExcludedFromCompilation(virtualFile)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return Visitor(holder)
  }

  private class Visitor(val holder: ProblemsHolder) : KtVisitorVoid() {
    override fun visitImportDirective(importDirective: KtImportDirective) {
      val referenceExpression = importDirective.importedReference ?: return
      val reference = when(referenceExpression) {
        is KtReferenceExpression -> referenceExpression
        else -> referenceExpression.getChildOfType<KtReferenceExpression>()
      } ?: return
      if (canResolve(reference.mainReference)) return
      holder.registerProblem(
        referenceExpression,
        QodanaBundle.message("inspection.message.unresolved.reference", referenceExpression.text),
        ProblemHighlightType.ERROR
      )
    }

    override fun visitTypeReference(typeReference: KtTypeReference) {

      val referenceExpression = (typeReference.typeElement as? KtUserType)?.referenceExpression ?: return
      if (canResolve(referenceExpression.mainReference) || typeReference.isPlaceholder) return
      holder.registerProblem(
        typeReference,
        QodanaBundle.message("inspection.message.unresolved.reference", typeReference.text),
        ProblemHighlightType.ERROR
      )
    }

    fun canResolve(reference: KtReference): Boolean {
      val resolveResults = reference.multiResolve(false)
      return resolveResults.isNotEmpty()
    }
  }
}
