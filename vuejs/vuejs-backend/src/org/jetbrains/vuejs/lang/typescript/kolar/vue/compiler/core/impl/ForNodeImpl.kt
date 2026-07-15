// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForParseResult
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

class ForNodeImpl(
  private val tag: XmlTag,
) : ForNode {
  override val loc: SourceLocation
    get() = PsiSourceLocation(tag)

  override val parseResult: ForParseResult
    get() = TODO("not implemented")

  override val children: List<Node> by children(tag)
}
