// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

// CompilerDOM.Node — base for all AST nodes
interface Node {
  val type: NodeTypes
  val loc: SourceLocation
}

// CompilerDOM.CommentNode
data class CommentNode(
  override val type: NodeTypes,  // NodeTypes.COMMENT
  val content: String,
  override val loc: SourceLocation,
) : Node

// CompilerDOM.ElementNode (minimal — full definition to be added when element.ts is converted)
interface ElementNode : Node {
  val children: List<Node>
}

// CompilerDOM.SimpleExpressionNode
interface SimpleExpressionNode : Node {
  val content: String
  val isStatic: Boolean
}

// CompilerDOM.DirectiveNode
interface DirectiveNode : Node {
  val arg: Node?
  val exp: Node?
  val rawName: String?
}

// CompilerDOM.ForParseResult
data class ForParseResult(
  val source: SimpleExpressionNode,
  val value: Node?,
  val key: Node?,
  val index: Node?,
)

// CompilerDOM.ForNode
interface ForNode : Node {
  val parseResult: ForParseResult
  val children: List<Node>
}

// CompilerDOM.IfBranchNode
interface IfBranchNode : Node {
  val condition: Node?
  val children: List<Node>
}

// CompilerDOM.IfNode
interface IfNode : Node {
  val branches: List<IfBranchNode>
}
