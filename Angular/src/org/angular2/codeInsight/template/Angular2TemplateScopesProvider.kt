// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement

abstract class Angular2TemplateScopesProvider {


  /**
   * If Angular expression is injected the @{code hostElement} is not null.
   */
  abstract fun getScopes(element: PsiElement,
                         hostElement: PsiElement?): List<Angular2TemplateScope>

  open fun isImplicitReferenceExpression(expression: JSReferenceExpression): Boolean {
    return false
  }

  companion object {

    internal val EP_NAME = ExtensionPointName.create<Angular2TemplateScopesProvider>("org.angular2.templateScopesProvider")
  }
}
