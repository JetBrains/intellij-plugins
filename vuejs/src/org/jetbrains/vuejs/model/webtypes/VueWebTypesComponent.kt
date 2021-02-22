// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSAnyType
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.json.HtmlTag
import java.util.*

internal class VueWebTypesComponent(tag: HtmlTag, context: VueWebTypesEntitiesContainer.WebTypesContext)
  : VueWebTypesSourceEntity(tag, context), VueRegularComponent {

  override val global: VueGlobal? get() = context.parent.global
  override val parents: List<VueEntitiesContainer> get() = listOf(context.parent)

  override val data: List<VueDataProperty> = Collections.emptyList()
  override val computed: List<VueComputedProperty> = Collections.emptyList()
  override val methods: List<VueMethod> = Collections.emptyList()
  override val props: List<VueInputProperty> = tag.attributes.asSequence()
    .filter { it.name != null }
    .map { VueWebTypesInputProperty(it, context) }
    .toList()

  override val emits: List<VueEmitCall> = tag.events.asSequence()
    .filter { it.name != null }
    .map { VueWebTypesEmitCall(it, context) }
    .toList()

  override val slots: List<VueSlot> = tag.slots.asSequence()
    .plus(tag.vueScopedSlots)
    .filter { it.name != null }
    .map { VueWebTypesSlot(it, context) }
    .toList()

  override val extends: List<VueContainer> = emptyList()
  override val components: Map<String, VueComponent> = Collections.emptyMap()
  override val directives: Map<String, VueDirective> = Collections.emptyMap()
  override val filters: Map<String, VueFilter> = Collections.emptyMap()
  override val mixins: List<VueMixin> = Collections.emptyList()
  override val model: VueModelDirectiveProperties =
    tag.vueModel?.let { VueModelDirectiveProperties(it.prop, it.event) }
    ?: VueModelDirectiveProperties()
  override val defaultName: String = tag.name!!

  override val thisType: JSType
    get() = if (source != null)
      super.thisType
    else
      (context.packageJsonFile?.let { getDefaultVueComponentInstanceType(it) } ?: JSAnyType.get(source, false))

}
