// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.contextOfType
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import java.util.*

class VueSourceProvideType private constructor(typeSource: JSTypeSource,
                                               private val element: PsiElement,
                                               private val symbol: PsiNamedElement?)
  : JSSimpleTypeBaseImpl(typeSource), JSCodeBasedType {

  constructor(element: PsiElement, symbol: PsiNamedElement?) : this(JSTypeSourceFactory.createTypeSource(element, true), element, symbol)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueSourceProvideType(source, element, symbol)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    (type is VueSourceProvideType && type.element == element && type.symbol == symbol)

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType {
    if (symbol != null) {
      val explicitType = JSResolveUtil.getElementJSType(symbol)?.substitute().asSafely<JSGenericTypeImpl>()?.arguments?.getOrNull(0)
      if (explicitType != null) {
        return explicitType
      }
    }

    return when (element) {
             is JSInitializerOwner -> JSResolveUtil.getElementJSType(element.initializer)
             is JSLiteralExpression, is JSReferenceExpression -> element.contextOfType<JSCallExpression>()
               ?.arguments
               ?.getOrNull(1)
               ?.let { JSCodeBasedTypeFactory.getCodeBasedType(it, true, false) }
             is JSTypeOwner -> element.jsType
             else -> JSUnknownType.TS_INSTANCE
           }?.let { VueUnwrapRefType(it, element) } ?: JSUnknownType.TS_INSTANCE
  }

  override fun hashCodeImpl(): Int = Objects.hash(element, symbol)

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueSourceProvideType")
      return
    }
    substitute().buildTypeText(format, builder)
  }

}