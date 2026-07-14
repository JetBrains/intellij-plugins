// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.IfBranchNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

class IfBranchNodeImpl(
  private val tag: XmlTag,
  private val directiveName: String,
) : IfBranchNode {
  override val loc: SourceLocation
    get() = PsiSourceLocation(tag)

  override val condition: ExpressionNode
    get() = TODO("not implemented")

  override val children: List<Node> by lazy {
    if (tag.localName == TEMPLATE_TAG_NAME)
      getChildren(tag)
    else
      listOf(ElementNodeImpl(tag))
  }
}
