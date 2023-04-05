// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptReferenceExpressionResolver
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.psi.ResolveResult
import com.intellij.util.SmartList
import org.angular2.codeInsight.Angular2ComponentPropertyResolveResult
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression

class Angular2ReferenceExpressionResolver(expression: JSReferenceExpressionImpl,
                                          ignorePerformanceLimits: Boolean) : TypeScriptReferenceExpressionResolver(expression,
                                                                                                                    ignorePerformanceLimits) {

  override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    if (myReferencedName == null) return ResolveResult.EMPTY_ARRAY
    if (myRef is Angular2PipeReferenceExpression) {
      return resolvePipeNameReference(expression, incompleteCode)
    }
    else if (myQualifier == null || myQualifier is JSThisExpression) {
      return resolveTemplateVariable(expression)
    }
    return super.resolve(expression, incompleteCode)
  }

  override fun postProcessIndexResults(results: Array<ResolveResult>): Array<ResolveResult> {
    return results
  }

  private fun resolvePipeNameReference(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    if (!incompleteCode) {
      val results = expression.multiResolve(true)
      //expected type evaluator uses incomplete = true results so we have to cache it and reuse inside incomplete = false
      return JSTypeSignatureChooser(expression.parent as JSCallExpression).chooseOverload(results)
    }

    val scope = Angular2DeclarationsScope(expression)
    val pipe = Angular2EntitiesProvider.findPipes(expression.project, myReferencedName!!).find { scope.contains(it) }
               ?: return ResolveResult.EMPTY_ARRAY
    return if (!pipe.transformMembers.isEmpty())
      pipe.transformMembers.map { JSResolveResult(it) }.toTypedArray()
    else
      arrayOf(JSResolveResult(pipe.typeScriptClass ?: pipe.sourceElement))
  }

  private fun resolveTemplateVariable(expression: JSReferenceExpressionImpl): Array<ResolveResult> {
    myReferencedName!!

    val access = JSReadWriteAccessDetector.ourInstance
      .getExpressionAccess(expression)

    val results = SmartList<ResolveResult>()
    Angular2TemplateScopesResolver.resolve(myRef) { resolveResult ->
      val element = resolveResult.element as? JSPsiElementBase
      if (element != null && myReferencedName == element.name) {
        remapSetterGetterIfNeeded(results, resolveResult, access)
        return@resolve false
      }
      true
    }
    return results.toTypedArray<ResolveResult>()
  }

  companion object {

    private fun remapSetterGetterIfNeeded(results: MutableList<ResolveResult>,
                                          resolveResult: ResolveResult,
                                          access: ReadWriteAccessDetector.Access) {
      val element = resolveResult.element as JSPsiElementBase?
      if (element !is TypeScriptFunction) {
        results.add(resolveResult)
        return
      }
      val add: (JSFunction) -> Unit =
        if (resolveResult is Angular2ComponentPropertyResolveResult)
          { function -> results.add(resolveResult.copyWith(function)) }
        else
          { function -> results.add(JSResolveResult(function)) }
      val function = element as TypeScriptFunction?
      if (function!!.isGetProperty && access == ReadWriteAccessDetector.Access.Write) {
        findPropertyAccessor(function, true, add)
      }
      else if (function.isSetProperty && access == ReadWriteAccessDetector.Access.Read) {
        findPropertyAccessor(function, false, add)
      }
      else {
        add(function)
        if (access == ReadWriteAccessDetector.Access.ReadWrite) {
          findPropertyAccessor(function, function.isGetProperty, add)
        }
      }
    }

    @JvmStatic
    fun findPropertyAccessor(function: TypeScriptFunction,
                             isSetter: Boolean,
                             processor: (JSFunction) -> Unit) {
      val parent = function.parent as? TypeScriptClass
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
}
