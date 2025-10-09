// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.types.JSRecursiveTypeVisitor
import com.intellij.lang.javascript.psi.types.JSTypeBaseImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext

interface VueCompleteType : JSType

fun createStrictTypeSource(element: PsiElement?) =
  JSTypeSource(element, JSTypeSource.SourceLanguage.TS, true)

fun JSType.asCompleteType(): VueCompleteType =
  this as? VueCompleteType ?: VueCompleteTypeImpl(this)

private class VueCompleteTypeImpl(private val baseType: JSType, source: JSTypeSource) : JSTypeBaseImpl(source), VueCompleteType {

  constructor(baseType: JSType) : this(baseType, baseType.source)

  override fun copyWithNewSource(source: JSTypeSource): JSType = VueCompleteTypeImpl(baseType, source)

  override fun hashCodeImpl(): Int = baseType.hashCode()

  override fun acceptChildren(visitor: JSRecursiveTypeVisitor) {
    baseType.accept(visitor)
  }

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    type is VueCompleteTypeImpl
    && type.baseType.isEquivalentTo(baseType, context, allowResolve)

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueCompleteTypeImpl(")
      baseType.buildTypeText(format, builder)
      builder.append(")")
      return
    }
    substitute().buildTypeText(format, builder)
  }

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType = baseType

}