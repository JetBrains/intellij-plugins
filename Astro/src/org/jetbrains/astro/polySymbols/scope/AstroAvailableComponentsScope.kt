// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.model.Pointer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndexEx
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileType
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.polySymbols.symbols.AstroComponent

internal class AstroAvailableComponentsScope(project: Project) : PolySymbolScopeWithCache<Project, Unit>(AstroFramework.ID, project, project, Unit) {

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == ASTRO_COMPONENTS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val psiManager = PsiManager.getInstance(project)
    val files = if (ApplicationManager.getApplication().isUnitTestMode)
      FileTypeIndex.getFiles(AstroFileType, GlobalSearchScope.projectScope(project))
    else
      FileBasedIndexEx.disableUpToDateCheckIn<Collection<VirtualFile>, Exception> {
        FileTypeIndex.getFiles(AstroFileType, GlobalSearchScope.projectScope(project))
      }
    files.forEach { vf ->
      psiManager.findFile(vf)?.let { consumer(AstroComponent(it)) }
    }
    cacheDependencies.add(VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    cacheDependencies.add(DumbService.getInstance(project))
  }

  override fun getModificationCount(): Long {
    return VirtualFileManager.getInstance().structureModificationCount +
           DumbService.getInstance(project).modificationTracker.modificationCount
  }

  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<Project, Unit>> =
    Pointer.hardPointer(this)

}