// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.context

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.indexing.FileBasedIndexImpl
import com.intellij.webSymbols.context.WebSymbolsContext
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.sfc.AstroFileType

private const val KIND_ASTRO_PROJECT = "astro-project"
private const val CONTEXT_ASTRO = "astro"

fun isAstroProject(context: PsiElement): Boolean =
  WebSymbolsContext.get(KIND_ASTRO_PROJECT, context) == CONTEXT_ASTRO

fun isAstroProject(contextFile: VirtualFile, project: Project): Boolean =
  WebSymbolsContext.get(KIND_ASTRO_PROJECT, contextFile, project) == CONTEXT_ASTRO

fun isAstroFrameworkContext(context: PsiElement): Boolean =
  WebSymbolsContext.get(WebSymbolsContext.KIND_FRAMEWORK, context) == AstroFramework.ID

fun isAstroFrameworkContext(contextFile: VirtualFile, project: Project): Boolean =
  WebSymbolsContext.get(WebSymbolsContext.KIND_FRAMEWORK, contextFile, project) == AstroFramework.ID

fun hasAstroFiles(project: Project): Boolean =
  CachedValuesManager.getManager(project).getCachedValue(project) {
    CachedValueProvider.Result.create(
      FileBasedIndexImpl.disableUpToDateCheckIn<Boolean, Exception> {
        FileTypeIndex.containsFileOfType(AstroFileType.INSTANCE, GlobalSearchScope.projectScope(project))
      },
      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
      DumbService.getInstance(project)
    )
  }
