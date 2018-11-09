// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSSmartCompletionContributor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import org.jetbrains.vuejs.codeInsight.VueComponentDetailsProvider

class VueInsideTemplateCompletionContributor : JSSmartCompletionContributor() {
  override fun getSmartCompletionVariants(location: JSReferenceExpression): List<LookupElement>? {
    val basicVariants = super.getSmartCompletionVariants(location)
    val vueVariants = VueComponentDetailsProvider.INSTANCE.getAttributesAndCreateLookupElements(location, JSLookupPriority.MAX_PRIORITY)
    return mergeVariants(basicVariants, vueVariants)
  }
}