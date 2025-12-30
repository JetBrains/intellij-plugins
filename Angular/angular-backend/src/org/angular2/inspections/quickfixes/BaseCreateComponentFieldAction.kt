// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.template.Template
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.angular2.entities.source.Angular2SourceUtil

abstract class BaseCreateComponentFieldAction(fieldName: String?)
  : CreateJSVariableIntentionAction(fieldName, true, false, false) {

  override fun isAvailable(project: Project?, element: PsiElement?, editor: Editor?, file: PsiFile?): Boolean =
    element != null && Angular2SourceUtil.findComponentClass(element).let { it != null && !JSProjectUtil.isInLibrary(it) }

  override fun applyFix(project: Project, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
    val targetClass = Angular2SourceUtil.findComponentClass(psiElement) ?: return
    doApplyFix(project, targetClass, targetClass.containingFile, null)
  }

  override fun calculateInitialAnchor(predefinedAnchor: PsiElement?, nodeForAnchor: PsiElement?): PsiElement? {
    return predefinedAnchor ?: nodeForAnchor
  }

  override fun assertValidContext(psiElement: PsiElement, referenceExpression: JSReferenceExpression?) {
    if (psiElement !is TypeScriptClass) super.assertValidContext(psiElement, referenceExpression)
  }

  override fun beforeStartTemplateAction(referenceExpression: JSReferenceExpression?,
                                         editor: Editor,
                                         anchor: PsiElement,
                                         isStaticContext: Boolean): JSReferenceExpression? {
    return referenceExpression
  }

  override fun skipParentIfClass(): Boolean {
    return false
  }

  override fun addAccessModifier(template: Template,
                                 referenceExpression: JSReferenceExpression,
                                 staticContext: Boolean,
                                 targetClass: JSClass) {
    Angular2FixesTemplateUtil.addClassMemberModifiers(template, staticContext, targetClass)
  }

}