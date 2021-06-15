// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.index.BOOTSTRAP_VUE_MODULE
import org.jetbrains.vuejs.index.SHARDS_VUE_MODULE
import org.jetbrains.vuejs.index.VUETIFY_MODULE
import org.jetbrains.vuejs.model.*

class VueSourcePlugin constructor(private val project: Project,
                                  override val moduleName: String?,
                                  override val moduleVersion: String?,
                                  private val packageJsonFile: VirtualFile) : UserDataHolderBase(), VuePlugin {

  override val parents: List<VueEntitiesContainer> = emptyList()

  override val directives: Map<String, VueDirective> = emptyMap()
  override val filters: Map<String, VueFilter> = emptyMap()
  override val mixins: List<VueMixin> = emptyList()

  override val source: PsiDirectory?
    get() = PsiManager.getInstance(project).findFile(packageJsonFile)?.parent

  override val components: Map<String, VueComponent>
    get() = CachedValuesManager.getManager(project).getCachedValue(this) {
      val dependencies = mutableListOf<Any>(NodeModulesDirectoryManager.getInstance(project).nodeModulesDirChangeTracker,
                                            packageJsonFile)
      val psiDirectory = source
      val components: Map<String, VueComponent>
      if (psiDirectory == null) {
        components = emptyMap()
        dependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
      }
      else {
        val directoryFile = psiDirectory.virtualFile
        val scope = GlobalSearchScopesCore.directoryScope(psiDirectory.project, directoryFile, true)
        val globalize = PACKAGES_WITH_GLOBAL_COMPONENTS.contains(psiDirectory.name)

        if (directoryFile.`is`(VFileProperty.SYMLINK)) {
          // Track modifications in plugins ASTs only if they are possibly local
          dependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
        }

        components = VueComponentsCalculation.calculateScopeComponents(scope, globalize).map.asSequence()
          .mapNotNull { VueModelManager.getComponent(it.value.first)?.let { component -> Pair(it.key, component) } }
          .distinctBy { it.first }
          .toMap()
      }
      CachedValueProvider.Result(components, *dependencies.toTypedArray())
    } ?: emptyMap()

  override fun equals(other: Any?): Boolean {
    return (other as? VueSourcePlugin)?.packageJsonFile == packageJsonFile
           && other.project == project
  }

  override fun hashCode(): Int {
    var result = project.hashCode()
    result = 31 * result + packageJsonFile.hashCode()
    return result
  }

  companion object {
    private val PACKAGES_WITH_GLOBAL_COMPONENTS = arrayOf(VUETIFY_MODULE, BOOTSTRAP_VUE_MODULE, SHARDS_VUE_MODULE)
  }
}
