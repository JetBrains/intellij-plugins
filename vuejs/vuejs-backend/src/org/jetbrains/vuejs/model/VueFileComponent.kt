// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget

interface VueFileComponent : VueNamedComponent, PsiSourcedPolySymbol {

  override val source: PsiFile

  override fun createPointer(): Pointer<out VueFileComponent>

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    listOf(VueComponentSourceNavigationTarget(source))

}