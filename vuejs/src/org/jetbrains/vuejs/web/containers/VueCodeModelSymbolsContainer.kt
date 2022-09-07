// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.containers

import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.WebSymbolsContainerWithCache
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.asWebSymbol

class VueCodeModelSymbolsContainer<K> private constructor(private val container: VueEntitiesContainer,
                                                          project: Project,
                                                          dataHolder: UserDataHolder,
                                                          private val proximity: VueModelVisitor.Proximity,
                                                          key: K)
  : WebSymbolsContainerWithCache<UserDataHolder, K>(VueFramework.ID, project, dataHolder, key) {

  companion object {

    fun create(container: VueEntitiesContainer, proximity: VueModelVisitor.Proximity): VueCodeModelSymbolsContainer<*>? {
      container.source
        ?.let {
          return VueCodeModelSymbolsContainer(container, it.project, it, proximity, proximity)
        }
      return if (container is VueGlobal)
        VueCodeModelSymbolsContainer(container, container.project, container.project, proximity, container.packageJsonUrl ?: "")
      else null
    }

  }

  override fun toString(): String {
    return "EntityContainerWrapper($container)"
  }

  override fun createPointer(): Pointer<VueCodeModelSymbolsContainer<K>> {
    val containerPtr = container.createPointer()
    val dataHolderPtr = dataHolder.let { if (it is Project) Pointer.hardPointer(it) else (it as PsiElement).createSmartPointer() }
    val project = this.project
    val proximity = this.proximity
    val key = this.key
    return Pointer {
      val container = containerPtr.dereference() ?: return@Pointer null
      val dataHolder = dataHolderPtr.dereference() ?: return@Pointer null
      VueCodeModelSymbolsContainer(container, project, dataHolder, proximity, key)
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(project).modificationCount +
    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS.modificationCount

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    visitContainer(container, proximity, consumer)
    if (container is VueGlobal) {
      visitContainer(container.unregistered, VueModelVisitor.Proximity.OUT_OF_SCOPE, consumer)
    }
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  private fun visitContainer(container: VueEntitiesContainer, forcedProximity: VueModelVisitor.Proximity, consumer: (WebSymbol) -> Unit) {
    container.acceptEntities(object : VueModelVisitor() {

      override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
        component.asWebSymbol(name, forcedProximity)?.let(consumer)
        return true
      }

      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        directive.asWebSymbol(name, forcedProximity)?.let(consumer)
        return true
      }

    }, VueModelVisitor.Proximity.LOCAL)
  }

}