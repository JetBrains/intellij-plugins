// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.types

import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import org.jetbrains.vuejs.libraries.vuex.VuexUtils
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.VUEX_PACKAGE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.getNamespaceFromMapper
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.NamespaceProvider
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.getFunctionReference

object VuexStoreTypeProvider {

  fun addTypeFromResolveResult(evaluator: JSTypeEvaluator, result: PsiElement): Boolean {
    if (result is TypeScriptField) {
      val typeConstructor = getTypeConstructor(result.name) ?: return false
      if (result.containingFile.parent?.parent?.name != VUEX_PACKAGE) return false
      evaluator.addType(typeConstructor(result, ""), result, true)
      return true
    }
    else if (result is JSParameter) {
      val typeConstructor = getTypeConstructor(result.name) ?: return false
      val functionDef = result.contextOfType(JSFunction::class)
      val callContext = (functionDef as? JSProperty ?: functionDef?.context as? JSProperty)
                          ?.let { it.context?.context }
                        ?: functionDef?.context
      val functionReference = getFunctionReference(callContext)
      val namespaceProvider: NamespaceProvider
      when (functionReference?.referenceName) {
        in VuexUtils.VUEX_MAPPERS -> namespaceProvider = {
          getNamespaceFromMapper(it, false)
        }
        in VuexUtils.VUEX_DEC_MAPPERS -> if (functionReference?.context?.context !is ES6Decorator)
          return false
        else namespaceProvider = {
          getNamespaceFromMapper(it, true)
        }
        else -> return false
      }
      evaluator.addType(typeConstructor(result, namespaceProvider(result)), result, true)
      return true
    }
    return false
  }

  private fun getTypeConstructor(name: String?) = when (name) {
    STATE -> ::VuexContainerStateType
    GETTERS -> ::VuexContainerGettersType
    else -> null
  }
}
