// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRAttr
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRStyle
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateClassProperty(
  source: Source,
  classNameWithDot: String,
  offset: Int,
  propertyType: String,
): Sequence<Code> = sequence {
  yield("$newLine & { ")
  val boundary = yield(Boundary.start(source, offset, codeFeatures.navigation))
  yield("'")
  yield(DataSegment(
    text = classNameWithDot.substring(1),
    source = source,
    sourceOffset = offset + 1,
    data = boundary.features,
  ))
  yield("'")
  yield(boundary.end(offset + classNameWithDot.length))
  yield(": $propertyType")
  yield(" }")
}

fun generateStyleImports(
  style: IRStyle,
): Sequence<Code> = sequence {
  val src = style.src
  if (src is IRAttr.WithText) {
    yield("$newLine & typeof import(")
    val boundary = yield(Boundary.start(Source("main"), src.offset, codeFeatures.navigationAndVerification))
    yield("'")
    yield(DataSegment(
      text = src.text,
      source = Source("main"),
      sourceOffset = src.offset,
      data = boundary.features,
    ))
    yield("'")
    yield(boundary.end(src.offset + src.text.length))
    yield(").default")
  }
  for ((text, offset) in style.imports) {
    yield("$newLine & typeof import('")
    yield(DataSegment(
      text = text,
      source = style.name,
      sourceOffset = offset,
      data = codeFeatures.navigationAndVerification,
    ))
    yield("').default")
  }
}
