// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.intentions.JSChangeModifierIntentionBase
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.parentOfType
import com.intellij.util.IncorrectOperationException
import com.intellij.util.asSafely
import org.angular2.inspections.getInputSourceElements
import org.angular2.inspections.isAccessible
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.jetbrains.annotations.Nls

class AngularChangeModifierQuickFix(
  private val newModifier: JSAttributeList.AccessType,
  private val ownerClassName: String? = null,
) : JSChangeModifierIntentionBase(), LocalQuickFix {

  override fun supportsModifier(element: PsiElement): Boolean = true

  override fun getInspectionAccessType(): JSAttributeList.AccessType =
    newModifier

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getName(): String {
    return text
  }

  override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
    return if (isAngularTemplateElement(element))
      super.isAvailable(project, editor, locateMemberToEdit(element)?.targetIntentionElement ?: return false)
    else
      super.isAvailable(project, editor, element)
  }

  @Throws(IncorrectOperationException::class)
  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    if (!isAngularTemplateElement(element)) {
      super.invoke(project, editor, element)
      return
    }

    val member = locateMemberToEdit(element) ?: return
    super.invoke(project, editor, member.targetIntentionElement)
  }

  private val PsiElement.targetIntentionElement
    get() = asSafely<PsiNameIdentifierOwner>()?.nameIdentifier ?: this

  private fun locateMemberToEdit(element: PsiElement): PsiElement? {
    var owner: PsiElement? = element
    owner = if (owner is PsiWhiteSpace)
      owner.getPrevSibling()
    else
      owner?.parent

    if (owner is Angular2HtmlPropertyBinding) {
      return getInputSourceElements(owner).find { input ->
        val inputOwner = input.parentOfType<TypeScriptClass>()
        val minAccessType = if (inputOwner == owner) JSAttributeList.AccessType.PROTECTED else JSAttributeList.AccessType.PUBLIC
        inputOwner?.name == ownerClassName && !isAccessible(input, minAccessType)
      }
    }

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
    return language is HTMLLanguage || language is Angular2ExprDialect
  }
}
