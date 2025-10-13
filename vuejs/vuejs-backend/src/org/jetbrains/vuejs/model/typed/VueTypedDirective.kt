// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.model.Pointer
import com.intellij.openapi.project.DumbService
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.typed.VueTypedDirectives.getDirectiveModifiers

class VueTypedDirective(
  override val source: TypeScriptPropertySignature,
  override val defaultName: String,
) : VueTypedContainer(source),
    VueDirective {

  override val jsType: JSType?
    get() = source.jsType

  override val thisType: JSType
    get() = JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS)

  override val modifiers: List<VueDirectiveModifier>
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(
        getDirectiveModifiers(source, VueMode.CLASSIC),
        DumbService.getInstance(source.project).modificationTracker,
        PsiModificationTracker.MODIFICATION_COUNT,
      )
    }

  override fun createPointer(): Pointer<VueTypedDirective> {
    val sourcePtr = source.createSmartPointer()
    val defaultName = this.defaultName
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueTypedDirective(source, defaultName)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueTypedDirective
    && other.source == this.source

  override fun hashCode(): Int =
    source.hashCode()
}
