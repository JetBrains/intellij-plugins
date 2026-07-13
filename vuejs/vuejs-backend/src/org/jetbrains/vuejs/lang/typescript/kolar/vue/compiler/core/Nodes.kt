// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

// CompilerDOM.Node — base for all AST nodes
interface Node {
  val loc: SourceLocation
}

// CompilerDOM.CommentNode
interface CommentNode : Node {
  val content: String
}

// CompilerDOM.TextNode
interface TextNode : Node {
  val content: String
}

// CompilerDOM.ElementTypes
enum class ElementTypes {
  ELEMENT,   // 0
  COMPONENT, // 1
  TEMPLATE,  // 2
  SLOT,      // 3

  ;
}

// CompilerDOM.ConstantTypes
enum class ConstantTypes {
  NOT_CONSTANT,   // 0
  CAN_SKIP_PATCH, // 1
  CAN_HOIST,      // 2
  CAN_STRINGIFY,  // 3

  ;
}

// CompilerDOM.AttributeNode
interface AttributeNode : Node {
  val name: String
  val value: TextNode?
}

// CompilerDOM.ElementNode (minimal — full definition to be added when element.ts is converted)
interface ElementNode : Node {
  val tag: String
  val tagType: ElementTypes
  val isSelfClosing: Boolean
  val children: List<Node>
  val props: List<Node>
}

// CompilerDOM.ExpressionNode — sealed marker for SimpleExpressionNode | CompoundExpressionNode
sealed interface ExpressionNode : Node

// CompilerDOM.SimpleExpressionNode
interface SimpleExpressionNode : ExpressionNode {
  val content: String
  val isStatic: Boolean
  val constType: ConstantTypes
}

// CompilerDOM.DirectiveNode
interface DirectiveNode : Node {
  val name: String
  val arg: ExpressionNode?
  val exp: ExpressionNode?
  val rawName: String?
  val modifiers: List<SimpleExpressionNode>
}

// CompilerDOM.ForParseResult
data class ForParseResult(
  val source: ExpressionNode,
  val value: ExpressionNode?,
  val key: ExpressionNode?,
  val index: ExpressionNode?,
)

// CompilerDOM.ForNode
interface ForNode : Node {
  val parseResult: ForParseResult
  val children: List<Node>
}

// CompilerDOM.IfBranchNode
interface IfBranchNode : Node {
  val condition: ExpressionNode?
  val children: List<Node>
}

// CompilerDOM.IfNode
interface IfNode : Node {
  val branches: List<IfBranchNode>
}

// CompilerDOM.RootNode
interface RootNode : Node {
  val children: List<Node>
  val components: List<String>
}

// CompilerDOM.InterpolationNode
interface InterpolationNode : Node {
  val content: ExpressionNode
}

// CompilerDOM.CompoundExpressionNode
interface CompoundExpressionNode : ExpressionNode {
  val children: List<Any /* Node | string | symbol */>
}
