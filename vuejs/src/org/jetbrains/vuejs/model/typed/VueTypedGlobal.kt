// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.library.JSLibraryUtil.findUpClosestNodeModulesResolveRoot
import com.intellij.lang.javascript.psi.JSCommonTypeNames.MODULE_PREFIX
import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.util.stubSafeChildren
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.index.GLOBAL_COMPONENTS
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.*
import java.util.*

class VueTypedGlobal(override val delegate: VueGlobal,
                     override val source: PsiElement) : VueDelegatedEntitiesContainer<VueGlobal>(), VueGlobal {

  private val typedGlobalComponents: Map<String, VueComponent> =
    CachedValuesManager.getCachedValue(source) {
      val file = source.containingFile
      val map = TypeScriptUtil.getAllAugmentationModules(source.project, file)
        .asSequence()
        .filter { module -> module.name?.removePrefix(MODULE_PREFIX)?.let { it.startsWith("@$VUE_MODULE") || it.startsWith(VUE_MODULE) } == true }
        .flatMap { it.stubSafeChildren }
        .filterIsInstance<TypeScriptInterface>()
        .filter { it.name == GLOBAL_COMPONENTS }
        .flatMap { it.fields.asSequence() }
        .groupBy { it.name }
        .mapNotNull { nameToField ->
          val fromLibrary = mutableListOf<JSField>()
          val field = nameToField.value.find { property ->
            val isFromProject = property.containingFile?.virtualFile?.let {
              findUpClosestNodeModulesResolveRoot(it)
            } != null

            if (isFromProject) {
              return@find true
            }

            fromLibrary.add(property)
            false
          } ?: fromLibrary.singleOrNull() ?: return@mapNotNull null
          val fieldName = field.name ?: return@mapNotNull null
          Pair(fieldName, VueTypedComponent(field, fieldName))
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