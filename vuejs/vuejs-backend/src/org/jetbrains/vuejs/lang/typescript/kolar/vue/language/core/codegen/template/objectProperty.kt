// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCamelized
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateStringLiteralKey
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRegex
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize

fun generateObjectProperty(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  code: String,
  offset: Int,
  features: VueCodeInformation,
  shouldCamelize: Boolean = false,
  shouldBeConstant: Boolean = false,
): Sequence<Code> = sequence {
  if (code.startsWith("[") && code.endsWith("]")) {
    if (shouldBeConstant) {
      yieldAll(generateInterpolation(
        options,
        ctx,
        options.template,
        features,
        code.substring(1, code.length - 1),
        offset + 1,
        "[${names.tryAsConstant}(",
        ")]",
      ))
    }
    else {
      yieldAll(generateInterpolation(options, ctx, options.template, features, code, offset))
    }
  }
  else if (shouldCamelize) {
    if (identifierRegex.matches(camelize(code))) {
      yieldAll(generateCamelized(code, "template", offset, features))
    }
    else {
      val boundary = yield(Boundary.start("template", offset, features))
      yield("'")
      yieldAll(generateCamelized(code, "template", offset, boundary.features))
      yield("'")
      yield(boundary.end(offset + code.length))
    }
  }
  else {
    if (identifierRegex.matches(code)) {
      yield(DataSegment(text = code, source = "template", sourceOffset = offset, data = features))
    }
    else {
      yieldAll(generateStringLiteralKey(code, offset, features))
    }
  }
}
