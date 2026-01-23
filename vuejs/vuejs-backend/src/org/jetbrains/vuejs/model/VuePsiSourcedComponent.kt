// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget

interface VuePsiSourcedComponent : VueComponent, PsiSourcedPolySymbol {

  override val source: PsiElement

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    listOf(VueComponentSourceNavigationTarget(source))

  override fun createPointer(): Pointer<out VuePsiSourcedComponent>
}