// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.source.VueSourceGlobal
import java.util.*

/**
 * A wrapper around a regular global container, which provides only components.
 */
class VueModule(
  override val delegate: VueGlobal,
  override val source: PsiElement,
) : VueDelegatedEntitiesContainer<VueGlobal>(),
    VueGlobal {

  override val components: Map<String, VueComponent>
    get() = delegate.components

  override val apps: List<VueApp>
    get() = emptyList()

  override val libraries: List<VueLibrary>
    get() = emptyList()

  override val unregistered: VueEntitiesContainer
    get() = delegate.unregistered

  override val project: Project
    get() = delegate.project

  override val packageJsonUrl: String?
    get() = delegate.packageJsonUrl

  override val parents: List<VueEntitiesContainer>
    get() = emptyList()

  override fun getParents(scopeElement: VueScopeElement): List<VueEntitiesContainer> =
    emptyList()

  override fun createPointer(): Pointer<out VueGlobal> {
    val delegatePtr = delegate.createPointer()
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueModule(delegate, source)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueModule
    && other.delegate == delegate
    && other.source == source

  override fun hashCode(): Int =
    Objects.hash(delegate, source)

  companion object {
    fun get(context: PsiElement): VueModule? =
      VueGlobalImpl.get(context).takeUnless { it is VueSourceGlobal }?.let { VueModule(it, context) }
  }
}