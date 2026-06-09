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
interface ElementNode : Node
