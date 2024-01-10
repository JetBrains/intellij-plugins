// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.template.Template
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.entities.Angular2ComponentLocator

class CreateComponentFieldIntentionAction(referenceExpression: JSReferenceExpression)
  : BaseCreateComponentFieldAction(referenceExpression.referenceName) {

  private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression> = referenceExpression.createSmartPointer()

  override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> {
    return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
  }

}