// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ArrayUtilRt
import com.intellij.util.ProcessingContext
import org.angular2.Angular2DecoratorUtil.PIPE_DEC
import org.angular2.entities.Angular2EntitiesProvider
import org.angularjs.codeInsight.refs.AngularJSReferenceBase

class Angular2PipeNameReferencesProvider : PsiReferenceProvider() {

  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    return arrayOf(Angular2PipeNameReference(element as JSLiteralExpression))
  }

  class Angular2PipeNameReference(element: JSLiteralExpression)
    : AngularJSReferenceBase<JSLiteralExpression>(element, ElementManipulators.getValueTextRange(element)) {

    override fun isSoft(): Boolean {
      return false
    }

    override fun resolveInner(): PsiElement? {
      val decorator = PsiTreeUtil.getParentOfType(element, ES6Decorator::class.java)
      return if (decorator != null && PIPE_DEC == decorator.decoratorName) {
        Angular2EntitiesProvider.getPipe(decorator)?.sourceElement
      }
      else null
    }

    override fun getVariants(): Array<Any> {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY
    }
  }
}
