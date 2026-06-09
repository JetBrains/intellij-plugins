// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRAttr
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRStyle
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.startBoundary

fun generateClassProperty(
  source: String,
  classNameWithDot: String,
  offset: Int,
  propertyType: String,
): Sequence<Code> = sequence {
  yield(StringSegment("$newLine & { "))
  val token = yield(startBoundary(source, offset, codeFeatures.navigation))
  yield(StringSegment("'"))
  yield(DataSegment(
    text = classNameWithDot.substring(1),
    source = source,
    sourceOffset = offset + 1,
    data = VueCodeInformation(__combineToken = token),
  ))
  yield(StringSegment("'"))
  yield(endBoundary(token, offset + classNameWithDot.length))
  yield(StringSegment(": $propertyType"))
  yield(StringSegment(" }"))
}

fun generateStyleImports(
  style: IRStyle,
): Sequence<Code> = sequence {
  val features = codeFeatures.navigationAndVerification
  val src = style.src
  if (src is IRAttr.WithText) {
    yield(StringSegment("$newLine & typeof import("))
    val token = yield(startBoundary("main", src.offset, features))
    yield(StringSegment("'"))
    yield(DataSegment(
      text = src.text,
      source = "main",
      sourceOffset = src.offset,
      data = VueCodeInformation(__combineToken = token),
    ))
    yield(StringSegment("'"))
    yield(endBoundary(token, src.offset + src.text.length))
    yield(StringSegment(").default"))
  }
  for ((text, offset) in style.imports) {
    yield(StringSegment("$newLine & typeof import('"))
    yield(DataSegment(
      text = text,
      source = style.name,
      sourceOffset = offset,
      data = features,
    ))
    yield(StringSegment("').default"))
  }
}
