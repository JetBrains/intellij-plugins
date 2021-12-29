// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import java.util.Objects

class VueSourceDirective(name: String, override val source: PsiElement) : VueDirective {

  override val defaultName: String = name
  override val parents: List<VueEntitiesContainer> = emptyList()

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueSourceDirective && other.defaultName == defaultName && other.source == source)

  override fun hashCode(): Int =
    Objects.hash(defaultName, source)

  override fun createPointer(): Pointer<VueDirective> {
    val name = defaultName
    val source = this.source.createSmartPointer()
    return Pointer {
      val newSource = source.dereference() ?: return@Pointer null
      VueSourceDirective(name, newSource)
    }
  }

}
