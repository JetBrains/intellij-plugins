// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.google.common.base.Objects
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.JSTypeWithIncompleteSubstitution
import com.intellij.lang.javascript.psi.types.JSCodeBasedType
import com.intellij.lang.javascript.psi.types.JSSimpleRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSSimpleTypeBaseImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.model.VueInstanceOwner

class VueComponentInstanceType(source: JSTypeSource,
                               private val instanceOwner: VueInstanceOwner,
                               typeMembers: List<PropertySignature>)
  : JSSimpleTypeBaseImpl(source), JSCodeBasedType, JSTypeWithIncompleteSubstitution {

  private val typeMembers = typeMembers.toList()
  private val membersNames = typeMembers.map { it.memberName }
  private val hashCode by lazy { Objects.hashCode(membersNames.toTypedArray()) }

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return VueComponentInstanceType(source, instanceOwner, typeMembers)
  }

  override fun resolvedHashCodeImpl(): Int = hashCode

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    type is VueComponentInstanceType
    && type.instanceOwner == instanceOwner
    && membersNames == membersNames

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#$javaClass")
      return
    }
    substitute().buildTypeText(format, builder)
  }

  override fun substituteCompletely(): JSType = JSSimpleRecordTypeImpl(source, typeMembers)
}
