// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.completion.JSSmartCompletionContributor
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.util.Processor
import org.jetbrains.vuejs.codeInsight.template.VueTemplateScopesResolver
import java.util.*

class VueInsideTemplateCompletionContributor : JSSmartCompletionContributor() {
  override fun getSmartCompletionVariants(location: JSReferenceExpression): List<LookupElement>? {
    val basicVariants = super.getSmartCompletionVariants(location)

    val vueVariants = ArrayList<LookupElement>()

    VueTemplateScopesResolver.resolve(location, Processor { resolveResult ->
      val element = resolveResult.element as? JSPsiElementBase
      if (element != null) {
        vueVariants.add(JSCompletionUtil.withJSLookupPriority(JSLookupUtilImpl.createLookupElement(element),
                                                              JSLookupPriority.MAX_PRIORITY))
      }
      true
    })
    return mergeVariants(basicVariants, vueVariants)
  }
}
