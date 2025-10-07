// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSRecordMemberSourceFactory
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyNewType
import com.intellij.lang.javascript.psi.types.recordImpl.PropertySignatureImpl
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.VueMixin
import java.util.*

class VueTypedMixin(source: JSExpression) : VueTypedContainer(source), VueMixin {

  override val thisType: JSType
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(
        JSResolveUtil.getExpressionJSType(source as JSExpression)
          ?.let { JSApplyNewType(it, it.source) }
          ?.substitute()
          ?.asRecordType()
          ?.remapJSProperties(),
        PsiModificationTracker.MODIFICATION_COUNT)
    } ?: JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS)

  override fun createPointer(): Pointer<VueTypedMixin> {
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.asSafely<JSExpression>()?.let { VueTypedMixin(it) }
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueTypedMixin
    && other.source == this.source

  override fun hashCode(): Int =
    Objects.hash(source)
}

private fun JSRecordType.remapJSProperties(): JSType? =
  JSRecordTypeImpl(
    source,
    typeMembers.map {
      // When we have a mixin coming from source definition, we need to remap JSProperties to VueImplicitElement
      // to not lose the calculated JSType.
      if (it is JSRecordType.PropertySignature && it.memberSource.singleElement is JSProperty) {
        PropertySignatureImpl(it.memberName, it.isPrivateName, it.privateNameDepth, it.isConst, it.jsType,
                              it.keyType, it.setterJSType, it.isOptional, it.isNumericKey,
                              JSRecordMemberSourceFactory.createSource(VueImplicitElement(it.memberName, it.jsType, it.memberSource.singleElement!!,
                                                                                          JSImplicitElement.Type.Property, true)))
      }
      else it
    }
  )

