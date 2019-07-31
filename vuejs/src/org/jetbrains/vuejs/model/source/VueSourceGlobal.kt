// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.model.*

class VueSourceGlobal(override val project: Project, private val packageJson: VirtualFile?) : VueGlobal {

  override val global: VueGlobal = this
  override val plugins: List<VuePlugin> = emptyList()
  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = emptyList()

  override val directives: Map<String, VueDirective>
    get() = getCachedValue { searchScope ->
      StreamEx.of(getForAllKeys(searchScope, VueGlobalDirectivesIndex.KEY))
        .mapToEntry({ it.name }, { VueSourceDirective(it.name, it.parent) })
        // TODO properly support multiple directives with the same name
        .distinctKeys()
        .toMap()
    }
  override val filters: Map<String, VueFilter> = emptyMap()

  override val apps: List<VueApp>
    get() = getCachedValue { buildAppsList(it) }
  override val mixins: List<VueMixin>
    get() = getCachedValue { buildMixinsList(it) }

  override val components: Map<String, VueComponent>
    get() {
      return getComponents(true)
    }

  override val unregistered: VueEntitiesContainer = object : VueEntitiesContainer {
    override val components: Map<String, VueComponent>
      get() {
        return getComponents(false)
      }
    override val directives: Map<String, VueDirective> = emptyMap()
    override val filters: Map<String, VueFilter> = emptyMap()
    override val mixins: List<VueMixin> get() = emptyList()
    override val source: PsiElement? = null
    override val parents: List<VueEntitiesContainer> = emptyList()
  }

  override fun equals(other: Any?): Boolean {
    return (other as? VueSourceGlobal)?.let {
      it.project == project && it.packageJson == packageJson
    } ?: false
  }

  override fun hashCode(): Int {
    return (project.hashCode()) * 31 + packageJson.hashCode()
  }

  private fun getComponents(global: Boolean): Map<String, VueComponent> {
    return getCachedValue { scope ->
      val componentsData = VueComponentsCalculation.calculateScopeComponents(scope, false)

      val moduleComponents = componentsData.map

      val localComponents: MutableMap<String, VueComponent> = EntryStream.of(moduleComponents)
        .filterValues { !it.second }
        .mapValues { VueModelManager.getComponent(it.first) }
        .nonNullValues()
        // TODO properly support multiple components with the same name
        .distinctKeys()
        .into(mutableMapOf())

      val globalComponents: MutableMap<String, VueComponent> = EntryStream.of(moduleComponents)
        .filterValues { it.second }
        .mapValues { VueModelManager.getComponent(it.first) }
        .nonNullValues()
        // TODO properly support multiple components with the same name
        .distinctKeys()
        .into(mutableMapOf())

      componentsData.libCompResolveMap.forEach { (alias, target) ->
        localComponents[target]?.let { localComponents.putIfAbsent(alias, it) }
        globalComponents[target]?.let { globalComponents.putIfAbsent(alias, it) }
      }

      mapOf(Pair(true, globalComponents),
            Pair(false, localComponents))
    }[global] ?: emptyMap()
  }

  private fun <T> getCachedValue(provider: (GlobalSearchScope) -> T): T {
    val psiFile: PsiFile? = packageJson?.let {
      PsiManager.getInstance(project).findFile(it)
    }
    val searchScope = psiFile?.parent
                        ?.let {
                          GlobalSearchScopesCore.directoryScope(it, true)
                            .intersectWith(GlobalSearchScope.projectScope(project))
                        }
                      ?: GlobalSearchScope.projectScope(project)
    val manager = CachedValuesManager.getManager(project)
    return CachedValuesManager.getManager(project).getCachedValue(
      psiFile ?: project,
      manager.getKeyForClass(provider::class.java),
      { Result.create(provider(searchScope), PsiModificationTracker.MODIFICATION_COUNT) },
      false)
  }

  companion object {
    private fun buildMixinsList(scope: GlobalSearchScope): List<VueMixin> {
      val elements = resolve(GLOBAL, scope, VueMixinBindingIndex.KEY) ?: return emptyList()
      return StreamEx.of(elements)
        .map { VueComponents.vueMixinDescriptorFinder(it) }
        .nonNull()
        .map { VueModelManager.getMixin(it!!) }
        .nonNull()
        .toList()
    }

    private fun buildAppsList(scope: GlobalSearchScope): List<VueApp> {
      return StreamEx.of(getForAllKeys(scope, VueOptionsIndex.KEY))
        .filter(VueComponents.Companion::isNotInLibrary)
        .map { it as? JSObjectLiteralExpression ?: PsiTreeUtil.getParentOfType(it, JSObjectLiteralExpression::class.java) }
        .nonNull()
        .map { VueModelManager.getApp(it!!) }
        .filter { it.element != null }
        .toList()
    }
  }
}
