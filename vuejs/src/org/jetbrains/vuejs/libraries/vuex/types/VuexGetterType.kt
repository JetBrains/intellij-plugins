// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.types

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.JSType.TypeTextFormat
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSCodeBasedType
import com.intellij.lang.javascript.psi.types.JSSimpleTypeBaseImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.util.ProcessingContext

class VuexGetterType(source: JSTypeSource,
                     private val element: JSTypeOwner)
  : JSSimpleTypeBaseImpl(source), JSCodeBasedType, JSTypeWithIncompleteSubstitution {

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return VuexGetterType(source, element)
  }

  override fun hashCodeImpl(): Int {
    return element.jsType?.hashCode() ?: 0
  }

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean {
    return (type as? VuexGetterType)?.element == element
  }

  override fun buildTypeTextImpl(format: TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == TypeTextFormat.SIMPLE) {
      builder.append("#$javaClass")
      return
    }
    substitute().buildTypeText(format, builder)
  }

  override fun substituteCompletely(): JSType {
    val getterFunctionType = element.jsType?.substitute()
    return (getterFunctionType as? JSFunctionType)?.returnType
           ?: JSAnyType.get(source)
  }
}
