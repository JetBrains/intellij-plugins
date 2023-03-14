// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.javascript.web.js.jsType
import com.intellij.javascript.web.webTypes.js.WebTypesTypeScriptSymbolTypeSupport
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.model.Pointer
import com.intellij.navigation.NavigationTarget
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.Stack
import com.intellij.util.containers.mapSmartSet
import com.intellij.webSymbols.*
import com.intellij.webSymbols.html.WebSymbolHtmlAttributeValue
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class OneTimeBindingsProvider : WebSymbolsScope {

  override fun getSymbols(namespace: SymbolNamespace,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (namespace == WebSymbol.NAMESPACE_JS
        && kind == KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS
        && params.queryExecutor.allowResolve) {
      // Avoid any conflicts with attribute selectors over the attribute value
      val attributeSelectors = params.queryExecutor
        .runNameMatchQuery(WebSymbol.NAMESPACE_JS, KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS, name ?: "",
                           scope = scope.toList())
        .filter { it.attributeValue?.required == false }
        .mapSmartSet { it.name }

      params.queryExecutor
        .runNameMatchQuery(
          WebSymbol.NAMESPACE_JS, KIND_NG_DIRECTIVE_INPUTS, name ?: "",
          scope = scope.toList())
        .asSequence()
        .filter { isOneTimeBindingProperty(it) }
        .map { Angular2OneTimeBinding(it, !attributeSelectors.contains(it.name)) }
        .toList()
    }
    else emptyList()

  override fun createPointer(): Pointer<out WebSymbolsScope> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other is OneTimeBindingsProvider

  override fun hashCode(): Int = 0

  companion object {

    const val PROP_DELEGATE_PRIORITY = "ng-delegate-priority"

    private val ONE_TIME_BINDING_EXCLUDES = listOf(Angular2AttributeValueProvider.NG_CLASS_ATTR)
    private val STRING_TYPE: JSType = JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE)

    @JvmStatic
    fun isOneTimeBindingProperty(property: WebSymbol): Boolean {
      if (ONE_TIME_BINDING_EXCLUDES.contains(property.name) || KIND_NG_DIRECTIVE_INPUTS != property.kind) {
        return false
      }
      if (property.virtual) return true
      val type = property.jsType ?: return true
      val source = (property as? PsiSourcedWebSymbol)?.source ?: return true

      return CachedValuesManager.getCachedValue(source) {
        CachedValueProvider.Result.create(ConcurrentHashMap<WebSymbol, Boolean>(),
                                          PsiModificationTracker.MODIFICATION_COUNT)
      }.getOrPut(property) {
        expandStringLiteralTypes(type).isDirectlyAssignableType(
          STRING_TYPE, JSTypeComparingContextService.createProcessingContextWithCache(source))
      }
    }

    private fun expandStringLiteralTypes(type: JSType): JSType =
      TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(
        type).transformTypeHierarchy { toApply -> if (toApply is JSPrimitiveType) STRING_TYPE else toApply }
  }

  private class Angular2OneTimeBinding(delegate: WebSymbol, val requiresValue: Boolean)
    : WebSymbolDelegate<WebSymbol>(delegate), PsiSourcedWebSymbol {

    override val source: PsiElement?
      get() = (delegate as? PsiSourcedWebSymbol)?.source

    override val kind: SymbolKind get() = KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS

    override val priority: WebSymbol.Priority
      get() = WebSymbol.Priority.LOW

    override val properties: Map<String, Any>
      get() = super<WebSymbolDelegate>.priority
                ?.let { super<WebSymbolDelegate>.properties + Pair(PROP_DELEGATE_PRIORITY, it) }
              ?: super<WebSymbolDelegate>.properties


    override val attributeValue: WebSymbolHtmlAttributeValue?
      get() = if (WebTypesTypeScriptSymbolTypeSupport.isBoolean(jsType)) {
        WebSymbolHtmlAttributeValue.create(WebSymbolHtmlAttributeValue.Kind.PLAIN,
                                           WebSymbolHtmlAttributeValue.Type.COMPLEX, false,
                                           null,
                                           JSCompositeTypeFactory.createUnionType(
                                             JSTypeSource.EXPLICITLY_DECLARED,
                                             JSStringLiteralTypeImpl(name, false, JSTypeSource.EXPLICITLY_DECLARED),
                                             JSStringLiteralTypeImpl("true", false, JSTypeSource.EXPLICITLY_DECLARED),
                                             JSStringLiteralTypeImpl("false", false, JSTypeSource.EXPLICITLY_DECLARED)
                                           ))
      }
      else if (!requiresValue)
        WebSymbolHtmlAttributeValue.create(required = false)
      else null

    override fun createPointer(): Pointer<Angular2OneTimeBinding> {
      val delegatePtr = this.delegate.createPointer()
      val requiresValue = this.requiresValue
      return Pointer {
        delegatePtr.dereference()?.let { Angular2OneTimeBinding(it, requiresValue) }
      }
    }

    override fun equals(other: Any?): Boolean =
      other is Angular2OneTimeBinding
      && other.delegate == delegate
      && other.requiresValue == requiresValue

    override fun hashCode(): Int =
      Objects.hash(delegate, requiresValue)

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<WebSymbolDelegate>.getNavigationTargets(project)

    override val psiContext: PsiElement?
      get() = super<WebSymbolDelegate>.psiContext

  }
}