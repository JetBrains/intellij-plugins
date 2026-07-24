// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
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
  val name: Source
  val lang: String
  val content: IRContent
}

// IRScript | IRScriptSetup — blocks that carry a TypeScript AST
sealed interface IRScriptBlock : IRBlock {
  val ast: JSEmbeddedContent
}

// Top-level Vue SFC IR
data class IR(
  val template: IRTemplate?,
  val script: IRScript?,
  val scriptSetup: IRScriptSetup?,
  val styles: List<IRStyle>,
)

data class IRTemplate(
  override val name: Source,
  override val lang: String,
  override val content: IRContent,
  val ast: RootNode?,
) : IRBlock

data class IRScript(
  override val name: Source,
  override val lang: String,
  override val content: IRContent,
  val src: IRAttr?,
  override val ast: JSEmbeddedContent,
) : IRScriptBlock

data class IRScriptSetup(
  override val name: Source,
  override val lang: String,
  override val content: IRContent,
  val generic: IRAttr?,
  override val ast: JSEmbeddedContent,
) : IRScriptBlock

data class IRStyle(
  override val name: Source,
  override val lang: String,
  override val content: IRContent,
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

interface IRContent {
  val startOffset: Int
  val endOffset: Int
  fun indexOf(string: String, startIndex: Int): Int
  fun substring(startIndex: Int, endIndex: Int): String
}
