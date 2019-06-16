// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.index.GLOBAL
import org.jetbrains.vuejs.index.VueMixinBindingIndex
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.resolve
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.registry.VueWebTypesRegistry

class VueSourceGlobal(private val module: Module) : VueGlobal {

  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = emptyList()
  override val global = this

  override val directives: Map<String, VueDirective> = emptyMap()
  override val filters: Map<String, VueFilter> = emptyMap()

  override val apps: List<VueApp> = emptyList()
  override val mixins: List<VueMixin>
    get() {
      val localModule = module
      return CachedValuesManager.getManager(localModule.project).getCachedValue(localModule) {
        CachedValueProvider.Result.create(buildMixinsList(module), PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

  override val components: Map<String, VueComponent>
    get() {
      return getComponents(true)
    }

  override val plugins: List<VuePlugin>
    get() {
      return VueWebTypesRegistry.getInstance().getVuePlugins(module)
    }

  fun getComponents(global: Boolean): Map<String, VueComponent> {
    val localModule = module
    return CachedValuesManager.getManager(localModule.project).getCachedValue(localModule) {
      val componentsData = VueComponentsCalculation.calculateScopeComponents(
        GlobalSearchScope.moduleWithDependenciesScope(localModule), false)

      val moduleComponents = componentsData.map

      val localComponents: MutableMap<String, VueComponent> = mutableMapOf()
      EntryStream.of(moduleComponents)
        .filterValues { !it.second }
        .mapValues { VueModelManager.getComponent(it.first) }
        .nonNullValues()
        .into(localComponents)

      val globalComponents: MutableMap<String, VueComponent> = mutableMapOf()
      EntryStream.of(moduleComponents)
        .filterValues { it.second }
        .mapValues { VueModelManager.getComponent(it.first) }
        .nonNullValues()
        .into(globalComponents)

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

  fun findComponent(templateElement: PsiElement): VueComponent? {
    return findModule(templateElement)
      ?.let { content -> ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment }
      ?.let { defaultExport -> VueComponents.getExportedDescriptor(defaultExport) }
      ?.obj
      ?.let { VueModelManager.getComponent(it) }
  }

  companion object {
    private fun buildMixinsList(module: Module): List<VueMixin> {
      val elements = resolve(GLOBAL, GlobalSearchScope.projectScope(module.project), VueMixinBindingIndex.KEY) ?: emptyList()
      return StreamEx.of(elements)
        .map { VueComponents.vueMixinDescriptorFinder(it) }
        .nonNull()
        .map { VueModelManager.getMixin(it!!) }
        .nonNull()
        .toList()
    }
  }
}
