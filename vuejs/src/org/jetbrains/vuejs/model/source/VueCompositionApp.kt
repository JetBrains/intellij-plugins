// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.model.Pointer
import com.intellij.psi.PsiQualifiedReference
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueComponents.getComponentDescriptor

data class VueCompositionApp(
  override val source: JSCallExpression,
) : VueCompositionContainer(),
    VueApp {

  override val mode: VueMode
    get() {
      val vapor = source.methodExpression
        ?.asSafely<PsiQualifiedReference>()
        ?.referenceName == CREATE_VAPOR_APP_FUN
      
      return if (vapor) VueMode.VAPOR else VueMode.CLASSIC
    }

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
            val container = getComponentDescriptor(initializer)?.let {
              VueModelManager.getComponent(it)
            } as? VueContainer
            CachedValueProvider.Result.create(container, PsiModificationTracker.MODIFICATION_COUNT)
          }
      }

  override fun getProximity(library: VueLibrary): VueModelVisitor.Proximity =
    library.defaultProximity

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val initializerPtr = source.createSmartPointer()
    return Pointer {
      initializerPtr.dereference()?.let { VueCompositionApp(it) }
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
