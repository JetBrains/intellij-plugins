// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.VueFramework
import org.jetbrains.vuejs.lang.html.VueFileType


fun isVueContext(context: PsiElement): Boolean = VueFramework.instance.isContext(context)

fun isVueContext(contextFile: VirtualFile, project: Project): Boolean = VueFramework.instance.isContext(contextFile, project)

fun hasVueFiles(project: Project): Boolean =
  CachedValuesManager.getManager(project).getCachedValue(project) {
    CachedValueProvider.Result.create(
      FileTypeIndex.containsFileOfType(VueFileType.INSTANCE, GlobalSearchScope.projectScope(project)),
      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
    )
  }

@Suppress("DEPRECATION")
fun enableVueTSService(project: Project): Boolean =
  !DumbService.isDumb(project) && (
    CachedValuesManager.getManager(project).getCachedValue(project) {
      val result = FileTypeIndex.containsFileOfType(VueFileType.INSTANCE, GlobalSearchScope.projectScope(project))
      CachedValueProvider.Result.create(result,
                                        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                                        ProjectRootModificationTracker.getInstance(project))

    }
    || project.baseDir
      ?.let { PsiManager.getInstance(project).findDirectory(it) }
      ?.let { VueFramework.instance.isContext(it) } == true)
