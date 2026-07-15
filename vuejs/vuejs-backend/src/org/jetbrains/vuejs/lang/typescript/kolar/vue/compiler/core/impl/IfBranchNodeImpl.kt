// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.IfBranchNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node

class IfBranchNodeImpl(
  private val tag: XmlTag,
  private val directiveName: String,
) : NodeImpl(tag),
    IfBranchNode {
  override val condition: ExpressionNode? by lazy {
    // TODO: support compound expressions
    tag.getAttribute(directiveName)
      ?.valueElement
      ?.let { SimpleExpressionNodeImpl(it) }
  }

  override val children: List<Node> by lazy {
    if (tag.localName == TEMPLATE_TAG_NAME)
      getChildren(tag)
    else
      listOf(ElementNodeImpl(tag))
  }
}
