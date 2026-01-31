// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.javascript.nodejs.library.node_modules.NodeModulesDirectoryManager
import com.intellij.javascript.nodejs.packages.NodePackageLinkResolver
import com.intellij.model.Pointer
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.impl.LightFilePointer
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.index.BOOTSTRAP_VUE_MODULE
import org.jetbrains.vuejs.index.SHARDS_VUE_MODULE
import org.jetbrains.vuejs.index.VUETIFY_MODULE
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueLibrary
import org.jetbrains.vuejs.model.VueMixin
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueNamedComponent
import org.jetbrains.vuejs.model.typed.VueTypedEntitiesProvider

class VueLibraryImpl(
  private val project: Project,
  override val moduleName: String?,
  override val moduleVersion: String?,
  private val packageJsonFile: VirtualFile,
) : UserDataHolderBase(),
    VueLibrary {

  override val parents: List<VueEntitiesContainer> = emptyList()

  override val directives: Map<String, VueDirective> = emptyMap()
  override val filters: Map<String, VueFilter> = emptyMap()
  override val mixins: List<VueMixin> = emptyList()

  override val source: PsiDirectory?
    get() = PsiManager.getInstance(project).findFile(packageJsonFile)?.parent

  override val defaultProximity: VueModelVisitor.Proximity
    get() = componentsWithProximity.first

  override val components: Map<String, VueNamedComponent>
    get() = componentsWithProximity.second

  private val componentsWithProximity: Pair<VueModelVisitor.Proximity, Map<String, VueNamedComponent>>
    get() = CachedValuesManager.getManager(project).getCachedValue(this) {
      val dependencies = mutableListOf<Any>(
        NodeModulesDirectoryManager.getInstance(project).nodeModulesDirChangeTracker,
        packageJsonFile,
        DumbService.getInstance(project).modificationTracker,
      )
      val psiDirectory = source
      val components: Pair<VueModelVisitor.Proximity, Map<String, VueNamedComponent>>
      if (psiDirectory == null) {
        components = Pair(VueModelVisitor.Proximity.GLOBAL, emptyMap())
        dependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
      }
      else {
        val directoryFile = psiDirectory.virtualFile
        val scope = GlobalSearchScopesCore.directoryScope(psiDirectory.project, directoryFile, true)
        val globalize = PACKAGES_WITH_GLOBAL_COMPONENTS.contains(psiDirectory.name)

        if (directoryFile.`is`(VFileProperty.SYMLINK)
            && FileIndexFacade.getInstance(project).isInContent(directoryFile.canonicalFile ?: directoryFile)) {
          // Track modifications in plugins ASTs only if they are possibly local
          dependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
        }

        val indexedPsiDirectory: PsiDirectory = findIndexedDirectory(psiDirectory)

        components =
          VueTypedEntitiesProvider.calculateDtsComponents(indexedPsiDirectory)
            .takeIf { it.isNotEmpty() }
            ?.let { Pair(VueModelVisitor.Proximity.OUT_OF_SCOPE, it) }
          ?: VueComponentsCalculation.calculateScopeComponents(scope, globalize)
            .list.asSequence()
            .map { Pair(fromAsset(it.first.name), it.first) }
            .distinctBy { it.first }
            .toMap()
            .let { Pair(VueModelVisitor.Proximity.GLOBAL, it) }
      }
      CachedValueProvider.Result(components, *dependencies.toTypedArray())
    }

  private fun findIndexedDirectory(psiDirectory: PsiDirectory): PsiDirectory {
    val target = NodePackageLinkResolver.resolve(psiDirectory.virtualFile)
    return psiDirectory.manager.findDirectory(target) ?: psiDirectory
  }

  override fun equals(other: Any?): Boolean {
    return (other as? VueLibraryImpl)?.packageJsonFile == packageJsonFile
           && other.project == project
  }

  override fun hashCode(): Int {
    var result = project.hashCode()
    result = 31 * result + packageJsonFile.hashCode()
    return result
  }

  override fun toString(): String {
    return "VueLibraryImpl($moduleName)"
  }

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val project = this.project
    val moduleName = this.moduleName
    val moduleVersion = this.moduleVersion
    val packageJsonFilePtr = LightFilePointer(packageJsonFile.url)
    return Pointer {
      packageJsonFilePtr.file?.let {
        VueLibraryImpl(project, moduleName, moduleVersion, it)
      }
    }
  }

  companion object {
    private val PACKAGES_WITH_GLOBAL_COMPONENTS = arrayOf(VUETIFY_MODULE, BOOTSTRAP_VUE_MODULE, SHARDS_VUE_MODULE)
  }
}
