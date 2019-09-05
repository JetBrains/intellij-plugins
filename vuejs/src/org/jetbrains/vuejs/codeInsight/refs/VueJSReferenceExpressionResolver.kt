// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import com.intellij.util.SmartList
import org.apache.commons.lang.StringUtils
import org.jetbrains.vuejs.codeInsight.template.VueTemplateScopesResolver

class VueJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                       ignorePerformanceLimits: Boolean) :
  JSReferenceExpressionResolver(referenceExpression!!, ignorePerformanceLimits) {

  override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    if (myReferencedName == null) return ResolveResult.EMPTY_ARRAY

    if (myQualifier == null || myQualifier is JSThisExpression) {
      resolveTemplateVariable(expression).let { if (it.isNotEmpty()) return it }
    }
    return super.resolve(expression, incompleteCode)
  }

  private fun resolveTemplateVariable(expression: JSReferenceExpressionImpl): Array<ResolveResult> {
    // TODO merge with Angular code
    assert(myReferencedName != null)
    val access = JSReadWriteAccessDetector.ourInstance
      .getExpressionAccess(expression)

    val results = SmartList<ResolveResult>()
    val name = StringUtils.uncapitalize(myReferencedName)
    VueTemplateScopesResolver.resolve(myRef, Processor { resolveResult ->
      val element = resolveResult.element as? JSPsiElementBase
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
    when (val element = resolveResult.element) {
      is JSFunctionItem -> {
        val add: (JSFunctionItem) -> Unit = { it -> results.add(JSResolveResult(it)) }
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
