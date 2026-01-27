// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.model.impl

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueDelegatedComponent
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueNamedComponent

class NuxtGlobalComponent(
  override val name: String,
  delegate: VueComponent,
  vueProximity: VueModelVisitor.Proximity? = null,
) : VueDelegatedComponent(delegate, vueProximity) {

  override fun withVueProximity(proximity: VueModelVisitor.Proximity): VueNamedComponent =
    NuxtGlobalComponent(name, delegate, proximity)

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    delegate.getNavigationTargets(project)

  override val elementToImport: PsiElement?
    get() = delegate.elementToImport

  override fun createPointer(): Pointer<NuxtGlobalComponent> {
    val name = this.name
    val delegatePtr = delegate.createPointer()
    val proximity = this.vueProximity
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      NuxtGlobalComponent(name, delegate, proximity)
    }
  }
}
