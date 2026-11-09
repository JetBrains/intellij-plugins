// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.toString
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.IfNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateVIf(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: IfNode,
): Sequence<Code> = sequence {
  val originalBlockConditionsLength = ctx.conditions.size

  for ((i, branch) in node.branches.withIndex()) {
    if (i == 0) {
      yield("if ")
    }
    else if (branch.condition != null) {
      yield("else if ")
    }
    else {
      yield("else ")
    }

    var addedBlockCondition = false

    val condition = branch.condition
    if (condition is SimpleExpressionNode) {
      val accessMark = ctx.accessLog.size
      val codes = generateInterpolation(
        options = options,
        ctx = ctx,
        block = options.template,
        data = codeFeatures.all,
        code = condition.content,
        start = condition.loc.startOffset,
        prefix = "(",
        suffix = ")",
        inNarrowing = true,
      ).toList()
      yieldAll(codes)
      ctx.conditions.add(TemplateCodegenContext.Condition(
        text = toString(codes),
        accesses = ctx.accessLog.subList(accessMark, ctx.accessLog.size).toList(),
      ))
      addedBlockCondition = true
      yield(" ")
    }

    yield("{$newLine")
    for (child in branch.children) {
      yieldAll(generateTemplateChild(options, ctx, child, enterNode = i != 0, treatTemplateAsFragment = true))
    }
    yield("}$newLine")

    if (addedBlockCondition) {
      val condition = ctx.conditions.last()
      condition.text = "!${condition.text}"
    }
  }

  ctx.conditions.subList(originalBlockConditionsLength, ctx.conditions.size).clear()
}
