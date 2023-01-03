// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.util.ProcessingContext
import org.angular2.lang.expr.psi.Angular2TemplateBindings

class Angular2TemplateBindingType : Angular2BaseType<Angular2TemplateBindings> {
  private val myKey: String

  constructor(attribute: Angular2TemplateBindings, key: String) : super(attribute, Angular2TemplateBindings::class.java) {
    myKey = key
  }

  private constructor(source: JSTypeSource, key: String) : super(source, Angular2TemplateBindings::class.java) {
    myKey = key
  }

  override val typeOfText: String
    get() = "*" + sourceElement.templateName + "#" + myKey

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return Angular2TemplateBindingType(source, myKey)
  }

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean {
    return super.isEquivalentToWithSameClass(type, context, allowResolve) && myKey == (type as Angular2TemplateBindingType).myKey
  }

  override fun hashCodeImpl(): Int {
    return super.hashCodeImpl() * 31 + myKey.hashCode()
  }

  override fun resolveType(context: JSTypeSubstitutionContext): JSType? {
    return BindingsTypeResolver.get(sourceElement).resolveDirectiveInputType(myKey)
  }
}