// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueImplicitElement

interface VuexNamedSymbol {
  val name: String
  val source: PsiElement

  fun getResolveTarget(namespace: String, qualifiedName: String): JSLocalImplicitElementImpl
}

interface VuexContainer {
  val isNamespaced: Boolean
  val isRoot: Boolean

  val state: Map<String, VuexStateProperty>
  val actions: Map<String, VuexAction>
  val mutations: Map<String, VuexMutation>
  val getters: Map<String, VuexGetter>

  val modules: Map<String, VuexModule>

  val source: PsiElement
  val initializer: JSObjectLiteralExpression?
}

interface VuexStore : VuexContainer {
  override val isNamespaced: Boolean
    get() = false

  override val isRoot: Boolean
    get() = true
}

interface VuexModule : VuexContainer, VuexNamedSymbol {

  override val isRoot: Boolean
    get() = false

}

interface VuexStateProperty : VuexNamedSymbol {
  fun getPropertySignature(namespace: String, qualifiedName: String): JSRecordType.PropertySignature =
    getResolveTarget(namespace, qualifiedName).let {
      JSRecordTypeImpl.PropertySignatureImpl(qualifiedName.substring(namespace.length), it.jsType, false, false, it)
    }
}

interface VuexAction : VuexNamedSymbol {
  val isRoot: Boolean

  override fun getResolveTarget(namespace: String, qualifiedName: String): JSLocalImplicitElementImpl {
    // TODO provide proper resolve target type
    return VueImplicitElement(qualifiedName.substring(namespace.length), null, source,
                              JSImplicitElement.Type.Function, true)
  }
}

interface VuexGetter : VuexNamedSymbol {
  fun getPropertySignature(namespace: String, qualifiedName: String): JSRecordType.PropertySignature =
    getResolveTarget(namespace, qualifiedName).let {
      JSRecordTypeImpl.PropertySignatureImpl(qualifiedName.substring(namespace.length), it.jsType, false, false, it)
    }
}

interface VuexMutation : VuexNamedSymbol {

  override fun getResolveTarget(namespace: String, qualifiedName: String): JSLocalImplicitElementImpl {
    // TODO provide proper resolve target type
    return VueImplicitElement(qualifiedName.substring(namespace.length), null, source,
                              JSImplicitElement.Type.Function, true)
  }
}
