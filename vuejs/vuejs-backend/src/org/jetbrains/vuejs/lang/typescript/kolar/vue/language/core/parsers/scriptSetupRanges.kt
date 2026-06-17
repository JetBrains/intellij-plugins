// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers

import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange

// CallExpressionRange { callExp, exp, arg?, typeArg? }
// Base for DefineProps, DefineEmits, DefineSlots, UseTemplateRef; also used standalone
interface CallExpressionRange {
  val callExp: TextRange<*>
  val exp: TextRange<*>
  val arg: TextRange<*>?
  val typeArg: TextRange<*>?
}

// Concrete base implementation for standalone usages (withDefaults, defineExpose, useAttrs, useCssModule, useSlots)
data class CallExpression(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
) : CallExpressionRange

data class DefineModel(
  val arg: TextRange<*>?,
  val localName: TextRange<*>?,
  val name: TextRange<*>?,
  val type: TextRange<*>?,
  val modifierType: TextRange<*>?,
  val runtimeType: TextRange<*>?,
  val defaultValue: TextRange<*>?,
  val required: Boolean?,
  val comments: TextRange<*>?,
)

// DefineProps extends CallExpressionRange
data class DefineProps(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
  val name: String?,
  val destructured: Map<String, *>?,  // Map<string, ts.Expression | undefined>
  val destructuredRest: String?,
  val statement: TextRange<*>,
) : CallExpressionRange

// DefineEmits extends CallExpressionRange
data class DefineEmits(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
  val name: String?,
  val hasUnionTypeArg: Boolean?,
  val statement: TextRange<*>,
) : CallExpressionRange

// DefineSlots extends CallExpressionRange
data class DefineSlots(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
  val name: String?,
  val statement: TextRange<*>,
) : CallExpressionRange

data class DefineOptions(
  val name: String?,
  val inheritAttrs: String?,
)

// UseTemplateRef extends CallExpressionRange
data class UseTemplateRef(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
  val name: String?,
) : CallExpressionRange

// ScriptSetupRanges — structural equivalent of ReturnType<typeof parseScriptSetupRanges>
data class ScriptSetupRanges(
  val leadingCommentEndOffset: Int,
  val importSectionEndOffset: Int,
  val bindings: List<TextRange<*>>,
  val components: List<TextRange<*>>,
  val defineModel: List<DefineModel>,
  val defineProps: DefineProps?,
  val withDefaults: CallExpression?,
  val defineEmits: DefineEmits?,
  val defineSlots: DefineSlots?,
  val defineExpose: CallExpression?,
  val defineOptions: DefineOptions?,
  val useAttrs: List<CallExpression>,
  val useCssModule: List<CallExpression>,
  val useSlots: List<CallExpression>,
  val useTemplateRef: List<UseTemplateRef>,
)
