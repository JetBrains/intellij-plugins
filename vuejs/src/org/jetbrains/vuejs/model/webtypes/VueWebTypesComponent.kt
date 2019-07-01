// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.javascript.psi.JSType
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.json.Tag
import java.util.*

class VueWebTypesComponent(tag: Tag, private val parent: VueWebTypesPlugin, typeProvider: (Any?) -> JSType?) : VueRegularComponent {

  override val source: PsiElement? = null
  override val global: VueGlobal? get() = parent.global
  override val parents: List<VueEntitiesContainer> get() = listOf(parent)

  override val data: List<VueDataProperty> = Collections.emptyList()
  override val computed: List<VueComputedProperty> = Collections.emptyList()
  override val methods: List<VueMethod> = Collections.emptyList()
  override val props: List<VueInputProperty> = tag.attributes.filter { it.name != null }.map { VueWebTypesInputProperty(it, typeProvider) }
  override val emits: List<VueEmitCall> = tag.events.filter { it.name != null }.map { VueWebTypesEmitCall(it) }
  override val slots: List<VueSlot> = tag.slots.filter { it.name != null }.map { VueWebTypesSlot(it) }
  override val template: PsiElement? = null
  override val element: String? = null
  override val extends: List<VueContainer> = emptyList()
  override val components: Map<String, VueComponent> = Collections.emptyMap()
  override val directives: Map<String, VueDirective> = Collections.emptyMap()
  override val filters: Map<String, VueFilter> = Collections.emptyMap()
  override val mixins: List<VueMixin> = Collections.emptyList()
  override val defaultName: String = tag.name!!

  private val sourceFile: String? = tag.sourceFile

}
