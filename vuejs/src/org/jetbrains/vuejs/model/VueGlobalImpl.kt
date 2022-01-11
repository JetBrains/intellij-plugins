// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.web.webTypes.nodejs.PackageJsonWebTypesRegistryManager
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil.NODE_MODULES
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.MultiMap
import org.jetbrains.vuejs.model.source.VueSourceGlobal
import org.jetbrains.vuejs.model.source.VueSourcePlugin
import java.util.concurrent.ConcurrentHashMap

internal class VueGlobalImpl(override val project: Project, override val packageJsonUrl: String)
  : VueDelegatedEntitiesContainer<VueGlobal>(), VueGlobal {

  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = emptyList()
  override val global: VueGlobal get() = this

  override val plugins: List<VuePlugin>
    get() = CachedValuesManager.getManager(project).getCachedValue(this, this::buildPluginsList)

  override val apps: List<VueApp> get() = delegate.apps
  override val unregistered: VueEntitiesContainer get() = delegate.unregistered

  private val mySourceGlobal = VueSourceGlobal(project, packageJsonUrl)
  private val packageJson: VirtualFile? get() = findFileByUrl(packageJsonUrl)

  override val delegate
    get() = mySourceGlobal

  override fun getParents(scopeElement: VueScopeElement): List<VueEntitiesContainer> =
    getElementToParentMap().get(scopeElement).toList()

  private fun getElementToParentMap(): MultiMap<VueScopeElement, VueEntitiesContainer> =
    CachedValuesManager.getManager(project).getCachedValue(this) {
      Result.create(buildElementToParentMap(),
                    PsiModificationTracker.MODIFICATION_COUNT,
                    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    } ?: MultiMap.empty()

  private fun buildPluginsList(): Result<List<VuePlugin>> {
    val result = mutableListOf<VuePlugin>()
    val dependencies = mutableListOf<Any>(VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    packageJson?.let { file ->
      dependencies.add(PackageJsonWebTypesRegistryManager.getModificationTracker(project, file))
      PackageJsonWebTypesRegistryManager.getNodeModulesWithoutWebTypes(project, file)
        .filter { isVueLibrary(it) }
        .map { VueSourcePlugin(project, it.name, it.version?.toString(), it.packageJsonFile) }
        .toCollection(result)
    }
    return Result.create(result, dependencies)
  }

  private fun buildElementToParentMap(): MultiMap<VueScopeElement, VueEntitiesContainer> {
    val result = MultiMap<VueScopeElement, VueEntitiesContainer>()
    sequenceOf(this)
      .plus(plugins)
      .plus(apps)
      .forEach { container ->
        sequenceOf(container.components.values,
                   container.directives.values,
                   container.filters.values,
                   container.mixins)
          .flatMap { it.asSequence() }
          .let { sequence ->
            if (container is VueApp)
              container.rootComponent?.let { rootComponent -> sequence.plus(rootComponent) } ?: sequence
            else
              sequence
          }
          .mapNotNull { if (it is VueDelegatedContainer<*>) it.delegate else it }
          .forEach { el -> result.putValue(el, container) }
      }
    return result
  }

  override fun equals(other: Any?): Boolean {
    return (other as? VueGlobalImpl)?.let {
      it.project == project && it.packageJsonUrl == packageJsonUrl
    } ?: false
  }

  override fun hashCode(): Int {
    return (project.hashCode()) * 31 + packageJsonUrl.hashCode()
  }

  override fun createPointer(): Pointer<out VueGlobal> = Pointer.hardPointer(this)

  companion object {

    fun findFileByUrl(packageJsonUrl: String?): VirtualFile? {
      return if (packageJsonUrl != null)
      // TODO consider refresh if not valid
        VirtualFileManager.getInstance().findFileByUrl(packageJsonUrl)
          ?.takeIf { it.isValid }
      else null
    }

    fun getParents(scopeElement: VueScopeElement): List<VueEntitiesContainer> =
      scopeElement.global?.getParents(scopeElement) ?: emptyList()

    fun get(context: PsiElement): VueGlobal {
      val psiFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context)
      return psiFile?.originalFile
               ?.virtualFile
               ?.let { locatePackageJson(it) }
               ?.let {
                 getGlobalsMap(context.project)
                   .computeIfAbsent(it.url) { url -> VueGlobalImpl(context.project, url) }
               }
             ?: VueSourceGlobal(context.project, null)
    }

    private val GLOBALS_CACHE_KEY = Key<CachedValue<MutableMap<String, VueGlobalImpl>>>("vue.globals")

    private fun getGlobalsMap(project: Project): MutableMap<String, VueGlobalImpl> =
      CachedValuesManager.getManager(project).getCachedValue(project, GLOBALS_CACHE_KEY, {
        Result.create(ConcurrentHashMap(), ModificationTracker.NEVER_CHANGED)
      }, false)

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

    private fun isVueLibrary(data: PackageJsonData): Boolean =
      data.name == "vue"
      || data.containsOneOfDependencyOfAnyType("vue-loader", "vue-latest", "vue", "vue-template-compiler")

  }
}
