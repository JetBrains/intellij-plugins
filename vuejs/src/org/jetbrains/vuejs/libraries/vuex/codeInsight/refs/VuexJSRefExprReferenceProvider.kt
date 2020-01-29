// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight.refs

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_GETTERS
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexContainer

class VuexJSRefExprReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (element !is JSReferenceExpression) return PsiReference.EMPTY_ARRAY
    val qualifier = element.qualifier as? JSReferenceExpression
    val accessor = when (qualifier?.referenceName) {
      GETTERS, ROOT_GETTERS -> VuexContainer::getters
      //STATE, ROOT_STATE -> VuexContainer::state
      else -> null
    }

    @Suppress("USELESS_CAST")
    val range = (element as JSReferenceExpression).referenceNameElement?.textRangeInParent
    val name = element.referenceName
    if (accessor != null && range != null && name != null) {
      return arrayOf(VuexEntityReference(element, range, accessor, name, { "" }, true))
    }
    return PsiReference.EMPTY_ARRAY
  }
}
