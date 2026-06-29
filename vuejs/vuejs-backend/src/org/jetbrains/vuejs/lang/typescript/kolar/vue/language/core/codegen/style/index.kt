// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.createTemplateCodegenContext
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.generateInterpolation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

class StyleGenerateResult(
  val codes: List<Code>,
  val ctx: TemplateCodegenContext,
)

fun generateStyle(
  options: StyleCodegenOptions,
): StyleGenerateResult {
  val ctx = createTemplateCodegenContext()
  val codes = mutableListOf<Code>()
  for (code in generateWorker(options, ctx)) {
    codes.add(
      if (code is DataSegment<VueCodeInformation>)
        DataSegment(
          text = code.text,
          source = code.source,
          sourceOffset = code.sourceOffset,
          data = ctx.resolveCodeFeatures(code.data),
        )
      else code
    )
  }
  return StyleGenerateResult(codes = codes, ctx = ctx)
}

private fun generateWorker(
  options: StyleCodegenOptions,
  ctx: TemplateCodegenContext,
): Sequence<Code> = sequence {
  val scope = ctx.scope()
  scope.declare(options.setupConsts.toList())
  yieldAll(generateStyleScopedClasses(options))
  yieldAll(generateStyleModules(options, ctx))
  yieldAll(generateCssVars(options, ctx))
  yieldAll(scope.end())
}

private fun generateCssVars(
  options: StyleCodegenOptions,
  ctx: TemplateCodegenContext,
): Sequence<Code> = sequence {
  for (style in options.styles) {
    for (binding in style.bindings) {
      yieldAll(generateInterpolation(
        options,
        ctx,
        style,
        codeFeatures.all,
        binding.text,
        binding.offset,
        "(",
        ")",
      ))
      yield(endOfLine)
    }
  }
}
