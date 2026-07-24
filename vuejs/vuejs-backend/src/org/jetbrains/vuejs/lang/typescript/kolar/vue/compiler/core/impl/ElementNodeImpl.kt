// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.util.startOffset
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlTagUtil.getEndTagNameElement
import com.intellij.xml.util.XmlTagUtil.getStartTagNameElement
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveInfo
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
    buildList {
      for (attribute in element.attributes) {
        val name = attribute.name
        if (name in STRUCTURAL_DIRECTIVE_NAMES)
          continue

        val info = VueAttributeNameParser.parse(attribute.name, element)
        val prop = if (info is VueDirectiveInfo) {
          DirectiveNodeImpl(attribute, info)
        }
        else {
          AttributeNodeImpl(attribute)
        }

        add(prop)
      }
    }
  }

  companion object {
    fun getNameElementsOffsets(
      node: ElementNode,
    ): List<Int> {
      node as ElementNodeImpl

      val startName = getStartTagNameElement(node.element)
      requireNotNull(startName)

      val endName = getEndTagNameElement(node.element)
                    ?: return listOf(startName.startOffset)

      return listOfNotNull(
        startName.startOffset,
        endName.startOffset,
      )
    }
  }
}
