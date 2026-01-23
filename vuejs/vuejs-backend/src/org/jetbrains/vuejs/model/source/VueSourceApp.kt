// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueApp
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueLibrary
import org.jetbrains.vuejs.model.VueModelVisitor

class VueSourceApp(
  declaration: JSObjectLiteralExpression,
) : VueSourceContainer<JSObjectLiteralExpression>(declaration, declaration, null),
    VueApp {

  override fun getProximity(library: VueLibrary): VueModelVisitor.Proximity =
    library.defaultProximity

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueSourceApp(source)
    }
  }

}
