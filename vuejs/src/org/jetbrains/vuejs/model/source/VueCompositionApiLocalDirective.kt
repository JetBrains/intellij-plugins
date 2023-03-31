// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import java.util.*

class VueCompositionApiLocalDirective(name: String, override val rawSource: JSPsiNamedElementBase) : VueDirective {

  override val defaultName: String = name
  override val parents: List<VueEntitiesContainer> = emptyList()
  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueCompositionApiLocalDirective && other.defaultName == defaultName && other.source == source)

  override fun hashCode(): Int =
    Objects.hash(defaultName, source)

  override fun toString(): String {
    return "VueCompositionApiLocalDirective($defaultName)"
  }

  override val source: PsiElement
    get() = rawSource.resolveIfImportSpecifier()

  override fun createPointer(): Pointer<VueCompositionApiLocalDirective> {
    val name = defaultName
    val source = this.rawSource.createSmartPointer()
    return Pointer {
      val newSource = source.dereference() ?: return@Pointer null
      VueCompositionApiLocalDirective(name, newSource)
    }
  }

}
