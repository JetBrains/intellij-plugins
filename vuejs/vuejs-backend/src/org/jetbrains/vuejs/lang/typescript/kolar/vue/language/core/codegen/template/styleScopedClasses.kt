// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateEscaped
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.startBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

private val classNameEscapeRegex = Regex("""([\\'])""")

fun generateStyleScopedClassReference(
  block: IRBlock,
  className: String,
  offset: Int,
  fullStart: Int = offset,
): Sequence<Code> = sequence {
  if (className.isEmpty()) {
    yield("/** @type {${names.StyleScopedClasses}['")
    yield(DataSegment(text = "", source = block.name, sourceOffset = offset, data = codeFeatures.completion))
    yield("']} */$endOfLine")
    return@sequence
  }

  yield("/** @type {${names.StyleScopedClasses}[")
  val token = yield(startBoundary(block.name, fullStart, codeFeatures.navigation))
  yield("'")
  yieldAll(generateEscaped(
    className,
    block.name,
    offset,
    codeFeatures.navigationAndCompletion,
    classNameEscapeRegex,
  ))
  yield("'")
  yield(endBoundary(token, offset + className.length))
  yield("]} */$endOfLine")
}
