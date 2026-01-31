// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.model.VueApp
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueGlobalImpl
import org.jetbrains.vuejs.model.VueLibrary
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VuePlugin
import org.jetbrains.vuejs.model.source.VueComponents.getComponent

data class VueCompositionApp(
  override val source: JSCallExpression,
  override val mode: VueMode,
) : VueCompositionContainer(mode),
    VueApp {

  override val rootComponent: VueComponent?
    get() = delegate.asSafely<VueComponent>()

  override val delegate: VueContainer?
    get() = getImplicitElement(source)
      ?.let { getParam(it, source, 0) }
      ?.let { initializer ->
        if (initializer is JSObjectLiteralExpression)
          VueModelManager.getApp(initializer)
        else
          CachedValuesManager.getCachedValue(initializer) {
            val container = getComponent(initializer)
            CachedValueProvider.Result.create(container, PsiModificationTracker.MODIFICATION_COUNT)
          }
      }

  override fun getProximity(library: VueLibrary): VueModelVisitor.Proximity =
    library.defaultProximity

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val sourcePointer = source.createSmartPointer()
    val mode = this.mode
    return Pointer {
      sourcePointer.dereference()
        ?.let { VueCompositionApp(it, mode) }
    }
  }

  override val parents: List<VueEntitiesContainer>
    get() = VueGlobalImpl.getParents(this)

  override fun pluginChain(): List<VuePlugin> =
    buildList {
      val visited = mutableSetOf<VuePlugin>()

      fun visit(plugins: List<VuePlugin>) {
        for (plugin in plugins) {
          if (!visited.add(plugin))
            continue

          visit(plugin.plugins)

          add(plugin)
        }
      }

      visit(plugins)
    }

  override fun toString(): String {
    return "VueCompositionApp($source)"
  }
}
