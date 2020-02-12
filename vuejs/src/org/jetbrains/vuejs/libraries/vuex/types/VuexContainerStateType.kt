// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.types

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.libraries.vuex.model.store.*
import org.jetbrains.vuejs.model.VueImplicitElement

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
    context.visit { namespace, container ->
      if (container is VuexModule
          && namespace.startsWith(baseNamespace)
          && namespace.length > baseNamespace.length
          && namespace.indexOf('/', prefixLength + 1) < 0) {
        val name = namespace.substring(prefixLength)
        val type = VuexContainerStateType(source, element, VuexStaticNamespace(namespace))
        result.add(JSRecordTypeImpl.PropertySignatureImpl(
          name, type, false, true,
          VueImplicitElement(name, type, container.source, JSImplicitElement.Type.Property)))
      }
    }
    context.visitSymbols(VuexContainer::state) { fullName, symbol ->
      if (fullName.startsWith(baseNamespace)
          && fullName.indexOf('/', prefixLength + 1) < 0) {
        result.add(JSRecordTypeImpl.PropertySignatureImpl(
          symbol.name, symbol.jsType, false, false, symbol.resolveTarget))
      }
    }
    return JSSimpleRecordTypeImpl(source, result)
  }
}
