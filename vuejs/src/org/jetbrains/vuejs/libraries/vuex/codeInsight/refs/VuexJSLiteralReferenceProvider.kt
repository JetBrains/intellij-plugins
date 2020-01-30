// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight.refs

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.contextOfType
import com.intellij.util.ProcessingContext
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.libraries.vuex.VuexUtils
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ACTION_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.COMMIT
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.DISPATCH
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTER_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_ACTIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_MUTATIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MUTATION_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.getNamespaceFromMapper
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexContainer
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexNamedSymbol

abstract class VuexJSLiteralReferenceProvider : PsiReferenceProvider() {

  companion object {

    val VUEX_INDEXED_ACCESS_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
      override fun getSettings(element: PsiElement): ReferenceProviderSettings? {
        val accessName = element.context.castSafelyTo<JSIndexedPropertyAccessExpression>()
          ?.qualifier
          ?.castSafelyTo<JSReferenceExpression>()
          ?.referenceName
        val accessor = when (accessName) {
          VuexUtils.GETTERS, VuexUtils.ROOT_GETTERS -> VuexContainer::getters
          VuexUtils.STATE, VuexUtils.ROOT_STATE -> VuexContainer::state
          else -> return null
        }
        return object : ReferenceProviderSettings {
          override val symbolAccessor = accessor
          override val baseNamespaceProvider: (PsiElement) -> String = { "" }
          override val isSoft: Boolean = true
        }
      }
    }

    val VUEX_DECORATOR_ARGUMENT_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
      override fun getSettings(element: PsiElement): ReferenceProviderSettings? {
        val decoratorName = PsiTreeUtil.getContextOfType(element, ES6Decorator::class.java)
          ?.decoratorName
        val accessor = when (decoratorName) {
          ACTION_DEC -> VuexContainer::actions
          MUTATION_DEC -> VuexContainer::mutations
          GETTER_DEC -> VuexContainer::getters
          STATE_DEC -> VuexContainer::state
          else -> return null
        }
        return object : ReferenceProviderSettings {
          override val symbolAccessor = accessor
          override val baseNamespaceProvider: (PsiElement) -> String = {
            getNamespaceFromMapper(it, true)
          }
          override val isSoft: Boolean = false
        }
      }
    }

    val VUEX_CALL_ARRAY_OBJECT_ITEM_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
      override fun getSettings(element: PsiElement): ReferenceProviderSettings? {
        val functionName = when (val context = element.context) {
          is JSProperty -> context.context?.context
          is JSArrayLiteralExpression -> context.context
          else -> null
        }
          ?.let { getFunctionReference(it) }
          ?.referenceName
        val accessor = when (functionName) {
          MAP_ACTIONS -> VuexContainer::actions
          MAP_MUTATIONS -> VuexContainer::mutations
          MAP_GETTERS -> VuexContainer::getters
          MAP_STATE -> VuexContainer::state
          else -> return null
        }
        return object : ReferenceProviderSettings {
          override val symbolAccessor = accessor
          override val baseNamespaceProvider: (PsiElement) -> String = {
            getNamespaceFromMapper(it, false)
          }
          override val isSoft: Boolean = false
        }
      }
    }

    val VUEX_CALL_ARGUMENT_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
      override fun getSettings(element: PsiElement): ReferenceProviderSettings? {
        val functionRef = getFunctionReference(element.context) ?: return null
        val functionName = functionRef.referenceName!!
        val accessor = when (functionName) {
          DISPATCH -> VuexContainer::actions
          COMMIT -> VuexContainer::mutations
          else -> null
        }
        val namespaceProvider: (PsiElement) -> String
        if (accessor !== null && functionRef.qualifier === null) {
          // Ensure we are within a correct context
          val mapperName = JSStubBasedPsiTreeUtil.resolveLocally(functionName, functionRef)
            ?.castSafelyTo<JSParameter>()
            ?.contextOfType(JSFunction::class)
            ?.context
            ?.castSafelyTo<JSProperty>()
            ?.context?.context
            ?.let { getFunctionReference(it) }
            ?.referenceName
          if ((functionName == DISPATCH && mapperName == MAP_ACTIONS)
              || (functionName == COMMIT && mapperName == MAP_MUTATIONS)) {
            namespaceProvider = { psiElement ->
              JSStubBasedPsiTreeUtil.resolveLocally(functionName, psiElement)
                ?.let { getNamespaceFromMapper(it, false) }
              ?: ""
            }
          }
          else return null
        }
        else {
          namespaceProvider = { "" }
        }
        return object : ReferenceProviderSettings {
          override val symbolAccessor = accessor
          override val baseNamespaceProvider: (PsiElement) -> String = namespaceProvider
          override val isSoft: Boolean = true
        }
      }
    }
  }

  protected abstract fun getSettings(element: PsiElement): ReferenceProviderSettings?

  protected fun getFunctionReference(callContext: PsiElement?): JSReferenceExpression? {
    return callContext?.let {
        if (it is JSArgumentList) it.context else it
      }
      ?.castSafelyTo<JSCallExpression>()
      ?.methodExpression
      ?.castSafelyTo<JSReferenceExpression>()
  }

  override fun getReferencesByElement(element: PsiElement, processingContext: ProcessingContext): Array<PsiReference> {
    if (element is JSLiteralExpression) {
      val settings = getSettings(element)
      if (settings != null && isVueContext(element)) {
        val text = getTextIfLiteral(element) ?: return PsiReference.EMPTY_ARRAY
        var lastIndex = 0
        var index = text.indexOf('/')
        val result = mutableListOf<PsiReference>()
        while (index > 0) {
          result.add(VuexNamespaceReference(element, TextRange(lastIndex, index), text.substring(0, index),
                                            settings.baseNamespaceProvider, soft = settings.isSoft))
          lastIndex = index + 1
          index = text.indexOf('/', lastIndex)
        }

        val accessor = settings.symbolAccessor
        if (accessor == null) {
          result.add(VuexNamespaceReference(element, TextRange(lastIndex, text.length), text,
                                            settings.baseNamespaceProvider, soft = settings.isSoft))
        }
        else {
          result.add(VuexEntityReference(element, TextRange(lastIndex, text.length), accessor, text,
                                         settings.baseNamespaceProvider, soft = settings.isSoft))
        }
        return result.toTypedArray()
      }
    }
    return PsiReference.EMPTY_ARRAY
  }


  protected interface ReferenceProviderSettings {
    val symbolAccessor: ((VuexContainer) -> Map<String, VuexNamedSymbol>)?
    val baseNamespaceProvider: (PsiElement) -> String
    val isSoft: Boolean
  }

}
