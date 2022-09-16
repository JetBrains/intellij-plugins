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
import org.jetbrains.annotations.Nullable
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ACTION_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.COMMIT
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.CONTEXT
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.DISPATCH
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTER_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_ACTIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_MUTATIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MUTATION_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.PROP_ROOT
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.PROP_TYPE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.isActionContextParameter
import org.jetbrains.vuejs.libraries.vuex.model.store.*

abstract class VuexJSLiteralReferenceProvider : PsiReferenceProvider() {

  companion object {

    val VUEX_INDEXED_ACCESS_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
      override fun getSettings(element: PsiElement): ReferenceProviderSettings? {
        val reference = element.context.castSafelyTo<JSIndexedPropertyAccessExpression>()
                          ?.qualifier
                          ?.castSafelyTo<JSReferenceExpression>()
                        ?: return null
        val referenceName = reference.referenceName
        val accessor = when (referenceName) {
          GETTERS, ROOT_GETTERS -> VuexContainer::getters
          STATE, ROOT_STATE -> VuexContainer::state
          else -> return null
        }

        val namespace = computeNamespace(referenceName, reference)
                        ?: return null

        return object : ReferenceProviderSettings {
          override val symbolAccessor = accessor
          override val baseNamespace: VuexStoreNamespace = namespace
          override val isSoft: Boolean = true
          override val includeMembers: Boolean = referenceName != STATE && referenceName != ROOT_STATE
        }
      }

      private fun computeNamespace(referenceName: String?,
                                   reference: JSReferenceExpression): VuexStoreNamespace? {
        referenceName ?: return null
        when (val firstQualifier = reference.qualifier) {
          null -> {
            // function parameter
            return JSStubBasedPsiTreeUtil.resolveLocally(referenceName, reference)
              .castSafelyTo<JSParameter>()
              ?.let { getNamespaceForGettersOrState(it, referenceName) }
          }
          is JSReferenceExpression -> {
            if (referenceName == ROOT_STATE || referenceName == ROOT_GETTERS) {
              return VuexStaticNamespace.EMPTY
            }
            // action context or global namespace
            return getNamespaceIfActionContextParam(firstQualifier)
                   ?: VuexStaticNamespace.EMPTY
          }
          else -> {
            return null
          }
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
          override val baseNamespace: VuexStoreNamespace = VuexHelpersContextNamespace(true)
          override val isSoft: Boolean = false
        }
      }
    }

    val VUEX_ARRAY_ITEM_OR_OBJECT_PROP_VALUE_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
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
          override val baseNamespace: VuexStoreNamespace = VuexHelpersContextNamespace(false)
          override val isSoft: Boolean = false
        }
      }
    }

    val VUEX_DISPATCH_COMMIT_OBJECT_ARG_REF_PROVIDER = object : VuexJSLiteralReferenceProvider() {
      override fun getSettings(element: PsiElement): ReferenceProviderSettings? {
        return element.context?.castSafelyTo<JSProperty>()
          ?.takeIf { it.name == PROP_TYPE }
          ?.context
          ?.let { VUEX_CALL_ARGUMENT_REF_PROVIDER.getSettings(it) }
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
        val namespace = computeNamespace(accessor, functionRef, functionName, element)
                        ?: return null
        return object : ReferenceProviderSettings {
          override val symbolAccessor = accessor
          override val baseNamespace: VuexStoreNamespace = namespace
          override val isSoft: Boolean = accessor != null
        }
      }

      private fun computeNamespace(accessor: VuexSymbolAccessor?,
                                   functionRef: JSReferenceExpression,
                                   functionName: String,
                                   element: PsiElement): VuexStoreNamespace? {
        if (accessor !== null) {
          val qualifier = functionRef.qualifier
          if (qualifier === null) {
            // Ensure we are within a correct context
            val param = JSStubBasedPsiTreeUtil.resolveLocally(functionName, functionRef)
              ?.castSafelyTo<JSParameter>()
            if (param?.context is JSDestructuringShorthandedProperty) {
              if (isPossiblyStoreActionContextParam(param)) {
                if (isRootCall(functionName, element))
                  return VuexStaticNamespace.EMPTY
                else
                  return VuexStoreActionContextNamespace()
              }
            }
            else {
              if (param?.contextOfType(JSFunction::class)
                  ?.let {
                    it as? JSProperty ?: it.context as? JSProperty
                  }
                  ?.context?.context
                  ?.let { getFunctionReference(it) }
                  ?.referenceName
                  ?.takeIf {
                    (functionName == DISPATCH && it == MAP_ACTIONS)
                    || (functionName == COMMIT && it == MAP_MUTATIONS)
                  } != null) {
                return object : VuexHelpersContextNamespace(false) {
                  override fun get(element: PsiElement): String =
                    JSStubBasedPsiTreeUtil.resolveLocally(functionName, element)?.let { super.get(it) } ?: ""
                }
              }
              return null
            }
          }
          else {
            return qualifier.castSafelyTo<JSReferenceExpression>()
              ?.let {
                if (!isRootCall(functionName, element)) {
                  getNamespaceIfActionContextParam(it) ?: VuexStaticNamespace.EMPTY
                }
                else {
                  VuexStaticNamespace.EMPTY
                }
              }
          }
        }
        else {
          return VuexStaticNamespace.EMPTY
        }
        return null
      }
    }

    private fun getNamespaceIfActionContextParam(contextReferenceExpression: JSReferenceExpression): VuexStoreNamespace? =
      contextReferenceExpression.takeIf { it.qualifier == null }
        ?.referenceName
        ?.takeIf { it == CONTEXT }
        ?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, contextReferenceExpression) }
        ?.takeIf { isActionContextParameter(it) && isPossiblyStoreContext(it) }
        ?.let {
          VuexStoreActionContextNamespace()
        }

    private fun isRootCall(functionName: @Nullable String,
                           element: PsiElement): Boolean =
      (functionName == DISPATCH || functionName == COMMIT)
      && element.contextOfType(JSCallExpression::class)
        ?.arguments
        ?.takeIf { it.getOrNull(0) == element }
        ?.getOrNull(if (element is JSObjectLiteralExpression) 1 else 2)
        ?.castSafelyTo<JSObjectLiteralExpression>()
        ?.findProperty(PROP_ROOT)
        ?.value
        ?.castSafelyTo<JSLiteralExpression>()
        ?.getExpressionKind(false) == JSLiteralExpressionKind.TRUE

    fun getFunctionReference(callContext: PsiElement?): JSReferenceExpression? {
      return callContext?.let {
        if (it is JSArgumentList) it.context else it
      }
        ?.castSafelyTo<JSCallExpression>()
        ?.methodExpression
        ?.castSafelyTo<JSReferenceExpression>()
    }
  }

  abstract fun getSettings(element: PsiElement): ReferenceProviderSettings?

  override fun getReferencesByElement(element: PsiElement, processingContext: ProcessingContext): Array<PsiReference> {
    if (element is JSLiteralExpression) {
      val text = getTextIfLiteral(element) ?: return PsiReference.EMPTY_ARRAY
      val settings = getSettings(element)
      if (settings != null && isVueContext(element)) {
        var lastIndex = 0
        var index = if (settings.includeMembers) text.indexOf('/') else -1
        val result = mutableListOf<PsiReference>()

        fun addReference(start: Int, end: Int) {
          if (start < end) {
            // The actual range in the text is offset by 1 due to quotes in the quoted literal
            result.add(VuexStoreSymbolStringReference(
              element, TextRange(start + 1, end + 1), settings.symbolAccessor, text.substring(0, end),
              end == text.length, settings.baseNamespace, settings.isSoft, settings.includeMembers))
          }
        }

        while (index > 0) {
          addReference(lastIndex, index)
          lastIndex = index + 1
          index = text.indexOf('/', lastIndex)
        }
        addReference(lastIndex, text.length)
        return result.toTypedArray()
      }
    }
    return PsiReference.EMPTY_ARRAY
  }


  interface ReferenceProviderSettings {
    val symbolAccessor: VuexSymbolAccessor?
    val baseNamespace: VuexStoreNamespace
    val isSoft: Boolean
    val includeMembers: Boolean get() = true
  }

}
