// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSType.TypeTextFormat
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.JSTypeWithIncompleteSubstitution
import com.intellij.lang.javascript.psi.types.JSCodeBasedType
import com.intellij.lang.javascript.psi.types.JSSimpleTypeBaseImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext

abstract class Angular2BaseType<T : PsiElement?> protected constructor(source: JSTypeSource, sourceClass: Class<T>)
  : JSSimpleTypeBaseImpl(source), JSCodeBasedType, JSTypeWithIncompleteSubstitution {
  protected constructor(source: T, sourceClass: Class<T>) : this(JSTypeSourceFactory.createTypeSource(source, true), sourceClass)

  init {
    sourceClass.cast(source.sourceElement)
  }

  protected abstract val typeOfText: String?
  protected abstract fun resolveType(context: JSTypeSubstitutionContext): JSType?
  override fun getSourceElement(): T {
    @Suppress("UNCHECKED_CAST")
    return super<JSSimpleTypeBaseImpl>.getSourceElement()!! as T
  }

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType? {
    val type = resolveType(context)
    if (type != null) {
      context.add(type)
    }
    return type
  }

  override fun substituteCompletely(): JSType {
    return this.substitute().substitute()
  }

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean {
    return type.javaClass == this.javaClass && type.sourceElement == sourceElement
  }

  override fun hashCodeImpl(): Int {
    return sourceHashCode
  }

  override fun buildTypeTextImpl(format: TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == TypeTextFormat.SIMPLE) {
      builder.append("ngtypeof#$typeOfText")
      return
    }
    substitute().buildTypeText(format, builder)
  }
}