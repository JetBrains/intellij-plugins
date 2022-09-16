// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model

import com.intellij.lang.javascript.buildTools.webpack.WebpackConfigManager
import com.intellij.lang.javascript.buildTools.webpack.WebpackReferenceContributor
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.asSafely
import com.intellij.util.containers.MultiMap
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.libraries.nuxt.NUXT_COMPONENTS_DEFS
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider

/**
 * @see org.jetbrains.vuejs.model.typed.VueTypedGlobal.typedGlobalComponents
 */
class NuxtComponentProvider : VueContainerInfoProvider {
  private val LAZY = "lazy" // https://nuxtjs.org/docs/directory-structure/components#dynamic-imports

  override fun getAdditionalComponents(scope: GlobalSearchScope,
                                       sourceComponents: VueContainerInfoProvider.ComponentsInfo): VueContainerInfoProvider.ComponentsInfo? =
    NuxtModelManager.getApplication(scope)
      ?.config
      ?.takeIf { config -> config.file.let {
        // Ensure we have a config, and ensure we don't have the auto-generated list of components in .nuxt folder
        it != null && it.parent?.findSubdirectory(NUXT_OUTPUT_FOLDER)?.findFile(NUXT_COMPONENTS_DEFS) == null}
      }
      ?.let { config ->
        // alternative idea: there's .nuxt/components/index.js that contains named exports with components,
        // although it looks like it handles Lazy prefix incorrectly
        // there's also .nuxt/components/plugin.js that contains object literal with correctly generated component names
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
              val dirPrefix = if (componentDirConfig.pathPrefix) {
                VfsUtil.getRelativePath(componentFile.parent, resolvedDirs[index].first, '-')
                  ?.takeIf { it.isNotEmpty() }
                  ?.let { fromAsset(it).replace(MULTI_HYPHEN_REGEX, "-") }
                ?: ""
              }
              else {
                ""
              }

              val configuredPrefix = componentDirConfig.prefix.let { if (it.isNotEmpty()) "$it-" else it }
              val mergedPrefixParts = (configuredPrefix + dirPrefix).split('-').filter { it.isNotEmpty() }

              val baseName = fromAsset(componentFile.nameWithoutExtension)
              val baseNameParts = baseName.split('-')

              var commonIndex = 0
              while (commonIndex < mergedPrefixParts.size) {
                if (mergedPrefixParts[commonIndex] == baseNameParts[0]) {
                  break
                }
                commonIndex += 1
              }

              val parts = mergedPrefixParts.subList(0, commonIndex) + baseNameParts
              val partsWithoutLazy = if (parts.firstOrNull() == LAZY) parts.drop(1) else parts
              val name = partsWithoutLazy.joinToString("-")

              sequenceOf(
                Triple(name, component, index),
                Triple("$LAZY-$name", component, index)
              )
            }
            else {
              emptySequence()
            }
          }
          .sortedBy { it.third }
          .distinctBy { it.first }
          .fold(MultiMap.create<String, VueComponent>()) { map, (name, component) -> map.also { it.putValue(name, component) } }
          .let { VueContainerInfoProvider.ComponentsInfo(MultiMap.empty(), it) }
      }

  private fun resolvePath(configFile: PsiFile, componentDir: NuxtConfig.ComponentsDirectoryConfig): VirtualFile? =
    if (componentDir.path.startsWith("~") || componentDir.path.startsWith("@")) {
      WebpackConfigManager.getInstance(configFile.project)
        .resolveConfig(configFile)
        .takeIf { !it.isEmpty() }
        ?.let { webpackReferenceProvider.getAliasedReferences(componentDir.path.trimEnd('/'), configFile, 0, it, true) }
        ?.lastOrNull()
        ?.resolve()
        ?.asSafely<PsiDirectory>()
        ?.virtualFile
    }
    else {
      configFile.virtualFile.parent?.findFileByRelativePath(componentDir.path)
    }
      ?.takeIf { it.isDirectory && it.name != JSLibraryUtil.NODE_MODULES }

  companion object {
    val webpackReferenceProvider = WebpackReferenceContributor()
    private val MULTI_HYPHEN_REGEX = Regex("-{2,}")
  }

}