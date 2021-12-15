// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.*
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import com.intellij.util.SmartList
import org.apache.commons.lang.StringUtils
import org.jetbrains.vuejs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterReferenceExpression
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelProximityVisitor

class VueJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                       ignorePerformanceLimits: Boolean) :
  JSReferenceExpressionResolver(referenceExpression!!, ignorePerformanceLimits) {

  companion object {
    fun resolveFiltersFromReferenceExpression(expression: VueJSFilterReferenceExpression): List<VueFilter> {
      val container = VueModelManager.findEnclosingContainer(expression)
      val filters = mutableListOf<VueFilter>()
      val referenceName = expression.referenceName
      container.acceptEntities(object : VueModelProximityVisitor() {
        override fun visitFilter(name: String, filter: VueFilter, proximity: Proximity): Boolean {
          return acceptSameProximity(proximity, name == referenceName) {
            filters.add(filter)
          }
        }
      })
      return filters
    }
  }

  override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> =
    when {
      myReferencedName == null -> ResolveResult.EMPTY_ARRAY
      myRef is VueJSFilterReferenceExpression -> resolveFilterNameReference(myRef, incompleteCode)
      myQualifier is JSThisExpression -> resolveTemplateVariable(expression)
      myQualifier == null -> resolveTemplateVariable(expression)
        .ifEmpty { super.resolve(expression, incompleteCode) }
      else -> super.resolve(expression, incompleteCode)
    }

  override fun resolveFromIndices(localProcessor: SinkResolveProcessor<ResolveResultSink>,
                                  excludeGlobalTypeScript: Boolean,
                                  includeTypeOnlyContextSymbols: Boolean): Array<ResolveResult> =
    if (myQualifier == null) {
      val processor = WalkUpResolveProcessor(myReferencedName!!, myContainingFile, myRef)
      processor.addLocalResults(localProcessor)
      getResultsFromProcessor(processor)
    }
    else super.resolveFromIndices(localProcessor, excludeGlobalTypeScript, includeTypeOnlyContextSymbols)

  private fun resolveFilterNameReference(expression: VueJSFilterReferenceExpression, incompleteCode: Boolean): Array<ResolveResult> {
    if (!incompleteCode) {
      val results = expression.multiResolve(true)
      //expected type evaluator uses incomplete = true results so we have to cache it and reuse inside incomplete = false
      return JSTypeSignatureChooser(expression.parent as JSCallExpression).chooseOverload(results)
    }
    assert(myReferencedName != null)

    val filters = resolveFiltersFromReferenceExpression(expression)
    return filters.asSequence()
      .map {
        JSResolveResult(it.source ?: JSImplicitElementImpl.Builder(myReferencedName!!, expression)
          .forbidAstAccess().toImplicitElement())
      }
      .toList()
      .toTypedArray()
  }

  private fun resolveTemplateVariable(expression: JSReferenceExpressionImpl): Array<ResolveResult> {
    // TODO merge with Angular code
    assert(myReferencedName != null)
    val access = JSReadWriteAccessDetector.ourInstance
      .getExpressionAccess(expression)

    val results = SmartList<ResolveResult>()
    val name = StringUtils.uncapitalize(myReferencedName)
    VueTemplateScopesResolver.resolve(myRef, Processor { resolveResult ->
      val element = resolveResult.element as? JSPsiNamedElementBase
      if (element != null && name == StringUtils.uncapitalize(element.name)) {
        remapSetterGetterIfNeeded(results, resolveResult, access)
        return@Processor false
      }
      true
    })
    return results.toTypedArray()
  }

  private fun remapSetterGetterIfNeeded(results: MutableList<ResolveResult>,
                                        resolveResult: ResolveResult,
                                        access: ReadWriteAccessDetector.Access) {
    val resolvedElement = resolveResult.element
    when (val element = if (resolvedElement is VueImplicitElement) resolvedElement.context else resolvedElement) {
      is JSFunctionItem -> {
        val add: (JSFunctionItem) -> Unit = if (resolvedElement is VueImplicitElement)
          { it -> results.add(JSResolveResult(resolvedElement.copyWithProvider(it))) }
        else
          { it -> results.add(JSResolveResult(it)) }
        when {
          element.isGetProperty && access == ReadWriteAccessDetector.Access.Write ->
            findPropertyAccessor(element, true, add)

          element.isSetProperty && access == ReadWriteAccessDetector.Access.Read ->
            findPropertyAccessor(element, false, add)

          else -> {
            add(element)
            if (access == ReadWriteAccessDetector.Access.ReadWrite) {
              findPropertyAccessor(element, element.isGetProperty, add)
            }
          }
        }
      }
      else -> results.add(resolveResult)
    }
  }

  private fun findPropertyAccessor(function: JSFunctionItem,
                                   isSetter: Boolean,
                                   processor: (JSFunctionItem) -> Unit) {
    val parent = function.parent as? JSClass
    val name = function.name
    if (name != null && parent != null) {
      JSClassUtils.processClassesInHierarchy(parent, true) { cls, _, _ ->
        for (`fun` in cls.functions) {
          if (name == `fun`.name && (`fun`.isGetProperty && !isSetter || `fun`.isSetProperty && isSetter)) {
            processor(`fun`)
            return@processClassesInHierarchy false
          }
        }
        true
      }
    }
  }
}
