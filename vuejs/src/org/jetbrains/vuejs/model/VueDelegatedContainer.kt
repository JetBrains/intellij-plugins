// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.psi.PsiElement

abstract class VueDelegatedContainer<T : VueContainer> : VueDelegatedEntitiesContainer<T>(), VueContainer {

  override val data: List<VueDataProperty>
    get() = delegate?.data ?: emptyList()

  override val computed: List<VueComputedProperty>
    get() = delegate?.computed ?: emptyList()

  override val methods: List<VueMethod>
    get() = delegate?.methods ?: emptyList()

  override val props: List<VueInputProperty>
    get() = delegate?.props ?: emptyList()

  override val emits: List<VueEmitCall>
    get() = delegate?.emits ?: emptyList()

  override val modelDecls: List<VueModelDecl>
    get() = delegate?.modelDecls ?: emptyList()

  override val slots: List<VueSlot>
    get() = delegate?.slots ?: emptyList()

  override val extends: List<VueContainer>
    get() = delegate?.extends ?: emptyList()

  override val model: VueModelDirectiveProperties
    get() = delegate?.model ?: VueModelDirectiveProperties()

  override val source: PsiElement?
    get() = delegate?.source

  override val rawSource: PsiElement?
    get() = delegate?.rawSource

  override val element: String?
    get() = delegate?.element

  override val template: VueTemplate<*>?
    get() = delegate?.template

  override val delimiters: Pair<String, String>?
    get() = delegate?.delimiters

  override val global: VueGlobal?
    get() = delegate?.global

  override val parents: List<VueEntitiesContainer>
    get() = delegate?.parents ?: emptyList()

  companion object {
    fun unwrap(container: Any): Any =
      (container as? VueDelegatedContainer<*>)?.delegate ?: container
  }
}