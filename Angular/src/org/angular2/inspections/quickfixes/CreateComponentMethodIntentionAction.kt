// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.template.Template
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.lang.javascript.validation.fixes.CreateJSFunctionIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import org.angular2.entities.source.Angular2SourceUtil

class CreateComponentMethodIntentionAction(methodExpression: JSReferenceExpression)
  : CreateJSFunctionIntentionAction(methodExpression.referenceName, true, false) {

  private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression> = methodExpression.createSmartPointer()

  override fun applyFix(project: Project, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
    val componentClass = Angular2SourceUtil.findComponentClass(psiElement)!!
    doApplyFix(project, componentClass, componentClass.containingFile, null)
  }

  override fun calculateInitialAnchor(predefinedAnchor: PsiElement?, nodeForAnchor: PsiElement?): PsiElement? {
    return predefinedAnchor ?: nodeForAnchor
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

  override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> {
    return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
  }

  override fun writeFunctionAndName(template: Template,
                                    createdMethodName: String,
                                    anchorParent: PsiElement,
                                    clazz: PsiElement?,
                                    referenceExpression: JSReferenceExpression) {
    val actualName = if (referenceExpression.qualifier is JSThisExpression) {
      referenceExpression.referenceName ?: createdMethodName
    }
    else {
      createdMethodName
    }
    template.addTextSegment(JSClassUtils.createClassFunctionName(actualName, anchorParent))
  }

  override fun addAccessModifier(template: Template,
                                 referenceExpression: JSReferenceExpression,
                                 staticContext: Boolean,
                                 targetClass: JSClass) {
    Angular2FixesTemplateUtil.addClassMemberModifiers(template, staticContext, targetClass)
  }
}