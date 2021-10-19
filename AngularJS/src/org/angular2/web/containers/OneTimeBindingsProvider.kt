// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.javascript.web.codeInsight.html.attributes.WebSymbolHtmlAttributeInfo
import com.intellij.javascript.web.symbols.*
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.model.Pointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.Stack
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS
import java.util.concurrent.ConcurrentHashMap

internal class OneTimeBindingsProvider : WebSymbolsContainer {

  override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if (namespace == WebSymbolsContainer.Namespace.JS && kind == KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS) {
      params.registry
        .runNameMatchQuery(
          listOfNotNull(WebSymbolsContainer.NAMESPACE_JS, KIND_NG_DIRECTIVE_INPUTS, name),
          context = context.toList())
        .asSequence()
        .filter { isOneTimeBindingProperty(it) }
        .map { Angular2OneTimeBinding(it) }
        .toList()
    }
    else emptyList()

  override fun createPointer(): Pointer<out WebSymbolsContainer> =
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
      val source = property.source ?: return true

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

  private class Angular2OneTimeBinding(delegate: WebSymbol) : WebSymbolDelegate<WebSymbol>(delegate) {

    override val kind: SymbolKind get() = KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS

    override val priority: WebSymbol.Priority
      get() = WebSymbol.Priority.LOW

    override val properties: Map<String, Any>
      get() = super.priority
                ?.let { super.properties + Pair(PROP_DELEGATE_PRIORITY, it) }
              ?: super.properties


    override val attributeValue: WebSymbol.AttributeValue?
      get() = if (WebSymbolHtmlAttributeInfo.isBooleanType(jsType)) {
        WebSymbolHtmlAttributeValueData(WebSymbol.AttributeValueKind.PLAIN,
                                        WebSymbol.AttributeValueType.JAVASCRIPT, false,
                                        null,
                                        JSCompositeTypeFactory.createUnionType(
                                          JSTypeSource.EXPLICITLY_DECLARED,
                                          JSStringLiteralTypeImpl(name, false, JSTypeSource.EXPLICITLY_DECLARED),
                                          JSStringLiteralTypeImpl("true", false, JSTypeSource.EXPLICITLY_DECLARED),
                                          JSStringLiteralTypeImpl("false", false, JSTypeSource.EXPLICITLY_DECLARED)
                                        ), null)
      }
      else null

    override fun createPointer(): Pointer<out WebSymbol> {
      val delegate = this.delegate.createPointer()
      return Pointer {
        delegate.dereference()?.let { Angular2OneTimeBinding(it) }
      }
    }

  }
}