// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil.NODE_MODULES
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.MultiMap
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.model.source.VueSourceGlobal
import org.jetbrains.vuejs.model.webtypes.registry.VueWebTypesRegistry

internal class VueGlobalImpl(override val project: Project, private val packageJson: VirtualFile)
  : VueDelegatedEntitiesContainer<VueGlobal>(), VueGlobal {

  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = emptyList()
  override val global: VueGlobal? get() = this

  override val plugins: List<VuePlugin>
    get() = packageJsonPsi?.let { psiFile ->
      CachedValuesManager.getCachedValue(psiFile, this::buildPluginsList)
    } ?: emptyList()

  override val apps: List<VueApp> get() = delegate.apps
  override val unregistered: VueEntitiesContainer get() = delegate.unregistered

  private val packageJsonPsi: PsiFile? get() = PsiManager.getInstance(project).findFile(packageJson)

  private val mySourceGlobal = VueSourceGlobal(project, packageJson)

  override val delegate
    get() = packageJsonPsi?.let { psiFile ->
      CachedValuesManager.getCachedValue(psiFile) {
        VueWebTypesRegistry.createWebTypesGlobal(project, packageJson, this)
        ?: Result.create(null as VueGlobal?, packageJson)
      }
    } ?: mySourceGlobal

  private fun getElementToParentMap(): MultiMap<VueScopeElement, VueEntitiesContainer> {
    return packageJsonPsi?.let { psiFile ->
      CachedValuesManager.getCachedValue(psiFile) {
        Result.create(buildElementToParentMap(),
                      PsiModificationTracker.MODIFICATION_COUNT,
                      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                      VueWebTypesRegistry.MODIFICATION_TRACKER)
      }
    } ?: MultiMap.empty()
  }

  private fun buildPluginsList(): Result<List<VuePlugin>> {
    val result = mutableListOf<VuePlugin>()
    val dependencies = mutableListOf<Any>()
    val enabledPackagesResult = VueWebTypesRegistry.instance.webTypesEnabledPackages
    val enabledPackages = enabledPackagesResult.value.asSequence()
      .flatMap { pkgName ->
        if (pkgName.startsWith('@') && pkgName.contains('/'))
          sequenceOf(pkgName, pkgName.takeWhile { ch -> ch != '/' })
        else sequenceOf(pkgName)
      }
      .toSet()
    dependencies.add(VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    dependencies.add(enabledPackagesResult.dependencyItems)
    dependencies.add(NodeModulesDirectoryManager.getInstance(project).nodeModulesDirChangeTracker)
    PackageJsonUtil.processUpPackageJsonFilesInAllScope(packageJson) { candidate ->
      result.addAll(getPlugins(candidate, enabledPackages))
      dependencies.add(candidate)
      true
    }
    return Result.create(result, *dependencies.toTypedArray())
  }

  private fun getPlugins(packageJson: VirtualFile,
                         enabledPackages: Set<String>): List<VuePlugin> =
    NodeModuleUtil.findNodeModulesByPackageJson(packageJson)
      ?.let { getVuePluginPackageJsons(it, enabledPackages) }
      ?.filter { isVueLibrary(it) }
      ?.map { VuePluginImpl(project, it) }
      ?.toList()
    ?: emptyList()

  private fun getVuePluginPackageJsons(nodeModules: VirtualFile,
                                       enabledPackages: Set<String>): Sequence<VirtualFile> {
    return nodeModules.children.asSequence()
      .filter { enabledPackages.contains(it.name) && it.isDirectory }
      .flatMap { dir ->
        val name = dir.name
        if (name.startsWith("@"))
          dir.children.asSequence().filter {
            enabledPackages.contains("$name/${it.name}") && it.isDirectory
          }
        else
          sequenceOf(dir)
      }
      .mapNotNull { PackageJsonUtil.findChildPackageJsonFile(it) }
      .plus(FilenameIndex.getVirtualFilesByName(
        project, PackageJsonUtil.FILE_NAME,
        GlobalSearchScopesCore.directoryScope(project, nodeModules, true)
      ))
      .distinct()
  }

  private fun buildElementToParentMap(): MultiMap<VueScopeElement, VueEntitiesContainer> {
    val result = MultiMap<VueScopeElement, VueEntitiesContainer>()
    StreamEx.of<VueEntitiesContainer>(this)
      .append(plugins)
      .append(apps)
      .forEach { container ->
        StreamEx.of(container.components.values,
                    container.directives.values,
                    container.filters.values,
                    container.mixins)
          .flatMap(Collection<VueScopeElement>::stream)
          .forEach { el -> result.putValue(el, container) }
      }
    return result
  }

  override fun equals(other: Any?): Boolean {
    return (other as? VueGlobalImpl)?.let {
      it.project == project && it.packageJson == packageJson
    } ?: false
  }

  override fun hashCode(): Int {
    return (project.hashCode()) * 31 + packageJson.hashCode()
  }

  companion object {
    fun getParents(scopeElement: VueScopeElement): List<VueEntitiesContainer> {
      return (scopeElement.global as? VueGlobalImpl)
               ?.getElementToParentMap()
               ?.get(scopeElement)
               ?.toList() ?: return emptyList()
    }

    fun get(context: PsiElement): VueGlobal? {
      val psiFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context)
      return psiFile?.originalFile
               ?.virtualFile
               ?.let { locatePackageJson(it) }
               ?.let { VueGlobalImpl(context.project, it) }
             ?: VueSourceGlobal(context.project, null)
    }

    private fun locatePackageJson(context: VirtualFile): VirtualFile? {
      var result: VirtualFile? = null
      PackageJsonUtil.processUpPackageJsonFilesInAllScope(context) { candidate ->
        var level = 4
        var parent = candidate.parent
        while (parent != null && level-- >= 0) {
          if (NODE_MODULES == parent.name) {
            return@processUpPackageJsonFilesInAllScope true
          }
          parent = parent.parent
        }
        result = candidate
        false
      }
      return result
    }

    private fun isVueLibrary(it: VirtualFile): Boolean {
      val data = PackageJsonUtil.getOrCreateData(it)
      return data.name == "vue"
             || data.containsOneOfDependencyOfAnyType("vue-loader", "vue-latest", "vue", "vue-template-compiler")
             || data.webTypes != null
    }

  }
}
