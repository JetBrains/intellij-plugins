// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.types

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.libraries.vuex.model.store.*

class VuexContainerStateType private constructor(source: JSTypeSource, element: PsiElement, baseNamespace: VuexStoreNamespace)
  : VuexContainerPropertyTypeBase(source, element, baseNamespace) {

  constructor(element: PsiElement, baseNamespace: VuexStoreNamespace)
    : this(JSTypeSource(element.containingFile, element, JSTypeSource.SourceLanguage.TS, true), element, baseNamespace)

  override val kind: String = "state"

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return VuexContainerStateType(source, element, baseNamespace)
  }

  override fun createStateRecord(context: VuexStoreContext, baseNamespace: String): JSRecordType? {
    val result = mutableListOf<JSRecordType.TypeMember>()
    val prefixLength = baseNamespace.length
    // TODO merge types from parent namespace states types
    context.visit { qualifiedName, container ->
      if (container is VuexModule
          && qualifiedName.startsWith(baseNamespace)
          && qualifiedName.length > baseNamespace.length
          && qualifiedName.indexOf('/', prefixLength + 1) < 0) {
        val name = qualifiedName.substring(prefixLength)
        val type = VuexContainerStateType(source, element, VuexStaticNamespace(qualifiedName))
        result.add(JSRecordTypeImpl.PropertySignatureImpl(
          name, type, false, false,
          VuexStoreStateElement(name, qualifiedName, container.source, type)))
      }
    }
    context.visitSymbols(VuexContainer::state) { qualifiedName, symbol ->
      if (qualifiedName.startsWith(baseNamespace)
          && qualifiedName.indexOf('/', prefixLength + 1) < 0) {
        result.add(symbol.getPropertySignature(baseNamespace, qualifiedName))
      }
    }
    return JSSimpleRecordTypeImpl(source, result)
  }
}
