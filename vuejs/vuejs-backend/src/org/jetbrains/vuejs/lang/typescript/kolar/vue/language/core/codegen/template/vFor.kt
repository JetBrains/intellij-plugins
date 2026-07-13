// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingNames
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

private data class VForParseResult(
  val leftExpressionRange: OffsetRange?,
  val leftExpressionText: String?,
) {
  data class OffsetRange(
    val start: Int,
    val end: Int,
  )
}

fun generateVFor(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ForNode,
): Sequence<Code> = sequence {
  val source = node.parseResult.source
  val (leftExpressionRange, leftExpressionText) = parseVForNode(node)
  val scope = ctx.scope()

  yield("for (const [")
  if (leftExpressionRange != null && leftExpressionText != null) {
    val collectAst = getTypeScriptAST("const [$leftExpressionText]")
    scope.declare(collectBindingNames(collectAst))
    yield(DataSegment(
      text = leftExpressionText,
      source = Source("template"),
      sourceOffset = leftExpressionRange.start,
      data = codeFeatures.all,
    ))
  }
  yield("] of ")
  if (source is SimpleExpressionNode) {
    yield("${names.vFor}(")
    yieldAll(generateInterpolation(
      options = options,
      ctx = ctx,
      block = options.template,
      data = codeFeatures.all,
      code = source.content,
      start = source.loc.startOffset,
      prefix = "(",
      suffix = ")",
    ))
    yield("!)") // #3102
  }
  else {
    yield("{} as any")
  }
  yield(") {$newLine")

  val inVFor = ctx.inVFor
  ctx.inVFor = true
  for (child in node.children) {
    yieldAll(generateTemplateChild(
      options = options,
      ctx = ctx,
      node = child,
      enterNode = false,
      treatTemplateAsFragment = true,
    ))
  }
  ctx.inVFor = inVFor

  yieldAll(scope.end())
  yield("}$newLine")
}

private fun parseVForNode(
  node: ForNode,
): VForParseResult {
  val result = node.parseResult
  val leftExpressionRange = if (result.value != null || result.key != null || result.index != null) {
    VForParseResult.OffsetRange(
      start = (result.value ?: result.key ?: result.index)!!.loc.startOffset,
      end = (result.index ?: result.key ?: result.value)!!.loc.endOffset,
    )
  }
  else null
  val leftExpressionText = if (leftExpressionRange != null) {
    node.loc.source.substring(
      leftExpressionRange.start - node.loc.startOffset,
      leftExpressionRange.end - node.loc.startOffset,
    )
  }
  else null
  return VForParseResult(leftExpressionRange, leftExpressionText)
}
