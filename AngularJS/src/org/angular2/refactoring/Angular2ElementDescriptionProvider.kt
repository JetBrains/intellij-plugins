// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewTypeLocation
import org.angular2.index.Angular2IndexingHandler.Companion.isPipe
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable

class Angular2ElementDescriptionProvider : ElementDescriptionProvider {
  override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
    val type = getTypeDescription(element)
    if (type != null) {
      if (location is UsageViewTypeLocation) {
        return type
      }
      return if (location is UsageViewLongNameLocation) {
        type + " " + (element as PsiNamedElement).name
      }
      else (element as PsiNamedElement).name
    }
    return null
  }

  companion object {
    private fun getTypeDescription(element: PsiElement): @NlsContexts.DetailedDescription String? {
      if (element is JSImplicitElement && isPipe(element)) {
        return Angular2Bundle.message("angular.description.pipe")
      }
      return if (element is Angular2HtmlAttrVariable) {
        Angular2Bundle.message("angular.description.ref-var")
      }
      else null
    }
  }
}