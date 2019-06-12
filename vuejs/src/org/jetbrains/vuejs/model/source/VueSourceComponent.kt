// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.index.VueIndexData
import org.jetbrains.vuejs.model.*

class VueSourceComponent(element: PsiElement, data: VueIndexData?) : VueRegularComponent {

  override val global: VueGlobal?
    get() {
      return VueModelManager.getGlobal(source)
    }
  override val source: PsiElement = element
  override val defaultName: String? = data?.originalName

  override val applications: List<VueApp> = emptyList()
  override val data: List<VueDataProperty> = emptyList()
  override val computed: List<VueComputedProperty> = emptyList()
  override val methods: List<VueMethod> = emptyList()
  override val props: List<VueInputProperty> = emptyList()
  override val emits: List<VueEmitCall> = emptyList()
  override val slots: List<VueSlot> = emptyList()
  override val template: PsiElement? = null
  override val element: String? = null
  override val extends: Any? = null
  override val components: Map<String, VueComponent> = emptyMap()
  override val directives: Map<String, VueDirective> = emptyMap()
  override val filters: Map<String, VueFilter> = emptyMap()
  override val mixins: List<VueMixin> = emptyList()


}
