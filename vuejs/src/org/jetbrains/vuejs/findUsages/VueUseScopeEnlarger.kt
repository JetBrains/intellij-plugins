// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.findUsages

import com.intellij.lang.javascript.psi.JSFieldVariable
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueUseScopeEnlarger : UseScopeEnlarger() {
  override fun getAdditionalUseScope(element: PsiElement): SearchScope? =
    if (element is JSFieldVariable
        && element.containingFile.language == VueLanguage.INSTANCE
        && element.parent
          ?.parent
          ?.takeIf {
            it is JSClass
            && PsiTreeUtil.getContextOfType(it, true, JSObjectLiteralExpression::class.java,
                                            JSClass::class.java) == null
          } != null) {
      LocalSearchScope(element.containingFile)
    }
    else null
}