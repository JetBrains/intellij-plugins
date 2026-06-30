// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateStringLiteralKey
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRE
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generatePropertyAccess(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  code: String,
  offset: Int,
  features: VueCodeInformation,
): Sequence<Code> = sequence {
  if (code.startsWith("[") && code.endsWith("]")) {
    yieldAll(generateInterpolation(options, ctx, options.template, features, code, offset))
  }
  else if (identifierRE.matches(code)) {
    yield(".")
    yield(DataSegment(text = code, source = "template", sourceOffset = offset, data = features))
  }
  else {
    yield("[")
    yieldAll(generateStringLiteralKey(code, offset, features))
    yield("]")
  }
}
