// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style

import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.generateStyleScopedClassReference
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateStyleScopedClasses(
  options: StyleCodegenOptions,
  ctx: TemplateCodegenContext,
): Sequence<Code> = sequence {
  val resolveStyleClassNames = options.vueCompilerOptions.resolveStyleClassNames
  val resolveStyleImports = options.vueCompilerOptions.resolveStyleImports
  if (resolveStyleClassNames == "false") return@sequence
  val scopedStyles = options.styles.filter { resolveStyleClassNames == "true" || it.scoped }
  if (scopedStyles.isEmpty()) return@sequence
  ctx.generatedTypes.add(names.StyleScopedClasses)

  val visited = mutableSetOf<String>()
  val deferredGenerates = mutableListOf<Sequence<Code>>()
  yield("type ${names.StyleScopedClasses} = {}")
  for (style in scopedStyles) {
    if (resolveStyleImports) {
      yieldAll(generateStyleImports(style))
    }
    for (className in style.classNames) {
      if (visited.add(className.text)) {
        yieldAll(generateClassProperty(style.name, className.text, className.offset, "boolean"))
      }
      else {
        deferredGenerates.add(
          generateStyleScopedClassReference(style, className.text.drop(1), className.offset + 1),
        )
      }
    }
  }
  yield(endOfLine)

  for (generate in deferredGenerates) {
    yieldAll(generate)
  }
}
