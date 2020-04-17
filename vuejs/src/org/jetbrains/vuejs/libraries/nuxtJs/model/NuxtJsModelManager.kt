// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxtJs.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.libraries.nuxtJs.model.impl.NuxtJsApplicationImpl

object NuxtJsModelManager {

  private const val NUXT_JS_CONFIG: String = "nuxt.config.js"

  fun getApplication(context: PsiElement): NuxtJsApplication? =
    context.containingFile?.originalFile?.virtualFile?.let {
      findParentEntry(it, getNuxtJsApplicationMap(context.project))
    }

  private fun getNuxtJsApplicationMap(project: Project): Map<VirtualFile, NuxtJsApplication> =
    CachedValuesManager.getManager(project).getCachedValue(project) {
      create<Map<VirtualFile, NuxtJsApplication>>(
        FilenameIndex.getVirtualFilesByName(project, NUXT_JS_CONFIG, GlobalSearchScope.projectScope(project))
          .associateBy({ it.parent }, { NuxtJsApplicationImpl(it, project) }),
        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }

  private fun findParentEntry(file: VirtualFile, nuxtJsApplicationMap: Map<VirtualFile, NuxtJsApplication>): NuxtJsApplication? {
    var tmp: VirtualFile? = file
    while (tmp != null) {
      nuxtJsApplicationMap[tmp]?.let { return it }
      tmp = tmp.parent
    }
    return null
  }

}