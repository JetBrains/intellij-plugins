// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import one.util.streamex.EntryStream
import org.jetbrains.vuejs.index.BOOTSTRAP_VUE
import org.jetbrains.vuejs.index.SHARDS_VUE
import org.jetbrains.vuejs.index.VUETIFY
import org.jetbrains.vuejs.model.*

class VueSourcePlugin private constructor(override val components: Map<String, VueComponent>,
                                          override val source: PsiElement) : VuePlugin {

  override val moduleName: String? = null
  override val parents: List<VueEntitiesContainer> = emptyList()

  override val directives: Map<String, VueDirective> = emptyMap()
  override val filters: Map<String, VueFilter> = emptyMap()
  override val mixins: List<VueMixin> = emptyList()

  companion object {
    private val PACKAGES_WITH_GLOBAL_COMPONENTS = arrayOf(VUETIFY, BOOTSTRAP_VUE, SHARDS_VUE)

    fun create(project: Project, packageJsonFile: VirtualFile): VueSourcePlugin? {
      val packageJson = PsiManager.getInstance(project).findFile(packageJsonFile)
      return packageJson?.parent?.let { psiDirectory ->
        CachedValuesManager.getCachedValue(psiDirectory) {
          val directoryFile = psiDirectory.virtualFile
          val scope = GlobalSearchScopesCore.directoryScope(psiDirectory.project, directoryFile, true)
          val globalize = PACKAGES_WITH_GLOBAL_COMPONENTS.contains(psiDirectory.name)

          val result: MutableMap<String, VueComponent> = EntryStream
            .of(VueComponentsCalculation.calculateScopeComponents(scope, globalize).map)
            .mapValues { VueModelManager.getComponent(it.first) }
            .nonNullValues()
            // TODO properly support multiple components with the same name
            .distinctKeys()
            .into(mutableMapOf())

          CachedValueProvider.Result(if (result.isEmpty()) null
                                     else VueSourcePlugin(result, psiDirectory),
                                     NodeModulesDirectoryManager.getInstance(psiDirectory.project).nodeModulesDirChangeTracker,
                                     psiDirectory)
        }
      }
    }
  }
}
