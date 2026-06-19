// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.RootNode

// TextRange<Node extends ts.Node = ts.Node> { node: Node; start: number; end: number }
data class TextRange<Node>(
  val node: Node,
  val start: Int,
  val end: Int,
)

// IRAttr = true | { text: string; offset: number }
sealed interface IRAttr {
  data object Present : IRAttr
  data class WithText(
    val text: String,
    val offset: Int,
  ) : IRAttr
}

// Base interface for all SFC block types
sealed interface IRBlock {
  val name: String
  val startTagEnd: Int
  val endTagStart: Int
  val lang: String
  val content: String
}

// IRScript | IRScriptSetup — blocks that carry a TypeScript AST
sealed interface IRScriptBlock : IRBlock {
  val ast: SourceFile
}

// Top-level Vue SFC IR
data class IR(
  val content: String,
  val comments: List<String>,
  val template: IRTemplate?,
  val script: IRScript?,
  val scriptSetup: IRScriptSetup?,
  val styles: List<IRStyle>,
)

data class IRTemplate(
  override val name: String,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  val ast: RootNode?,
) : IRBlock

data class IRScript(
  override val name: String,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  val src: IRAttr?,
  override val ast: SourceFile,
) : IRScriptBlock

data class IRScriptSetup(
  override val name: String,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  val generic: IRAttr?,
  override val ast: SourceFile,
) : IRScriptBlock

data class IRStyle(
  override val name: String,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  val src: IRAttr?,
  val module: IRAttr?,
  val scoped: Boolean,
  val imports: List<TextWithOffset>,
  val bindings: List<TextWithOffset>,
  val classNames: List<TextWithOffset>,
) : IRBlock {
  data class TextWithOffset(
    val text: String,
    val offset: Int,
  )
}
