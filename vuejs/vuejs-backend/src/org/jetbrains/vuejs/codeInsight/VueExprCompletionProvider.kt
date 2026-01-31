// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.BaseCompletionService
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.lang.javascript.completion.JSCompletionContributor
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY
import com.intellij.lang.javascript.completion.JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY_EXOTIC
import com.intellij.lang.javascript.completion.JSLookupPriority.LOWEST_PRIORITY
import com.intellij.lang.javascript.completion.JSLookupPriority.NESTING_LEVEL_1
import com.intellij.lang.javascript.completion.JSLookupPriority.NESTING_LEVEL_2
import com.intellij.lang.javascript.completion.JSLookupPriority.NESTING_LEVEL_3
import com.intellij.lang.javascript.completion.JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY
import com.intellij.lang.javascript.completion.JSLookupPriority.NO_RELEVANT_NO_SMARTNESS_PRIORITY
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.completion.JSPatternBasedCompletionContributor
import com.intellij.lang.javascript.completion.JSReferenceCompletionProvider
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import org.jetbrains.vuejs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterReferenceExpression
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor

class VueExprCompletionProvider : CompletionProvider<CompletionParameters>() {

  companion object {
    private val FILTERED_NON_CONTEXT_KEYWORDS = setOf("do", "class", "for", "function", "if", "import()", "switch", "throw",
                                                      "var", "let", "const", "try", "while", "with", "debugger")

    fun filterOutGenericJSResults(
      allowGlobalSymbols: Boolean,
      result: CompletionResultSet,
      parameters: CompletionParameters,
    ) {
      result.runRemainingContributors(parameters) { completionResult ->
        val lookupElement = completionResult.lookupElement
        // Filter out JavaScript symbols, and keywords such as 'class' and 'function'
        if (lookupElement is PrioritizedLookupElement<*>
            && lookupElement.getUserData(BaseCompletionService.LOOKUP_ELEMENT_CONTRIBUTOR)
              .let { it is JSCompletionContributor || it is JSPatternBasedCompletionContributor }) {
          val priority = lookupElement.priority.toInt()
          val proximity = lookupElement.explicitProximity

          // Filter some keywords
          if ((priority == NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
               || priority == LOWEST_PRIORITY.priorityValue)
              && FILTERED_NON_CONTEXT_KEYWORDS.contains(lookupElement.lookupString))
            return@runRemainingContributors

          val maxLookupPriority = NO_RELEVANT_NO_SMARTNESS_PRIORITY

          if (!allowGlobalSymbols
              && (priority < maxLookupPriority.priorityValue
                  || (priority == maxLookupPriority.priorityValue && proximity <= maxLookupPriority.proximityValue)))
            return@runRemainingContributors
        }
        result.withRelevanceSorter(completionResult.sorter)
          .withPrefixMatcher(completionResult.prefixMatcher)
          .addElement(lookupElement)
      }
    }
  }

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    var ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
    if (ref is PsiMultiReference) {
      ref = ref.references.find { r -> r is VueJSFilterReferenceExpression || r is JSReferenceExpressionImpl }
    }
    if (ref is VueJSFilterReferenceExpression) {
      val container = VueModelManager.findEnclosingContainer(ref)
      container.acceptEntities(object : VueModelVisitor() {
        override fun visitFilter(filter: VueFilter, proximity: Proximity): Boolean {
          if (proximity !== Proximity.OUT_OF_SCOPE) {
            filter.source
              .let { JSLookupUtilImpl.createLookupElement(it, filter.name) }
              .let { JSCompletionUtil.withJSLookupPriority(it, getJSLookupPriorityOf(proximity)) }
              .let { result.consume(it) }
          }
          return proximity !== Proximity.OUT_OF_SCOPE
        }
      })
      result.stopHere()
    }
    else if (ref is JSReferenceExpressionImpl
             && ref.qualifier is JSThisExpression?
             && ref.parent !is JSProperty) {
      val patchedResult = result.withRelevanceSorter(JSCompletionContributor.createOwnSorter(parameters))

      if (!JSReferenceCompletionProvider.skipReferenceCompletionByContext(parameters.position)) {
        VueTemplateScopesResolver.resolve(ref, Processor { resolveResult ->
          val element = resolveResult.element as? JSPsiNamedElementBase
          if (element != null) {
            JSLookupUtilImpl.createPrioritizedLookupItem(
              element, StringUtil.notNullize(element.name),
              if (element.name?.startsWith("$") == true)
                LOCAL_SCOPE_MAX_PRIORITY_EXOTIC
              else
                LOCAL_SCOPE_MAX_PRIORITY
            )?.let { patchedResult.addElement(it) }
          }
          true
        })
      }

      if (ref.qualifier is JSThisExpression) {
        patchedResult.stopHere()
      }
      else {
        filterOutGenericJSResults(true, result, parameters)
      }
    }
  }

  private fun getJSLookupPriorityOf(proximity: VueModelVisitor.Proximity): JSLookupPriority =
    when (proximity) {
      VueModelVisitor.Proximity.LOCAL -> LOCAL_SCOPE_MAX_PRIORITY
      VueModelVisitor.Proximity.APP -> NESTING_LEVEL_1
      VueModelVisitor.Proximity.LIBRARY -> NESTING_LEVEL_2
      VueModelVisitor.Proximity.GLOBAL -> NESTING_LEVEL_3
      else -> LOWEST_PRIORITY
    }
}
