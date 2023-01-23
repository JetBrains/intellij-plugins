// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.lang.html.HtmlCompatibleFile
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceHelper
import org.angular2.cli.config.AngularConfigProvider
import org.angular2.lang.Angular2LangUtil

class AngularCliFileReferenceHelper : FileReferenceHelper() {

  override fun isMine(project: Project, file: VirtualFile): Boolean {
    val psiFile = getPsiFileSystemItem(project, file)
    return ((psiFile is HtmlCompatibleFile || psiFile is StylesheetFile)
            && Angular2LangUtil.isAngular2Context(project, file)
            && AngularConfigProvider.getAngularProject(project, file) != null)
  }

  override fun getContexts(project: Project, file: VirtualFile): Collection<PsiFileSystemItem> {
    return AngularConfigProvider.getAngularProject(project, file)
             ?.sourceDir
             ?.let { PsiManager.getInstance(project).findDirectory(it) }
             ?.let { listOf(it) }
           ?: emptyList()
  }

  override fun getRoots(module: Module, hostFile: VirtualFile): Collection<PsiFileSystemItem> {
    return getContexts(module.project, hostFile)
  }
}
