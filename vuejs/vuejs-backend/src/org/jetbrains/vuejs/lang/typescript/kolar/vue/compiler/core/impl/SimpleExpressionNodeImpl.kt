// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.psi.xml.XmlAttributeValue
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ConstantTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

class SimpleExpressionNodeImpl(
  private val value: JSElement,
) : NodeImpl(value),
    DefaultSimpleExpressionNode {
  override val content: String
    get() = value.text
}

class XmlValueExpressionNodeImpl(
  private val value: XmlAttributeValue,
) : DefaultSimpleExpressionNode {
  override val loc: SourceLocation by lazy {
    AttributeValueSourceLocation(value)
  }

  override val content: String
    get() = value.value
}

interface DefaultSimpleExpressionNode :
  SimpleExpressionNode {
  override val isStatic: Boolean
    get() = false

  override val constType: ConstantTypes
    get() = ConstantTypes.NOT_CONSTANT
}
