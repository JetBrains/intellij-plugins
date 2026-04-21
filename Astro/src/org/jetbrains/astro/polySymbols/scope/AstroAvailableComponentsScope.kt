// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.model.Pointer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.query.polySymbolScopeCached
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndexEx
import org.jetbrains.astro.lang.AstroFileType
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.polySymbols.symbols.AstroComponent


internal fun astroAvailableComponentsScope(project: Project): PolySymbolScope =
  polySymbolScopeCached(project) {
    provides(ASTRO_COMPONENTS)
    initialize {
      val psiManager = PsiManager.getInstance(project)
      val files = if (ApplicationManager.getApplication().isUnitTestMode)
        FileTypeIndex.getFiles(AstroFileType, GlobalSearchScope.projectScope(project))
      else
        FileBasedIndexEx.disableUpToDateCheckIn<Collection<VirtualFile>, Exception> {
          FileTypeIndex.getFiles(AstroFileType, GlobalSearchScope.projectScope(project))
        }
      files.forEach { vf ->
        psiManager.findFile(vf)?.let { add(AstroComponent(it)) }
      }
      cacheDependencies(
        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
        DumbService.getInstance(project)
      )
    }
  }