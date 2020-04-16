// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxtJs.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.libraries.nuxtJs.model.impl.NuxtJsApplicationImpl
import java.util.*

object NuxtJsModelManager {

  private const val NUXT_JS_CONFIG: String = "nuxt.config.js"

  fun getApplication(context: PsiElement): NuxtJsApplication? {
    val filePath = context.containingFile?.originalFile?.virtualFile?.path ?: return null
    val applicationEntry = getNuxtJsApplicationMap(context.project).floorEntry(filePath) ?: return null
    if (FileUtil.startsWith(filePath, applicationEntry.key)) {
      return applicationEntry.value
    }
    return null
  }

  private fun getNuxtJsApplicationMap(project: Project): NavigableMap<String, NuxtJsApplication> =
    CachedValuesManager.getManager(project).getCachedValue(project) {
      create<NavigableMap<String, NuxtJsApplication>>(
        FilenameIndex.getVirtualFilesByName(project, NUXT_JS_CONFIG, GlobalSearchScope.projectScope(project))
          .associateByTo(TreeMap(), { it.parent.path }, { NuxtJsApplicationImpl(it, project) }),
        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }

}