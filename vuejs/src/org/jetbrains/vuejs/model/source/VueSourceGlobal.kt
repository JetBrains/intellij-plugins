// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.MultiMap
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.getSearchScope
import org.jetbrains.vuejs.index.*
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.registry.VueWebTypesRegistry

class VueSourceGlobal(override val project: Project) : VueGlobal {

  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = emptyList()
  override val global = this

  override val directives: Map<String, VueDirective>
    get() {
      return project.let { project ->
        CachedValuesManager.getManager(project).getCachedValue(project) {
          val result = StreamEx.of(getForAllKeys(getSearchScope(project), VueGlobalDirectivesIndex.KEY))
            .mapToEntry({ it.name }, { VueSourceDirective(it.name, it.parent) })
            // TODO properly support multiple directives with the same name
            .distinctKeys()
            .toMap()

          CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }
      }
    }
  override val filters: Map<String, VueFilter> = emptyMap()

  override val apps: List<VueApp>
    get() {
      return project.let { project ->
        CachedValuesManager.getManager(project).getCachedValue(project) {
          CachedValueProvider.Result.create(buildAppsList(getSearchScope(project)),
                                            PsiModificationTracker.MODIFICATION_COUNT)
        }
      }
    }
  override val mixins: List<VueMixin>
    get() {
      return project.let { project ->
        CachedValuesManager.getManager(project).getCachedValue(project) {
          CachedValueProvider.Result.create(buildMixinsList(getSearchScope(project)),
                                            PsiModificationTracker.MODIFICATION_COUNT)
        }
      }
    }

  override val components: Map<String, VueComponent>
    get() {
      return getComponents(true)
    }

  override val plugins: List<VuePlugin>
    get() {
      return VueWebTypesRegistry.getInstance().getVuePlugins(project)
    }

  fun getComponents(global: Boolean): Map<String, VueComponent> {
    return project.let { project ->
      CachedValuesManager.getManager(project).getCachedValue(project) {
        val componentsData = VueComponentsCalculation.calculateScopeComponents(getSearchScope(project), false)

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

        CachedValueProvider.Result.create(
          mapOf(Pair(true, globalComponents),
                Pair(false, localComponents)),
          PsiModificationTracker.MODIFICATION_COUNT)
      }[global] ?: emptyMap()
    }
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
        .map { VueSourceApp(it!!) }
        .filter { it.element != null }
        .toList()
    }

    fun getParents(scopeElement: VueScopeElement): List<VueEntitiesContainer> {
      val global = scopeElement.global ?: return emptyList()
      return getElementToParentMap(global.project).get(scopeElement).toList()
    }

    private fun getElementToParentMap(project: Project): MultiMap<VueScopeElement, VueEntitiesContainer> {
      return CachedValuesManager.getManager(project).getCachedValue(project) {
        CachedValueProvider.Result.create(buildElementToParentMap(project),
                                          PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

    private fun buildElementToParentMap(project: Project): MultiMap<VueScopeElement, VueEntitiesContainer> {
      val global = VueModelManager.getGlobal(project) ?: return MultiMap.empty()

      val result = MultiMap<VueScopeElement, VueEntitiesContainer>()
      StreamEx.of<VueEntitiesContainer>(global)
        .append(global.plugins)
        .append(global.apps)
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

  }
}
