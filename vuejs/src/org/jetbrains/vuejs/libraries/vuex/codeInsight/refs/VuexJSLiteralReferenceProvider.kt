// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight.refs

import com.intellij.lang.javascript.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.libraries.vuex.VuexUtils
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.getNamespaceFromMapper
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexContainer
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexNamedSymbol

abstract class VuexJSLiteralReferenceProvider : PsiReferenceProvider() {

  companion object {

    val VUEX_INDEXED_ACCESS_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
      override fun getConfig(element: PsiElement): ReferenceProviderConfig? {
        val accessName = element.context.castSafelyTo<JSIndexedPropertyAccessExpression>()
          ?.qualifier
          ?.castSafelyTo<JSReferenceExpression>()
          ?.referenceName
        val accessor = when (accessName) {
          VuexUtils.GETTERS, VuexUtils.ROOT_GETTERS -> VuexContainer::getters
          VuexUtils.STATE, VuexUtils.ROOT_STATE -> VuexContainer::state
          else -> return null
        }
        return object : ReferenceProviderConfig {
          override val isNamespaceReference: Boolean = false
          override val symbolAccessor: (VuexContainer) -> Map<String, VuexNamedSymbol> = accessor
          override val baseNamespaceProvider: (PsiElement) -> String = { "" }
          override val isSoft: Boolean = true
        }
      }
    }

    val VUEX_CALL_ARGUMENT_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
      override fun getConfig(element: PsiElement): ReferenceProviderConfig? {
        val context = element.context
        var callContext: PsiElement?
        val plainArg: Boolean
        if (context is JSArrayLiteralExpression) {
          plainArg = false
          callContext = context.context
        }
        else {
          plainArg = true
          callContext = context
        }
        if (callContext is JSArgumentList) {
          callContext = callContext.context
        }
        val functionName = callContext?.castSafelyTo<JSCallExpression>()
          ?.methodExpression
          ?.castSafelyTo<JSReferenceExpression>()
          ?.referenceName
        val accessor = when (functionName) {
          VuexUtils.MAP_ACTIONS, VuexUtils.DISPATCH -> VuexContainer::actions
          VuexUtils.MAP_MUTATIONS, VuexUtils.COMMIT -> VuexContainer::mutations
          VuexUtils.MAP_GETTERS -> VuexContainer::getters
          VuexUtils.MAP_STATE -> VuexContainer::state
          else -> return null
        }
        val namespaceProvider: (PsiElement) -> String
        val mapper = VuexUtils.VUEX_MAPPERS.contains(functionName)
        if (mapper && !plainArg) {
          namespaceProvider = ::getNamespaceFromMapper
        }
        else {
          namespaceProvider = { "" }
        }
        return object : ReferenceProviderConfig {
          override val isNamespaceReference: Boolean = plainArg && mapper
          override val symbolAccessor: (VuexContainer) -> Map<String, VuexNamedSymbol> = accessor
          override val baseNamespaceProvider: (PsiElement) -> String = namespaceProvider
          override val isSoft: Boolean = !mapper
        }
      }
    }
  }

  protected abstract fun getConfig(element: PsiElement): ReferenceProviderConfig?

  override fun getReferencesByElement(element: PsiElement, processingContext: ProcessingContext): Array<PsiReference> {
    if (element is JSLiteralExpression) {
      val config = getConfig(element)
      if (config != null && isVueContext(element)) {
        val text = getTextIfLiteral(element) ?: return PsiReference.EMPTY_ARRAY
        var lastIndex = 0
        var index = text.indexOf('/')
        val result = mutableListOf<PsiReference>()
        while (index > 0) {
          result.add(VuexNamespaceReference(element, TextRange(lastIndex, index), text.substring(0, index),
                                            config.baseNamespaceProvider, soft = config.isSoft))
          lastIndex = index + 1
          index = text.indexOf('/', lastIndex)
        }

        if (config.isNamespaceReference) {
          result.add(VuexNamespaceReference(element, TextRange(lastIndex, text.length), text,
                                            config.baseNamespaceProvider, soft = config.isSoft))
        }
        else {
          result.add(VuexEntityReference(element, TextRange(lastIndex, text.length), config.symbolAccessor, text,
                                         config.baseNamespaceProvider, soft = config.isSoft))
        }
        return result.toTypedArray()
      }
    }
    return PsiReference.EMPTY_ARRAY
  }


  protected interface ReferenceProviderConfig {
    val isNamespaceReference: Boolean
    val symbolAccessor: (VuexContainer) -> Map<String, VuexNamedSymbol>
    val baseNamespaceProvider: (PsiElement) -> String
    val isSoft: Boolean
  }

}
