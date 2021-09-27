// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.JSTypeWithIncompleteSubstitution
import com.intellij.lang.javascript.psi.types.*
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.model.VueInstanceOwner
import org.jetbrains.vuejs.model.VueNamedEntity
import java.util.*

class VueComponentInstanceType(source: JSTypeSource,
                               private val instanceOwner: VueInstanceOwner,
                               typeMembers: List<PropertySignature>)
  : JSSimpleTypeBaseImpl(source), JSCodeBasedType, JSTypeWithIncompleteSubstitution {

  private val typeMembers = typeMembers.toList()
  private val membersNames = typeMembers.map { it.memberName }

  override fun copyWithNewSource(source: JSTypeSource): JSType = VueComponentInstanceType(source, instanceOwner, typeMembers)

  override fun acceptChildren(visitor: JSRecursiveTypeVisitor) {
    typeMembers.forEach { it.acceptChildren(visitor) }
  }

  override fun hashCodeImpl(): Int = Objects.hashCode(membersNames.toTypedArray()) * 31 + instanceOwner.hashCode()

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean {
    return (type is VueComponentInstanceType
            && type.instanceOwner == instanceOwner
            && type.membersNames == membersNames)
  }

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueComponentInstanceType: ")
        .append(instanceOwner.javaClass.simpleName)
      if (instanceOwner is VueNamedEntity) {
        builder.append("(").append(instanceOwner.defaultName).append(")")
      }
      builder.append(" [")
      membersNames.forEach { builder.append(it).append(",") }
      builder.append("]")
      return
    }
    substitute().buildTypeText(format, builder)
  }

  override fun substituteCompletely(): JSType = JSSimpleRecordTypeImpl(source, typeMembers)
}
