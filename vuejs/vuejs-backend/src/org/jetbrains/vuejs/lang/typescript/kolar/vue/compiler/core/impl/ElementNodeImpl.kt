// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.dom.isNativeTag

class ElementNodeImpl(
  private val element: XmlTag,
) : NodeImpl(element),
    ElementNode {

  override val tag: String
    get() = element.localName

  override val tagType: ElementTypes by lazy {
    when {
      tag == TEMPLATE_TAG_NAME -> ElementTypes.TEMPLATE
      tag == SLOT_TAG_NAME -> ElementTypes.SLOT
      isNativeTag(tag) -> ElementTypes.ELEMENT
      else -> ElementTypes.COMPONENT
    }
  }

  override val isSelfClosing: Boolean
    get() = element.isEmpty

  override val children: List<Node> by children(element, ParentScope.ELEMENT)

  override val props: List<Node> by lazy {
    element.attributes.mapNotNull {
      val name = it.name

      when {
        name in STRUCTURAL_DIRECTIVE_NAMES
          -> null

        isDirectiveRawName(name) ->
          DirectiveNodeImpl(it)

        else -> AttributeNodeImpl(it)
      }
    }
  }
}
