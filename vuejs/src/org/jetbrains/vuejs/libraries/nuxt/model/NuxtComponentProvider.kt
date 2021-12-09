// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model

import com.intellij.lang.javascript.buildTools.webpack.WebPackConfigManager
import com.intellij.lang.javascript.buildTools.webpack.WebPackReferenceContributor
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.castSafelyTo
import com.intellij.util.containers.MultiMap
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider

class NuxtComponentProvider : VueContainerInfoProvider {

  override fun getAdditionalComponents(scope: GlobalSearchScope,
                                       sourceComponents: VueContainerInfoProvider.ComponentsInfo): VueContainerInfoProvider.ComponentsInfo? =
    NuxtModelManager.getApplication(scope)
      ?.config?.takeIf { it.file != null }
      ?.let { config ->
        val resolvedDirs = config.components.asSequence()
          .mapNotNull { componentDir -> resolvePath(config.file!!, componentDir)?.let { Pair(it, componentDir) } }
          .sortedWith(
            Comparator.comparingInt<Pair<VirtualFile, NuxtConfig.ComponentsDirectoryConfig>> { it.second.level }
              .thenComparingInt { dir -> -dir.first.path.count { it == '\\' || it == '/' } })
          .toList()

        sourceComponents.local.entrySet()
          .asSequence()
          .flatMap { it.value }
          .flatMap { component ->
            val componentFile = component.source?.containingFile?.virtualFile
                                ?: return@flatMap emptySequence()
            val index = resolvedDirs.indexOfFirst { VfsUtil.isAncestor(it.first, componentFile, true) }
            if (index < 0) return@flatMap emptySequence()
            val componentDirConfig = resolvedDirs[index].second
            if (componentFile.extension.let { componentDirConfig.extensions.contains(it) }) {
              val prefix = componentDirConfig.prefix.let { if (it.isNotEmpty()) "$it-" else it }
              val dirPrefix = if (componentDirConfig.pathPrefix) {
                VfsUtil.getRelativePath(componentFile.parent, resolvedDirs[index].first, '-')
                  ?.takeIf { it.isNotEmpty() }
                  ?.let{ fromAsset(it).replace(MULTI_HYPHEN_REGEX, "-") + "-" } ?: ""
              } else ""
              val baseName = fromAsset(componentFile.nameWithoutExtension)
              val name = if (prefix.isNotEmpty() && (baseName.startsWith(prefix) || baseName == componentDirConfig.prefix)) {
                baseName
              } else "$prefix$dirPrefix$baseName"
              sequenceOf(Triple(name, component, index), Triple("lazy-$name", component, index))
            }
            else emptySequence()
          }
          .sortedBy { it.third }
          .distinctBy { it.first }
          .fold(MultiMap.create<String, VueComponent>()) { map, (name, component) -> map.also { it.putValue(name, component) } }
          .let { VueContainerInfoProvider.ComponentsInfo(MultiMap.empty(), it) }
      }

  private fun resolvePath(configFile: PsiFile, componentDir: NuxtConfig.ComponentsDirectoryConfig): VirtualFile? =
    if (componentDir.path.startsWith("~") || componentDir.path.startsWith("@")) {
      WebPackConfigManager.getInstance(configFile.project)
        .resolveConfig(configFile)
        .takeIf { !it.isEmpty() }
        ?.let { webpackReferenceProvider.getAliasedReferences(componentDir.path.trimEnd('/'), configFile, 0, null, it, true) }
        ?.lastOrNull()
        ?.resolve()
        ?.castSafelyTo<PsiDirectory>()
        ?.virtualFile
    }
    else {
      configFile.virtualFile.parent?.findFileByRelativePath(componentDir.path)
    }
      ?.takeIf { it.isDirectory && it.name != JSLibraryUtil.NODE_MODULES }

  companion object {
    val webpackReferenceProvider = WebPackReferenceContributor()
    private val MULTI_HYPHEN_REGEX = Regex("-{2,}")
  }

}