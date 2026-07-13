// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

import com.intellij.psi.xml.XmlComment
import com.intellij.psi.xml.XmlTag

// CompilerDOM.RootNode
interface RootNode : Node {
  val children: List<Node>
  val components: List<String>
}

class RootNodeImpl(
  private val template: XmlTag,
) : RootNode {
  override val loc: SourceLocation
    get() = PsiSourceLocation(template)

  override val children: List<Node> by lazy {
    template.children.mapNotNull { child -> 
      when (child) {
        is XmlTag -> ElementNodeImpl(child)
        is XmlComment -> CommentNodeImpl(child)
        else -> null
      }
    }
  }

  override val components: List<String> =
    // TBD
    emptyList()
}
