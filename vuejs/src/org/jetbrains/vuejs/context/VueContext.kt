// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

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
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.lang.html.VueFileType


fun isVueContext(context: PsiElement): Boolean = VueFramework.instance.isContext(context)

fun isVueContext(contextFile: VirtualFile, project: Project): Boolean = VueFramework.instance.isContext(contextFile, project)

fun hasVueFiles(project: Project): Boolean =
  CachedValuesManager.getManager(project).getCachedValue(project) {
    CachedValueProvider.Result.create(
      FileBasedIndexImpl.disableUpToDateCheckIn<Boolean, Exception> {
        FileTypeIndex.containsFileOfType(VueFileType.INSTANCE, GlobalSearchScope.projectScope(project))
      },
      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
      DumbService.getInstance(project)
    )
  }
