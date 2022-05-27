// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.vuex.types

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexContainer
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreContext
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreNamespace

class VuexContainerGettersType private constructor(source: JSTypeSource, element: PsiElement, baseNamespace: VuexStoreNamespace)
  : VuexContainerPropertyTypeBase(source, element, baseNamespace) {

  constructor(element: PsiElement, baseNamespace: VuexStoreNamespace)
    : this(JSTypeSource(element, JSTypeSource.SourceLanguage.TS, true), element, baseNamespace)

  override val kind: String = "getters"

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return VuexContainerGettersType(source, element, baseNamespace)
  }

  override fun createStateRecord(context: VuexStoreContext, baseNamespace: String): JSRecordType {
    val result = mutableListOf<JSRecordType.TypeMember>()
    context.visitSymbols(VuexContainer::getters) { qualifiedName, symbol ->
      if (qualifiedName.startsWith(baseNamespace)) {
        result.add(symbol.getPropertySignature(baseNamespace, qualifiedName))
      }
    }
    return JSSimpleRecordTypeImpl(source, result)
  }
}
