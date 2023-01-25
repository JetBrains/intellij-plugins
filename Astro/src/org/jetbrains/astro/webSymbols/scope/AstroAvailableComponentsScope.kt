// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.scope

import com.intellij.model.Pointer
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndexImpl
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileType
import org.jetbrains.astro.webSymbols.symbols.AstroComponent

class AstroAvailableComponentsScope(project: Project) : WebSymbolsScopeWithCache<Project, Unit>(AstroFramework.ID, project, project, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val psiManager = PsiManager.getInstance(project)
    FileBasedIndexImpl.disableUpToDateCheckIn<Collection<VirtualFile>, Exception> {
      FileTypeIndex.getFiles(AstroFileType.INSTANCE, GlobalSearchScope.projectScope(project))
    }
      .asSequence()
      .mapNotNull { psiManager.findFile(it) }
      .forEach {
        consumer(AstroComponent(it))
      }
    cacheDependencies.add(VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    cacheDependencies.add(DumbService.getInstance(project))
  }

  override fun getModificationCount(): Long {
    return VirtualFileManager.getInstance().structureModificationCount +
           DumbService.getInstance(project).modificationTracker.modificationCount
  }

  override fun createPointer(): Pointer<out WebSymbolsScopeWithCache<Project, Unit>> =
    Pointer.hardPointer(this)

}