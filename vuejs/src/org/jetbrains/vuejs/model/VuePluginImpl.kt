// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.model.source.VueSourcePlugin
import org.jetbrains.vuejs.model.webtypes.registry.VueWebTypesRegistry

class VuePluginImpl(private val project: Project, private val packageJson: VirtualFile) :
  VueDelegatedEntitiesContainer<VuePlugin>(), VuePlugin {

  override val moduleName: String? = NodeModuleUtil.inferNodeModulePackageName(packageJson)
  override val source: PsiElement? = null
  override val parents get() = VueGlobalImpl.getParents(this)

  override val delegate: VuePlugin
    get() = CachedValuesManager.getManager(project).getCachedValue(this) {
      buildPlugin()
    }

  private fun buildPlugin(): Result<VuePlugin>? {
    val webTypes = VueWebTypesRegistry.createWebTypesPlugin(project, packageJson, this)
    val dependencies = mutableSetOf<Any>(packageJson, NodeModulesDirectoryManager.getInstance(project).nodeModulesDirChangeTracker)
    dependencies.addAll(webTypes.dependencyItems)
    return Result.create(webTypes.value ?: VueSourcePlugin(project, packageJson), dependencies)
  }

  override fun toString(): String {
    return "VuePlugin [$moduleName]"
  }

  override fun equals(other: Any?) =
    (other as? VuePluginImpl)?.let {
      it.project == project && it.packageJson == packageJson
    } ?: false

  override fun hashCode() = (project.hashCode()) * 31 + packageJson.hashCode()

}
