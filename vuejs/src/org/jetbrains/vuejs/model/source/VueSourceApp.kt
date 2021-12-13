// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.model.VueApp
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VuePlugin

class VueSourceApp(source: JSImplicitElement, declaration: JSObjectLiteralExpression)
  : VueSourceContainer(source, VueSourceEntityDescriptor(declaration)), VueApp {

  override fun getProximity(plugin: VuePlugin): VueModelVisitor.Proximity =
    plugin.defaultProximity

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val sourcePtr = (source as JSImplicitElement).createSmartPointer()
    val declarationPtr = (descriptor.initializer as JSObjectLiteralExpression).createSmartPointer()
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      val declaration = declarationPtr.dereference() ?: return@Pointer null
      VueSourceApp(source, declaration)
    }
  }

}
