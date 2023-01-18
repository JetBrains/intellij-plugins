// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.javascript.web.js.WebJSResolveUtil.resolveMergedInterfaceJSTypeFromNodeModule
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.*
import java.util.*

class VueTypedGlobal(override val delegate: VueGlobal,
                     override val source: PsiElement) : VueDelegatedEntitiesContainer<VueGlobal>(), VueGlobal {

  private val typedGlobalComponents: Map<String, VueComponent> =
    CachedValuesManager.getCachedValue(source) {
      val jsRecordType = resolveMergedInterfaceJSTypeFromNodeModule(source, VUE_MODULE, "GlobalComponents")
      val map = jsRecordType.properties.asSequence().mapNotNull { property ->
        property.memberSource.singleElement?.let { Pair(property.memberName, VueTypedComponent(it, property.memberName)) }
      }.toMap()
      CachedValueProvider.Result.create(map, PsiModificationTracker.MODIFICATION_COUNT)
    }

  override val components: Map<String, VueComponent>
    get() = delegate.components + typedGlobalComponents

  override val apps: List<VueApp>
    get() = delegate.apps

  override val plugins: List<VuePlugin>
    get() = delegate.plugins

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

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueTypedGlobal
    && other.delegate == delegate
    && other.source == source

  override fun hashCode(): Int =
    Objects.hash(delegate, source)

  override val parents: List<VueEntitiesContainer>
    get() = emptyList()
}