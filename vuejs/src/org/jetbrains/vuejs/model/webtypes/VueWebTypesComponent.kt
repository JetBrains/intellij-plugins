// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.json.Tag
import java.util.*

class VueWebTypesComponent(tag: Tag) : VueRegularComponent {
  override val data: List<VueDataProperty>
    get() = Collections.emptyList()
  override val computed: List<VueComputedProperty>
    get() = Collections.emptyList()
  override val methods: List<VueMethod>
    get() = Collections.emptyList()
  override val props: List<VueInputProperty> = tag.attributes?.filter { it.name != null }?.map { VueWebTypesInputProperty(it) }
                                               ?: Collections.emptyList()
  override val emits: List<VueEmitCall> = tag.events?.filter { it.name != null }?.map { VueWebTypesEmitCall(it) }
                                          ?: Collections.emptyList()
  override val slots: List<VueSlot> = tag.slots?.filter { it.name != null }?.map { VueWebTypesSlot(it) }
                                      ?: Collections.emptyList()
  override val template: PsiElement?
    get() = null
  override val element: String?
    get() = null
  override val extends: Any?
    get() = null
  override val components: Map<String, VueComponent>
    get() = Collections.emptyMap()
  override val directives: Map<String, VueDirective>
    get() = Collections.emptyMap()
  override val filters: Map<String, VueFilter>
    get() = Collections.emptyMap()
  override val mixins: List<VueMixin>
    get() = Collections.emptyList()
  override val applications: List<VueApp>
    get() = Collections.emptyList()
  override val global: VueGlobal
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
  override val defaultName: String = tag.name!!

  override val source: PsiElement? = null

  private val sourceFile: String? = tag.sourceFile

}
