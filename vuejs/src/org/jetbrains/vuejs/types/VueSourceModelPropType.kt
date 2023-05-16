// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.types.*
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.codeInsight.fixPrimitiveTypes
import org.jetbrains.vuejs.codeInsight.getJSTypeFromPropOptions

class VueSourceModelPropType private constructor(
  typeSource: JSTypeSource,
  private val name: String,
  private val options: JSObjectLiteralExpression,
) : JSSimpleTypeBaseImpl(typeSource), JSCodeBasedType {

  constructor(name: String, options: JSObjectLiteralExpression) :
    this(JSTypeSourceFactory.createTypeSource(options, true), name, options)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueSourceModelPropType(source, name, options)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    (type is VueSourceModelPropType && type.name == name && type.options == options)

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType =
    getJSTypeFromPropOptions(options)
      ?.substitute(context)
      ?.fixPrimitiveTypes()
    ?: JSAnyType.get(source)

  override fun hashCodeImpl(): Int = name.hashCode()

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueSourceModelPropType: ").append(name)
      return
    }
    substitute().buildTypeText(format, builder)
  }

}