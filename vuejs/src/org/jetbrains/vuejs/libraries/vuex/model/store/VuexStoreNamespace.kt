// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.util.JSDestructuringUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.contextOfType
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.stubSafeCallArguments
import org.jetbrains.vuejs.libraries.vuex.VuexUtils
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.getFunctionReference

interface VuexStoreNamespace {
  fun get(element: PsiElement): String
}

class VuexStaticNamespace(val name: String) : VuexStoreNamespace {
  override fun get(element: PsiElement): String = name

  override fun equals(other: Any?): Boolean {
    return (other as? VuexStaticNamespace)?.name == name
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  companion object {
    val EMPTY = VuexStaticNamespace("")
  }
}

open class VuexHelpersContextNamespace(private val decorator: Boolean) : VuexStoreNamespace {

  override fun get(element: PsiElement): String {
    val call = PsiTreeUtil.getContextOfType(element, JSCallExpression::class.java)
    val functionRef = call?.methodExpression?.castSafelyTo<JSReferenceExpression>()
                      ?: return ""
    return (if (functionRef.qualifier !== null)
      functionRef.qualifier.castSafelyTo<JSReferenceExpression>()
        ?.resolve()
        ?.castSafelyTo<JSVariable>()
        ?.let { getNamespaceFromHelpersVar(it, decorator) }
    else {
      val functionName = functionRef.referenceName ?: return ""
      val location = JSStubBasedPsiTreeUtil.resolveLocally(functionName, functionRef)
      if (location is JSVariable)
        getNamespaceFromHelpersVar(location, decorator)
      else
        call.stubSafeCallArguments
          .getOrNull(0)
          ?.castSafelyTo<JSLiteralExpression>()
          ?.let { getTextIfLiteral(it) }
    }) ?: ""
  }

  private fun getNamespaceFromHelpersVar(variable: JSVariable, decorator: Boolean): String? {
    return (variable.initializer
            ?: JSDestructuringUtil.getNearestDestructuringInitializer(variable))
      ?.castSafelyTo<JSCallExpression>()
      ?.takeIf {
        it.methodExpression?.castSafelyTo<JSReferenceExpression>()
          ?.referenceName == if (decorator) VuexUtils.CREATE_NAMESPACED_DECS else VuexUtils.CREATE_NAMESPACED_HELPERS
      }
      ?.arguments
      ?.getOrNull(0)
      ?.let { getTextIfLiteral(it) }
  }

  override fun equals(other: Any?): Boolean {
    return (other?.javaClass == javaClass)
           && (other as? VuexHelpersContextNamespace)?.decorator == decorator
  }

  override fun hashCode(): Int {
    return decorator.hashCode()
  }
}

class VuexStoreActionContextNamespace : VuexStoreContextNamespace({ it.actions.values })

open class VuexStoreContextNamespace(private val accessor: (VuexContainer) -> Collection<VuexNamedSymbol>) : VuexStoreNamespace {

  override fun get(element: PsiElement): String {
    val originalElement = getApproxOriginalElement(element) ?: return ""
    var result = ""
    VuexModelManager.getVuexStoreContext(originalElement)?.visit { fullName, container ->
      if (accessor(container).any { PsiTreeUtil.isContextAncestor(it.source, originalElement, false) }) {
        result = fullName
      }
    }
    return result
  }

  private fun getApproxOriginalElement(element: PsiElement): PsiElement? {
    CompletionUtil.getOriginalElement(element)?.let { return it }
    return element.containingFile.originalFile.findElementAt(element.startOffset)
  }

  override fun equals(other: Any?): Boolean {
    return (other as? VuexStoreContextNamespace)?.accessor?.javaClass == accessor.javaClass
  }

  override fun hashCode(): Int {
    return accessor.javaClass.hashCode()
  }

}

fun isPossiblyStoreContext(element: PsiElement): Boolean {
  return element.contextOfType<JSFunction>()
    ?.let {
      it is JSProperty
      || it.context is JSProperty
      || it.context?.castSafelyTo<JSVariable>()
        ?.context?.castSafelyTo<JSVarStatement>()
        ?.attributeList?.hasModifier(JSAttributeList.ModifierType.EXPORT) == true
    } == true
}

fun isPossiblyStoreActionContextParam(element: JSParameter): Boolean {
  return element.context
           ?.context?.castSafelyTo<JSDestructuringObject>()
           ?.context?.castSafelyTo<JSDestructuringParameter>()
           ?.let {
             it.contextOfType<JSFunction>()?.parameters?.getOrNull(0) == it
           } == true
         && isPossiblyStoreContext(element)
}

fun getNamespaceForGettersOrState(element: JSParameter, name: String): VuexStoreNamespace? {
  if (element.context is JSDestructuringShorthandedProperty) {
    // ensure we recognize store context only where it looks most appropriate
    if (isPossiblyStoreActionContextParam(element)) {
      if (name == ROOT_GETTERS || name == ROOT_STATE) {
        return VuexStaticNamespace.EMPTY
      }
      else {
        return VuexStoreActionContextNamespace()
      }
    }
  }
  else {
    val functionDef = element.contextOfType(JSFunction::class)
    val callContext = (functionDef as? JSProperty ?: functionDef?.context as? JSProperty)
                        ?.let { it.context?.context }
                      ?: functionDef?.context
    val functionReference = getFunctionReference(callContext)
    when (functionReference?.referenceName) {
      in VuexUtils.VUEX_MAPPERS -> return VuexHelpersContextNamespace(false)
      in VuexUtils.VUEX_DEC_MAPPERS -> {
        if (functionReference?.context?.context is ES6Decorator)
          return VuexHelpersContextNamespace(true)
      }
      else -> if (isPossiblyStoreContext(element)) {
        when (name) {
          GETTERS -> return VuexStoreContextNamespace { it.getters.values }
          STATE -> return VuexStoreContextNamespace {
            it.mutations.values.asSequence().plus(it.getters.values.asSequence()).toList()
          }
          ROOT_GETTERS, ROOT_STATE -> return VuexStaticNamespace.EMPTY
        }
      }
    }
  }
  return null
}
