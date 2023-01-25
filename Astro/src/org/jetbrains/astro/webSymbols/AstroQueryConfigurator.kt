// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.webSymbols.scope.AstroAvailableComponentsScope
import org.jetbrains.astro.webSymbols.scope.AstroFrontmatterScope

class AstroQueryConfigurator : WebSymbolsQueryConfigurator {

  companion object {

    const val PROP_ASTRO_PROXIMITY = "x-astro-proximity"

  }

  override fun getScope(project: Project,
                        element: PsiElement?,
                        context: WebSymbolsContext,
                        allowResolve: Boolean): List<WebSymbolsScope> =
    if (context.framework == AstroFramework.ID
        && element?.containingFile is AstroFileImpl) {
      listOf(AstroFrontmatterScope(element.containingFile as AstroFileImpl), AstroAvailableComponentsScope(project))
    }
    else emptyList()
}