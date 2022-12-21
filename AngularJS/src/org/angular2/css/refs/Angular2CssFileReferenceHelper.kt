// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.refs

import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.model.ModelBranch
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.util.SmartList
import com.intellij.webpack.WebpackCssFileReferenceHelper
import org.angular2.cli.config.AngularConfigProvider

class Angular2CssFileReferenceHelper : WebpackCssFileReferenceHelper() {
  override fun getContexts(project: Project, file: VirtualFile): Collection<PsiFileSystemItem> {
    val result = SmartList<PsiFileSystemItem>(AngularCliAwareCssFileReferenceResolver(project, file))
    AngularConfigProvider.getAngularProject(project, file)
      ?.stylePreprocessorIncludeDirs
      ?.mapNotNullTo(result) { dir -> PsiManager.getInstance(project).findDirectory(dir) }
    return result
  }

  private class AngularCliAwareCssFileReferenceResolver(project: Project, contextFile: VirtualFile)
    : WebpackTildeFileReferenceResolver(project, contextFile) {

    override fun obtainBranchCopy(branch: ModelBranch): AngularCliAwareCssFileReferenceResolver {
      val fileCopy = branch.findFileCopy(virtualFile)
      return AngularCliAwareCssFileReferenceResolver(project, fileCopy)
    }

    override fun findRootDirectories(context: VirtualFile, project: Project): Collection<VirtualFile> {
      val ngProject = AngularConfigProvider.getAngularProject(project, context)
      if (ngProject != null) {
        val tsConfig = TypeScriptConfigService.Provider.parseConfigFile(project, ngProject.tsConfigFile)
        if (tsConfig != null) {
          val baseUrl = tsConfig.baseUrl
          if (baseUrl != null) {
            return listOf(baseUrl)
          }
        }
        val cssResolveDir = ngProject.cssResolveRootDir
        if (cssResolveDir != null) {
          return listOf(cssResolveDir)
        }
      }
      return emptyList()
    }
  }
}
