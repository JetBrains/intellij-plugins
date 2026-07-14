// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

class DirectiveNodeImpl(
  private val attribute: XmlAttribute,
) : DirectiveNode {
  override val loc: SourceLocation
    get() = PsiSourceLocation(attribute)

  override val name: String
    get() = TODO("not implemented")

  override val arg: ExpressionNode?
    get() = null // TBD

  override val exp: ExpressionNode?
    get() = null // TBD

  override val rawName: String?
    get() = null // TBD

  override val modifiers: List<SimpleExpressionNode>
    get() = emptyList() // TBD
}