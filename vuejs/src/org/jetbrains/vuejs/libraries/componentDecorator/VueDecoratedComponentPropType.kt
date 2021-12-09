// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.componentDecorator

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.types.*
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.codeInsight.getDecoratorArgument
import org.jetbrains.vuejs.codeInsight.getJSTypeFromPropOptions

class VueDecoratedComponentPropType private constructor(typeSource: JSTypeSource,
                                                        private val member: JSRecordType.PropertySignature,
                                                        private val decorator: ES6Decorator?,
                                                        private val decoratorArgumentIndex: Int)
  : JSSimpleTypeBaseImpl(typeSource), JSCodeBasedType {

  constructor(member: JSRecordType.PropertySignature, decorator: ES6Decorator?, decoratorArgumentIndex: Int)
    : this(JSTypeSourceFactory.createTypeSource(member.memberSource.singleElement!!, false),
           member, decorator, decoratorArgumentIndex)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueDecoratedComponentPropType(source, member, decorator, decoratorArgumentIndex)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    (type is VueDecoratedComponentPropType
     && type.member == member
     && type.decorator == decorator
     && type.decoratorArgumentIndex == decoratorArgumentIndex)

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType =
    getJSTypeFromPropOptions(getDecoratorArgument(decorator, decoratorArgumentIndex)) ?: member.jsType ?: JSAnyType.get(source)

  override fun hashCodeImpl(): Int = member.memberHashCode

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueDecoratedComponentPropType: ").append(member.memberName)
      return
    }
    substitute().buildTypeText(format, builder)
  }

}