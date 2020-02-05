// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement

interface VuexNamedSymbol {
  val name: String
  val source: PsiElement
  val resolveTarget: PsiElement
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
  val jsType: JSType?
  override val resolveTarget: PsiElement
    get() = JSLocalImplicitElementImpl(name, jsType, source, JSImplicitElement.Type.Property)
}

interface VuexAction : VuexNamedSymbol {
  val isRoot: Boolean

  // TODO provide proper resolve target type
  override val resolveTarget: PsiElement
    get() = JSLocalImplicitElementImpl(name, null, source, JSImplicitElement.Type.Function)
}

interface VuexGetter : VuexNamedSymbol {
  val jsType: JSType?
  override val resolveTarget: PsiElement
    get() = JSLocalImplicitElementImpl(name, jsType, source, JSImplicitElement.Type.Property)
}

interface VuexMutation : VuexNamedSymbol {
  // TODO provide proper resolve target type
  override val resolveTarget: PsiElement
    get() = JSLocalImplicitElementImpl(name, null, source, JSImplicitElement.Type.Function)
}
