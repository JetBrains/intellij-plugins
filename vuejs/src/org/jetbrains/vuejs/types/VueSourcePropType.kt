// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.documentation.JSDocumentationUtils
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.TypeScriptMergedTypeImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.psi.PsiNamedElement
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.codeInsight.fixPrimitiveTypes
import org.jetbrains.vuejs.codeInsight.getJSTypeFromPropOptions
import org.jetbrains.vuejs.codeInsight.getPropTypeFromGenericType

class VueSourcePropType private constructor(typeSource: JSTypeSource, private val element: PsiNamedElement)
  : JSSimpleTypeBaseImpl(typeSource), JSCodeBasedType {

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
    val jsDocType = JSDocumentationUtils.findType(element as? JSNamedElement)
      ?.let { JSTypeParser.createTypeFromJSDoc(element.getProject(), it, JSTypeSourceFactory.createTypeSource(element, true)) }
      ?.let { getPropTypeFromGenericType(it) ?: it }

    if (jsDocType != null) {
      return jsDocType
    }

    return when (element) {
      is JSProperty -> getJSTypeFromPropOptions(element.value)
      is TypeScriptMergedTypeImplicitElement ->
        getPropTypeFromGenericType(element.jsType) ?: getJSTypeFromPropOptions((element.explicitElement as? JSProperty)?.value)
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