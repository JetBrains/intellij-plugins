// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueMixin

class VueSourceMixin(source: JSImplicitElement, descriptor: VueSourceEntityDescriptor)
  : VueSourceContainer(source, descriptor), VueMixin {

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val sourcePtr = (source as JSImplicitElement).createSmartPointer()
    val descriptorPtr = descriptor.createPointer()
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      val descriptor = descriptorPtr.dereference() ?: return@Pointer null
      VueSourceMixin(source, descriptor)
    }
  }

}
