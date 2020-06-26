// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.javascript.psi.JSType
import org.jetbrains.vuejs.codeInsight.ATTR_DIRECTIVE_PREFIX
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueDirectiveArgument
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.webtypes.json.HtmlAttribute

internal class VueWebTypesDirective(attribute: HtmlAttribute,
                                    context: VueWebTypesEntitiesContainer.WebTypesContext)
  : VueWebTypesSourceEntity(attribute, context), VueDirective {

  init {
    assert(attribute.name!!.startsWith(ATTR_DIRECTIVE_PREFIX)) {
      attribute.name!!
    }
  }

  override val parents: List<VueEntitiesContainer> = listOf(context.parent)
  override val defaultName: String? = attribute.name!!.substring(2)
  override val acceptsValue: Boolean = (attribute.value as? Map<*, *>)?.get("kind") != "no-value"
  override val acceptsNoValue: Boolean = !acceptsValue || (attribute.value as? Map<*, *>)?.get("required") != true
  override val jsType: JSType? = context.getType(attribute.value)
  override val modifiers: List<VueDirectiveModifier> = attribute.vueModifiers.asSequence()
    .filter { it.name != null }
    .map { VueWebTypesDirectiveModifier(it, context) }
    .toList()
  override val argument: VueDirectiveArgument? = attribute.vueArgument?.let { VueWebTypesDirectiveArgument(it, context) }
}
