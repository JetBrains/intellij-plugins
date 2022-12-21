// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.intentions.JSPublicModifierIntention
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiWhiteSpace
import com.intellij.util.IncorrectOperationException
import com.intellij.util.asSafely
import org.angular2.inspections.AngularInaccessibleComponentMemberInAotModeInspection
import org.angular2.lang.expr.Angular2Language
import org.jetbrains.annotations.Nls

class AngularMakePublicQuickFix : JSPublicModifierIntention(), LocalQuickFix {

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getName(): String {
    return text
  }

  override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
    return if (isAngularTemplateElement(element))
      AngularInaccessibleComponentMemberInAotModeInspection.accept(locateMemberToEdit(element))
    else
      super.isAvailable(project, editor, element)
  }

  @Throws(IncorrectOperationException::class)
  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    if (!isAngularTemplateElement(element)) {
      super.invoke(project, editor, element)
      return
    }

    val member = locateMemberToEdit(element)
    if (member == null || !AngularInaccessibleComponentMemberInAotModeInspection.accept(member)) {
      return
    }
    if (editor != null) {
      PsiNavigationSupport.getInstance()
        .createNavigatable(project, member.containingFile.virtualFile, member.textOffset)
        .navigate(true)
    }

    super.invoke(project, editor, member.asSafely<PsiNameIdentifierOwner>()?.nameIdentifier ?: member)
  }

  private fun locateMemberToEdit(element: PsiElement): PsiElement? {
    var owner: PsiElement? = element
    owner = if (owner is PsiWhiteSpace)
      owner.getPrevSibling()
    else
      owner?.parent

    if (owner != null && owner !is JSReferenceExpression) {
      owner = owner.prevSibling
    }
    if (owner is JSReferenceExpression) {
      owner = owner.resolve()
    }
    if (owner is PsiNameIdentifierOwner) {
      owner = owner.nameIdentifier
    }
    if (owner == null) {
      return null
    }
    var member: PsiElement? = getField(owner)
    if (member == null) {
      member = if (owner is JSFunction) owner else getFunction(owner)
    }
    return member
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    invoke(project, null, descriptor.psiElement)
  }

  override fun getPriority(): PriorityAction.Priority {
    return PriorityAction.Priority.HIGH
  }

  private fun isAngularTemplateElement(element: PsiElement): Boolean {
    val language = element.containingFile.language
    return language is HTMLLanguage || language is Angular2Language
  }
}
