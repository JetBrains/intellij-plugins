package org.jetbrains.qodana.actions

import com.intellij.codeInspection.*
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement

class QodanaSuppressIntentionAction(private val suppressFix: SuppressQuickFix) : SuppressIntentionAction() {
  override fun getText(): String = suppressFix.name

  override fun getFamilyName(): String = suppressFix.familyName

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
    return suppressFix.isAvailable(project, element)
  }

  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    val problemDescriptor = InspectionManager.getInstance(project)
      .createProblemDescriptor(element, "QodanaSuppressIntention", suppressFix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true)
    suppressFix.applyFix(project, problemDescriptor)
    return
  }
}

class QodanaSuppressableProblemGroup(private val toolId: String, val file: VirtualFile?) : SuppressableProblemGroup {
  override fun getProblemName(): String? = null

  override fun getSuppressActions(element: PsiElement?): Array<SuppressIntentionAction> {
    val language = LanguageUtil.getFileLanguage(file) ?: return emptyArray()
    val inspectionSuppressors = LanguageInspectionSuppressors.INSTANCE.allForLanguage(language)
    for (suppressor in inspectionSuppressors) {
      val suppressIntentionActions: Array<SuppressIntentionAction> = suppressor.getSuppressActions(element, toolId).map {
        QodanaSuppressIntentionAction(it)
      }.toTypedArray()
      if (suppressIntentionActions.isNotEmpty()) return suppressIntentionActions
    }
    return emptyArray()
  }
}