// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.model.Pointer
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.typed.VueTypedDirectiveModifier
import org.jetbrains.vuejs.model.typed.VueTypedDirectives.getDirectiveModifiers
import java.util.*

class VueSourceDirective(
  name: String,
  override val source: PsiElement,
  private val typeSource: PsiElement? = null,
) : VueDirective {

  override val defaultName: String = name
  override val parents: List<VueEntitiesContainer> = emptyList()

  override val modifiers: List<VueTypedDirectiveModifier>
    get() {
      typeSource ?: return emptyList()

      return CachedValuesManager.getCachedValue(typeSource) {
        CachedValueProvider.Result.create(
          getDirectiveModifiers(typeSource),
          DumbService.getInstance(typeSource.project).modificationTracker,
          PsiModificationTracker.MODIFICATION_COUNT,
        )
      }
    }

  override fun createPointer(): Pointer<out VueSourceDirective> {
    val name = defaultName
    val source = this.source.createSmartPointer()
    val typeSource = this.typeSource?.createSmartPointer()
    return Pointer {
      val newSource = source.dereference() ?: return@Pointer null
      val newTypeSource = typeSource?.dereference()
      VueSourceDirective(name, newSource, newTypeSource)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueSourceDirective && other.defaultName == defaultName && other.source == source)

  override fun hashCode(): Int =
    Objects.hash(defaultName, source)

  override fun toString(): String {
    return "VueSourceDirective($defaultName)"
  }
}
