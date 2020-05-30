// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupPriority.*
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import org.jetbrains.vuejs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterReferenceExpression
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor

class VueJSCompletionProvider : CompletionProvider<CompletionParameters>() {

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    var ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
    if (ref is PsiMultiReference) {
      ref = ref.references.find { r -> r is VueJSFilterReferenceExpression || r is JSReferenceExpressionImpl }
    }
    if (ref is VueJSFilterReferenceExpression) {
      val container = VueModelManager.findEnclosingContainer(ref)
      container?.acceptEntities(object : VueModelVisitor() {
        override fun visitFilter(name: String, filter: VueFilter, proximity: Proximity): Boolean {
          if (proximity !== Proximity.OUT_OF_SCOPE) {
            (filter.source
             ?: JSImplicitElementImpl.Builder(name, ref).setType(JSImplicitElement.Type.Method).forbidAstAccess().toImplicitElement())
              .let { JSLookupUtilImpl.createLookupElement(it, name) }
              .let { JSCompletionUtil.withJSLookupPriority(it, getJSLookupPriorityOf(proximity)) }
              .let { result.consume(it) }
          }
          return proximity !== Proximity.OUT_OF_SCOPE
        }
      })
      result.stopHere()
    }
    else if (ref is JSReferenceExpressionImpl && ref.qualifier is JSThisExpression?) {
      VueTemplateScopesResolver.resolve(ref, Processor { resolveResult ->
        val element = resolveResult.element as? JSPsiElementBase
        if (element != null) {
          result.consume(JSCompletionUtil.withJSLookupPriority(JSLookupUtilImpl.createLookupElement(element),
                                                               if (element.name?.startsWith("$") == true)
                                                                 LOCAL_SCOPE_MAX_PRIORITY_EXOTIC
                                                               else
                                                                 LOCAL_SCOPE_MAX_PRIORITY))
        }
        true
      })
    }
  }

  private fun getJSLookupPriorityOf(proximity: VueModelVisitor.Proximity): JSLookupPriority =
    when (proximity) {
      VueModelVisitor.Proximity.LOCAL -> LOCAL_SCOPE_MAX_PRIORITY
      VueModelVisitor.Proximity.APP -> NESTING_LEVEL_1
      VueModelVisitor.Proximity.PLUGIN -> NESTING_LEVEL_2
      VueModelVisitor.Proximity.GLOBAL -> NESTING_LEVEL_3
      else -> LOWEST_PRIORITY
    }
}
