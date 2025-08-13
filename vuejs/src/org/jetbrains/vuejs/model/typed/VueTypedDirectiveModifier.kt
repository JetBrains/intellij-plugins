// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueDirectiveModifier

data class VueTypedDirectiveModifier(
  override val source: TypeScriptPropertySignature,
) : VueDirectiveModifier {
  override val name: String
    get() = source.memberName

  fun createPointer(): Pointer<out VueTypedDirectiveModifier> {
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueTypedDirectiveModifier(source)
    }
  }
}
