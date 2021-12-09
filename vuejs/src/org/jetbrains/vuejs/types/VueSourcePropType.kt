// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.codeInsight.getJSTypeFromPropOptions

class VueSourcePropType private constructor(typeSource: JSTypeSource, private val property: JSProperty)
  : JSSimpleTypeBaseImpl(typeSource), JSCodeBasedType {

  constructor(property: JSProperty) : this(JSTypeSourceFactory.createTypeSource(property, true), property)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueSourcePropType(source, property)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    (type is VueSourcePropType && type.property == property)

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType =
    getJSTypeFromPropOptions(property.value)
      ?.substitute(context)
      ?.let {
        if (it is JSPrimitiveType && !it.isPrimitive)
          JSNamedTypeFactory.createType(it.primitiveTypeText, it.source, it.typeContext)
        else
          it
      }
    ?: JSAnyType.get(source)

  override fun hashCodeImpl(): Int = property.name.hashCode()

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueSourcePropType: ").append(property.name ?: "")
      return
    }
    substitute().buildTypeText(format, builder)
  }

}