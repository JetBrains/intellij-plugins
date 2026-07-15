// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.RootNode

class RootNodeImpl(
  private val template: XmlTag,
) : NodeImpl(template),
    RootNode {
  override val children: List<Node> by children(template, ParentScope.ELEMENT)

  override val components: List<String> =
    emptyList() // TBD
}
