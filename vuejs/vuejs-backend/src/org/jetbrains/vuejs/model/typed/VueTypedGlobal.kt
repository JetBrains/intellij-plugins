// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolPropertiesFromAugmentations
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.isGlobalDirectiveName
import org.jetbrains.vuejs.index.GLOBAL_COMPONENTS
import org.jetbrains.vuejs.index.GLOBAL_DIRECTIVES
import org.jetbrains.vuejs.index.VUE_CORE_MODULES
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.*

data class VueTypedGlobal(
  override val delegate: VueGlobal,
  override val source: PsiElement,
) : VueDelegatedEntitiesContainer<VueGlobal>(),
    VueGlobal {

  private val typedGlobalComponents: Map<String, VueComponent> =
    CachedValuesManager.getCachedValue(source) {
      val result = resolveSymbolPropertiesFromAugmentations(source, VUE_CORE_MODULES, GLOBAL_COMPONENTS)
        .mapValues { VueTypedComponent(it.value, it.key) }

      CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }

  private val typedGlobalDirectives: Map<String, VueDirective> =
    CachedValuesManager.getCachedValue(source) {
      val augmentedProperties = resolveSymbolPropertiesFromAugmentations(
        scope = source,
        moduleNames = setOf(VUE_MODULE),
        symbolName = GLOBAL_DIRECTIVES,
      )

      val result = buildMap {
        for ((name, source) in augmentedProperties) {
          if (!isGlobalDirectiveName(name)) {
            continue
          }

          put(name, VueTypedDirective(source, name))
        }
      }

      CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }

  override val components: Map<String, VueComponent>
    get() = delegate.components + typedGlobalComponents

  override val directives: Map<String, VueDirective>
    get() = delegate.directives + typedGlobalDirectives

  override val apps: List<VueApp>
    get() = delegate.apps

  override val libraries: List<VueLibrary>
    get() = delegate.libraries

  override val unregistered: VueEntitiesContainer
    get() = delegate.unregistered

  override val project: Project
    get() = delegate.project

  override val packageJsonUrl: String?
    get() = delegate.packageJsonUrl

  override fun getParents(scopeElement: VueScopeElement): List<VueEntitiesContainer> =
    delegate.getParents(scopeElement)

  override fun createPointer(): Pointer<out VueGlobal> {
    val delegatePtr = delegate.createPointer()
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueTypedGlobal(delegate, source)
    }
  }

  override val parents: List<VueEntitiesContainer>
    get() = emptyList()
}
