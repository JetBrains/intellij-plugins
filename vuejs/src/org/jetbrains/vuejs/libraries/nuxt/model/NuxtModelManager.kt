// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.libraries.nuxt.NUXT_CONFIG_FILE
import org.jetbrains.vuejs.libraries.nuxt.NUXT_CONFIG_FILE_TS
import org.jetbrains.vuejs.libraries.nuxt.NUXT_PKG
import org.jetbrains.vuejs.libraries.nuxt.model.impl.NuxtApplicationImpl

object NuxtModelManager {

  fun getApplication(project: Project, context: VirtualFile): NuxtApplication? =
    findParentEntry(context, getNuxtApplicationMap(project))

  fun getApplication(context: PsiElement): NuxtApplication? =
    context.containingFile?.originalFile?.virtualFile?.let {
      getApplication(context.project, it)
    }

  fun getApplication(context: GlobalSearchScope): NuxtApplication? =
    context.project
      ?.let { getNuxtApplicationMap(it) }
      ?.asSequence()
      ?.filter { context.contains(it.key) }
      ?.map { it.value }
      ?.singleOrNull()

  private fun getNuxtApplicationMap(project: Project): Map<VirtualFile, NuxtApplication> =
    CachedValuesManager.getManager(project).getCachedValue(project) {
      FilenameIndex.getVirtualFilesByName(NUXT_CONFIG_FILE, GlobalSearchScope.projectScope(project)).asSequence()
        .plus(FilenameIndex.getVirtualFilesByName(NUXT_CONFIG_FILE_TS, GlobalSearchScope.projectScope(project)))
        .associateBy({ it.parent }, { NuxtApplicationImpl(it, project) })
        .takeIf { it.isNotEmpty() }
        ?.let {
          create(it, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
        }
      ?: PackageJsonFileManager.getInstance(project).let { pkgJsonManager ->
        pkgJsonManager.validPackageJsonFiles.asSequence()
          .map { PackageJsonData.getOrCreate(it) }
          .filter { it.containsOneOfDependencyOfAnyType(NUXT_PKG) }
          .associateBy({ it.packageJsonFile.parent }, { NuxtApplicationImpl(it.packageJsonFile, project) })
          .let {
            create(it, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, pkgJsonManager.modificationTracker)
          }
      }
    }

  private fun findParentEntry(file: VirtualFile, nuxtApplicationMap: Map<VirtualFile, NuxtApplication>): NuxtApplication? {
    var tmp: VirtualFile? = file
    while (tmp != null) {
      nuxtApplicationMap[tmp]?.let { return it }
      tmp = tmp.parent
    }
    return null
  }

}