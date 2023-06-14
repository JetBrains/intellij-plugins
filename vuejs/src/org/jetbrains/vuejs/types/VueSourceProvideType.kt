// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.types.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import com.intellij.util.ProcessingContext

class VueSourceProvideType private constructor(typeSource: JSTypeSource, private val element: PsiElement)
  : JSSimpleTypeBaseImpl(typeSource), JSCodeBasedType {

  constructor(element: PsiElement) : this(JSTypeSourceFactory.createTypeSource(element, true), element)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueSourceProvideType(source, element)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    (type is VueSourceProvideType && type.element == element)

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType =
    when (element) {
      is JSTypeOwner -> element.jsType
      is JSLiteralExpression -> element.contextOfType<JSCallExpression>()
        ?.arguments
        ?.getOrNull(1)
        ?.let { JSCodeBasedTypeFactory.getCodeBasedType(it, true, false) }
      else -> JSUnknownType.TS_INSTANCE
    }?.let { VueUnwrapRefType(it, element) } ?: JSUnknownType.TS_INSTANCE

  override fun hashCodeImpl(): Int = element.hashCode()

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueSourceProvideType")
      return
    }
    substitute().buildTypeText(format, builder)
  }

}