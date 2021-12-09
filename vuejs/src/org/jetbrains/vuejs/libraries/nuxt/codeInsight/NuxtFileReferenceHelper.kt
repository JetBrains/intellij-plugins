// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.codeInsight


import com.intellij.lang.html.HtmlCompatibleFile
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceHelper
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtModelManager

class NuxtFileReferenceHelper : FileReferenceHelper() {

  override fun isMine(project: Project, file: VirtualFile): Boolean {
    return getPsiFileSystemItem(project, file)?.let {
      it is HtmlCompatibleFile
      && isVueContext(it)
      && NuxtModelManager.getApplication(it) != null
    } == true
  }

  override fun getContexts(project: Project, file: VirtualFile): Collection<PsiFileSystemItem> = emptyList()

  override fun getRoots(module: Module, file: VirtualFile): Collection<PsiFileSystemItem> =
    getPsiFileSystemItem(module.project, file)
      ?.let { NuxtModelManager.getApplication(it) }
      ?.staticResourcesDir
      ?.let { listOf(it) }
    ?: emptyList()
}
