// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.types

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.*
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.AstLoadingFilter
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression

class VueSourceSlotBindingType private constructor(typeSource: JSTypeSource,
                                                   private val attribute: XmlAttribute,
                                                   private val bindingName: String)
  : JSSimpleTypeBaseImpl(typeSource), JSCodeBasedType {

  constructor(attribute: XmlAttribute, bindingName: String) :
    this(JSTypeSourceFactory.createTypeSource(attribute, true), attribute, bindingName)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueSourceSlotBindingType(source, attribute, bindingName)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    (type is VueSourceSlotBindingType && type.attribute == attribute)

  override fun hashCodeImpl(): Int = bindingName.hashCode()

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueSourceSlotBindingType: ").append(bindingName)
      return
    }
    substitute().buildTypeText(format, builder)
  }

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType =
    AstLoadingFilter.forceAllowTreeLoading<JSType, Throwable>(attribute.containingFile) {
      attribute.valueElement
        ?.childrenOfType<ASTWrapperPsiElement>()
        ?.firstOrNull()
        ?.firstChild?.asSafely<VueJSEmbeddedExpression>()
        ?.firstChild
        ?.let { JSResolveUtil.getElementJSType(it) }
      ?: JSAnyType.get(attribute, false)
    }
}