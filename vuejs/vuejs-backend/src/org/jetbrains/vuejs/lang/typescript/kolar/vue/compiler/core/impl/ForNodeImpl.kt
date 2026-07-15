// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForParseResult
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node

class ForNodeImpl(
  tag: XmlTag,
) : NodeImpl(tag),
    ForNode {
  override val parseResult: ForParseResult by lazy {
    ForParseResultImpl(tag.getAttribute(V_FOR)!!)
  }

  override val children: List<Node> by children(tag, ParentScope.FOR)
}
