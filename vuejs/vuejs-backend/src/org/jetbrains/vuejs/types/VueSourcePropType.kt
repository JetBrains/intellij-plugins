// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSCodeBasedType
import com.intellij.lang.javascript.psi.types.JSTypeBaseImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
import com.intellij.psi.PsiNamedElement
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.codeInsight.fixPrimitiveTypes
import org.jetbrains.vuejs.codeInsight.getPropTypeFromDocComment
import org.jetbrains.vuejs.codeInsight.getPropTypeFromGenericType
import org.jetbrains.vuejs.codeInsight.getPropTypeFromPropOptions

class VueSourcePropType
private constructor(
  typeSource: JSTypeSource,
  private val element: PsiNamedElement,
) : JSTypeBaseImpl(typeSource),
    JSCodeBasedType {

  constructor(element: PsiNamedElement) : this(JSTypeSourceFactory.createTypeSource(element, true), element)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueSourcePropType(source, element)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    (type is VueSourcePropType && type.element == element)

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType =
    computeType(element)
      ?.substitute(context)
      ?.fixPrimitiveTypes()
    ?: JSAnyType.get(source)

  private fun computeType(element: PsiNamedElement): JSType? {
    val jsDocType = getPropTypeFromDocComment(element)
    if (jsDocType != null) {
      return jsDocType
    }

    return when (element) {
      is JSProperty -> getPropTypeFromPropOptions(element.value)
      is JSImplicitElement -> getPropTypeFromGenericType(element.jsType)
                              ?: element.jsType?.let { JSApplyCallType(it, it.source) }
                              ?: getPropTypeFromPropOptions((element.parent as? JSProperty)?.value)
      else -> null
    }
  }

  override fun hashCodeImpl(): Int = element.name.hashCode()

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueSourcePropType: ").append(element.name ?: "")
      return
    }
    substitute().buildTypeText(format, builder)
  }

}