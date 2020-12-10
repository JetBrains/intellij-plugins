// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model

import com.intellij.lang.javascript.buildTools.webpack.WebPackConfigManager
import com.intellij.lang.javascript.buildTools.webpack.WebPackReferenceContributor
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.findDefaultExport
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider

class NuxtComponentProvider : VueContainerInfoProvider {

  override fun getComponents(scope: GlobalSearchScope, local: Boolean): Map<String, VueComponent> =
    NuxtModelManager.getApplication(scope)
      ?.config?.takeIf { it.file != null }
      ?.let { config ->
        val visitedFiles = mutableSetOf<VirtualFile>()
        config.componentsDirs.asSequence()
          .mapNotNull { componentDir -> resolvePath(config.file!!, componentDir)?.let { Pair(it, componentDir) } }
          .sortedBy { dir -> -dir.first.path.count { it == '\\' || it == '/' } }
          .flatMap { pair ->
            findComponents(config.file!!.project, scope, visitedFiles, pair.first, pair.second)
          }
          .distinctBy { it.first }
          .toMap()
      }
    ?: emptyMap()

  private fun resolvePath(configFile: PsiFile, componentDir: NuxtConfig.ComponentsDirectory): VirtualFile? =
    if (componentDir.path.startsWith("~") || componentDir.path.startsWith("@")) {
      WebPackConfigManager.getInstance(configFile.project)
        .resolveConfig(configFile)
        .takeIf { !it.isEmpty() }
        ?.let { webpackReferenceProvider.getAliasedReferences(componentDir.path, configFile, 0, null, it, true) }
        ?.lastOrNull()
        ?.resolve()
        ?.castSafelyTo<PsiDirectory>()
        ?.virtualFile
    }
    else {
      configFile.virtualFile.parent?.findFileByRelativePath(componentDir.path)
    }
      ?.takeIf { it.isDirectory && it.name != JSLibraryUtil.NODE_MODULES }

  private fun findComponents(project: Project,
                             scope: GlobalSearchScope,
                             visitedFiles: MutableSet<VirtualFile>,
                             componentDirFile: VirtualFile,
                             componentDir: NuxtConfig.ComponentsDirectory): List<Pair<String, VueComponent>> {
    val extensions = componentDir.extensions
    val prefix = fromAsset(componentDir.prefix)
    val psiManager = PsiManager.getInstance(project)
    val result = mutableListOf<Pair<String, VueComponent>>()
    VfsUtil.visitChildrenRecursively(componentDirFile, object : VirtualFileVisitor<Void>() {
      override fun visitFile(file: VirtualFile): Boolean {
        if (!file.isDirectory
            && file.extension.let { extensions.contains(it) }
            && visitedFiles.add(file)
            && scope.contains(file)) {
          val name = fromAsset(file.nameWithoutExtension).let {
            when {
              prefix.isEmpty() -> it
              it.startsWith(prefix) -> it
              else -> "$prefix-$it"
            }
          }
          psiManager.findFile(file)
            ?.let { if (it is JSFile) findDefaultExport(it) else it }
            ?.let { VueModelManager.getComponent(it) }
            ?.let {
              result.add(Pair(name, it))
              result.add(Pair("lazy-$name", it))
            }
        }
        return !file.isDirectory || visitedFiles.add(file)
      }
    })
    return result
  }

  companion object {
    val webpackReferenceProvider = WebPackReferenceContributor()
  }

}