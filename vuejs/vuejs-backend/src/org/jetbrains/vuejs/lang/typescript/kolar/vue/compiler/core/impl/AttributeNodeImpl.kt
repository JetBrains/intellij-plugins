// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.AttributeNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.TextNode

class AttributeNodeImpl(
  private val attribute: XmlAttribute,
) : NodeImpl(attribute),
    AttributeNode {

  override val name: String
    get() = attribute.name

  override val value: TextNode? by lazy {
    attribute.valueElement?.let(::AttributeValueNodeImpl)
  }
}

private class AttributeValueNodeImpl(
  private val element: XmlAttributeValue,
) : NodeImpl(element),
    TextNode {
  override val content: String
    get() = element.value
}
