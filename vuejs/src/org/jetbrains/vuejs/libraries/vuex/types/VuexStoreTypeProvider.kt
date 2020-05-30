// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.types

import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.VUEX_PACKAGE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.isActionContextParameter
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStaticNamespace
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreNamespace
import org.jetbrains.vuejs.libraries.vuex.model.store.getNamespaceForGettersOrState

object VuexStoreTypeProvider {

  fun addTypeFromResolveResult(evaluator: JSTypeEvaluator, result: PsiElement): Boolean {
    if (result is TypeScriptField) {
      val typeConstructor = getTypeConstructor(result.name) ?: return false
      if (result.containingFile.parent?.parent?.name != VUEX_PACKAGE) return false
      if (!isVueContext(result)) return false
      evaluator.addType(typeConstructor(result, VuexStaticNamespace.EMPTY), result, true)
      return true
    }
    else if (result is JSParameter) {
      if (isActionContextParameter(result)) {
        if (!isVueContext(result)) return false
        evaluator.addType(VuexActionContextType(result), result, true)
        return true
      }
      val name = result.name
      val typeConstructor = getTypeConstructor(name) ?: return false
      val namespace: VuexStoreNamespace = getNamespaceForGettersOrState(result, name!!) ?: return false
      if (!isVueContext(result)) return false
      evaluator.addType(typeConstructor(result, namespace), result, true)
      return true
    }
    return false
  }

  private fun getTypeConstructor(name: String?) = when (name) {
    STATE, ROOT_STATE -> ::VuexContainerStateType
    GETTERS, ROOT_GETTERS -> ::VuexContainerGettersType
    else -> null
  }
}
