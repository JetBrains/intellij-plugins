// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRAttr
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateStyleModules(
  options: StyleCodegenOptions,
  ctx: TemplateCodegenContext,
): Sequence<Code> = sequence {
  val styleModules = options.styles.filter { it.module != null }
  if (styleModules.isEmpty()) return@sequence

  ctx.generatedTypes.add(names.StyleModules)

  yield("type ${names.StyleModules} = {$newLine")
  for (style in styleModules) {
    when (val module = style.module) {
      is IRAttr.Present -> yield("\$style")
      is IRAttr.WithText -> yield(DataSegment(
        text = module.text,
        source = "main",
        sourceOffset = module.offset,
        data = codeFeatures.navigation,
      ))
      null -> Unit
    }
    yield(": ")
    if (!options.vueCompilerOptions.strictCssModules) {
      yield("Record<string, string> & ")
    }
    yield("${names.PrettifyGlobal}<{}")
    if (options.vueCompilerOptions.resolveStyleImports) {
      yieldAll(generateStyleImports(style))
    }
    for ((text, offset) in style.classNames) {
      yieldAll(generateClassProperty(
        source = style.name,
        classNameWithDot = text,
        offset = offset,
        propertyType = "string",
      ))
    }
    yield(">$endOfLine")
  }
  yield("}$endOfLine")
}
