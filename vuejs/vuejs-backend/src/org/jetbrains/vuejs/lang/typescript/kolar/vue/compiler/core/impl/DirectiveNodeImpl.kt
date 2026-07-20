// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.util.startOffset
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElement
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ConstantTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

class DirectiveNodeImpl(
  private val attribute: XmlAttribute,
  private val info: VueDirectiveInfo,
) : NodeImpl(attribute),
    DirectiveNode {
  override val name: String
    get() = info.name

  override val arg: ExpressionNode? by lazy {
    val argument = info.arguments ?: return@lazy null
    val nameElement = attribute.nameElement ?: return@lazy null

    DirectiveArgExpressionNode(nameElement, argument)
  }

  override val exp: ExpressionNode? by lazy {
    attribute.valueElement
      ?.let(::XmlValueExpressionNodeImpl)
  }

  override val rawName: String
    get() = attribute.name

  override val modifiers: List<SimpleExpressionNode> by lazy {
    val nameElement = attribute.nameElement ?: return@lazy emptyList()

    info.modifiers.map { modifierName ->
      DirectiveModifierExpressionNode(nameElement, modifierName)
    }
  }
}

private class DirectiveArgExpressionNode(
  nameElement: XmlElement,
  private val argument: String,
) : SimpleExpressionNode {
  override val loc: SourceLocation by lazy {
    // naive implementation
    val startOffset = nameElement.startOffset + nameElement.text.indexOf(":$argument") + 1
    val endOffset = startOffset + argument.length
    val dynamicCorrection = if (isStatic) 0 else 1
    SourceLocationImpl(
      startOffset = startOffset + dynamicCorrection,
      endOffset = endOffset - dynamicCorrection,
      source = content,
    )
  }

  override val content: String by lazy {
    argument.removeSurrounding("[", "]")
  }

  override val isStatic: Boolean = !argument.startsWith("[")
  override val constType: ConstantTypes = ConstantTypes.NOT_CONSTANT
}

private class DirectiveModifierExpressionNode(
  nameElement: XmlElement,
  private val modifierName: String,
) : SimpleExpressionNode {
  override val loc: SourceLocation by lazy {
    // naive implementation
    val startOffset = nameElement.startOffset + nameElement.text.indexOf(".$modifierName") + 1
    SourceLocationImpl(
      startOffset = startOffset,
      endOffset = startOffset + modifierName.length,
      source = modifierName,
    )
  }

  override val content: String
    get() = modifierName

  override val isStatic: Boolean = true
  override val constType: ConstantTypes = ConstantTypes.NOT_CONSTANT
}
