// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.types.JSCodeBasedType
import com.intellij.lang.javascript.psi.types.JSSimpleTypeBaseImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.model.VueInstanceOwner
import org.jetbrains.vuejs.model.VueNamedEntity
import org.jetbrains.vuejs.model.source.INSTANCE_PROPS_PROP

class VuePropsType(source: JSTypeSource,
                   private val instanceOwner: VueInstanceOwner)
  : JSSimpleTypeBaseImpl(source), JSCodeBasedType, VueCompleteType {

  constructor(instanceOwner: VueInstanceOwner) : this(createStrictTypeSource(instanceOwner.source), instanceOwner)

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VuePropsType(source, instanceOwner)

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    type is VuePropsType && type.instanceOwner == instanceOwner

  override fun hashCodeImpl(): Int = instanceOwner.hashCode()

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VuePropsType: ")
        .append(instanceOwner.javaClass.simpleName)
      if (instanceOwner is VueNamedEntity) {
        builder.append("(").append(instanceOwner.defaultName).append(")")
      }
      return
    }
    substitute().buildTypeText(format, builder)
  }

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType? =
    instanceOwner.thisType.asRecordType()
      .findPropertySignature(INSTANCE_PROPS_PROP)
      ?.jsType

}