// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer

class CreateComponentFieldIntentionAction(referenceExpression: JSReferenceExpression)
  : BaseCreateComponentFieldAction(referenceExpression.referenceName) {

  private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression> = referenceExpression.createSmartPointer()

  override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> {
    return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
  }
}