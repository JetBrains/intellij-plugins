// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import one.util.streamex.EntryStream
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.registry.VueWebTypesRegistry

class VueSourceGlobal(private val module: Module) : VueGlobal {

  override val directives: Map<String, VueDirective> = emptyMap()
  override val filters: Map<String, VueFilter> = emptyMap()

  override val apps: List<VueApp> = emptyList()
  override val mixins: List<VueMixin> = emptyList()

  override val components: Map<String, VueComponent>
    get() {
      val localModule = module
      return CachedValuesManager.getManager(module.project).getCachedValue(localModule) {
        val result: MutableMap<String, VueComponent> = mutableMapOf()

        EntryStream.of(VueComponentsCalculation.calculateScopeComponents(
          GlobalSearchScope.moduleWithDependenciesScope(localModule), false).map)
          .mapValues { VueModelManager.getComponent(it.first) }
          .nonNullValues()
          .into(result)

        CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

  override val plugins: List<VuePlugin>
    get() {
      return VueWebTypesRegistry.getInstance().getVuePlugins(module)
    }


  fun findComponent(templateElement: PsiElement): VueComponent? {
    return null
  }
}
