// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.sfc

import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.CompilerError
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

data class ImportBinding(
  val isType: Boolean,
  val imported: String,
  val local: String,
  val source: String,
  val isFromSetup: Boolean,
  val isUsedInTemplate: Boolean,
)

// SFCBlock { type: string; content: string; attrs: Record<string, string | true>; loc: SourceLocation; ... }
sealed interface SFCBlock {
  val type: String
  val content: String
  val attrs: Map<String, Any>  // values are String or Boolean (true)
  val loc: SourceLocation
  val map: Any?       // RawSourceMap — opaque
  val lang: String?
  val src: String?

  // Represents custom/unrecognized block types (SFCDescriptor.customBlocks)
  data class Custom(
    override val type: String,
    override val content: String,
    override val attrs: Map<String, Any>,
    override val loc: SourceLocation,
    override val map: Any?,
    override val lang: String?,
    override val src: String?,
  ) : SFCBlock
}

// SFCTemplateBlock extends SFCBlock { type: 'template'; ast?: RootNode }
data class SFCTemplateBlock(
  override val type: String,
  override val content: String,
  override val attrs: Map<String, Any>,
  override val loc: SourceLocation,
  override val map: Any?,
  override val lang: String?,
  override val src: String?,
  val ast: Any?,  // RootNode — opaque TypeScript AST
) : SFCBlock

// SFCScriptBlock extends SFCBlock { type: 'script'; setup?: string | boolean; ... }
data class SFCScriptBlock(
  override val type: String,
  override val content: String,
  override val attrs: Map<String, Any>,
  override val loc: SourceLocation,
  override val map: Any?,
  override val lang: String?,
  override val src: String?,
  val setup: Any?,                    // string | boolean
  val bindings: Any?,                 // BindingMetadata — opaque
  val imports: Map<String, ImportBinding>?,
  val scriptAst: List<Any>?,          // _babel_types.Statement[]
  val scriptSetupAst: List<Any>?,     // _babel_types.Statement[]
  val warnings: List<String>?,
  val deps: List<String>?,
) : SFCBlock

// SFCStyleBlock extends SFCBlock { type: 'style'; scoped?: boolean; module?: string | boolean }
data class SFCStyleBlock(
  override val type: String,
  override val content: String,
  override val attrs: Map<String, Any>,
  override val loc: SourceLocation,
  override val map: Any?,
  override val lang: String?,
  override val src: String?,
  val scoped: Boolean?,
  val module: Any?,  // string | boolean
) : SFCBlock

data class SFCDescriptor(
  val filename: String,
  val source: String,
  val template: SFCTemplateBlock?,
  val script: SFCScriptBlock?,
  val scriptSetup: SFCScriptBlock?,
  val styles: List<SFCStyleBlock>,
  val customBlocks: List<SFCBlock>,
  val cssVars: List<String>,
  val slotted: Boolean,
  // shouldForceReload omitted — HMR optimization callback, not serializable data
)

// SFCParseResult { descriptor: SFCDescriptor; errors: (CompilerError | SyntaxError)[] }
data class SFCParseResult(
  val descriptor: SFCDescriptor,
  val errors: List<CompilerError>,  // SyntaxError covered by CompilerError.Generic
)
