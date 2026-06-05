// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.CompilerError

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
  val start: Int
  val end: Int
  val startTagEnd: Int
  val endTagStart: Int
  val lang: String
  val content: String
  val attrs: Map<String, Any>  // values are String or Boolean (true)
}

// Top-level Vue SFC IR
data class IR(
  val content: String,
  val comments: List<String>,
  val template: IRTemplate?,
  val script: IRScript?,
  val scriptSetup: IRScriptSetup?,
  val styles: List<IRStyle>,
  val customBlocks: List<IRCustomBlock>,
)

data class IRTemplate(
  override val name: String,
  override val start: Int,
  override val end: Int,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  override val attrs: Map<String, Any>,
  val ast: Any?,           // CompilerDOM.RootNode — opaque TypeScript AST
  val errors: List<CompilerError>,
  val warnings: List<CompilerError>,
) : IRBlock

data class IRScript(
  override val name: String,
  override val start: Int,
  override val end: Int,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  override val attrs: Map<String, Any>,
  val src: IRAttr?,
  val ast: Any?,  // ts.SourceFile — opaque TypeScript AST
) : IRBlock

data class IRScriptSetup(
  override val name: String,
  override val start: Int,
  override val end: Int,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  override val attrs: Map<String, Any>,
  val generic: IRAttr?,
  val ast: Any?,  // ts.SourceFile — opaque TypeScript AST
) : IRBlock

data class IRStyle(
  override val name: String,
  override val start: Int,
  override val end: Int,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  override val attrs: Map<String, Any>,
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

data class IRCustomBlock(
  override val name: String,
  override val start: Int,
  override val end: Int,
  override val startTagEnd: Int,
  override val endTagStart: Int,
  override val lang: String,
  override val content: String,
  override val attrs: Map<String, Any>,
  val type: String,
) : IRBlock
