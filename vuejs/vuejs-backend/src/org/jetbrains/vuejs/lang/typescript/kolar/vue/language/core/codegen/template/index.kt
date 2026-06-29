// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

class TemplateGenerateResult(
  val codes: List<Code>,
  val ctx: TemplateCodegenContext,
)

fun generateTemplate(
  options: TemplateCodegenOptions,
): TemplateGenerateResult {
  val ctx = TemplateCodegenContext()
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
  return TemplateGenerateResult(codes = codes, ctx = ctx)
}

private fun generateWorker(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
): Sequence<Code> = sequence {
  val scope = ctx.scope()
  scope.declare(options.setupConsts.toList())
  val vueCompilerOptions = options.vueCompilerOptions

  options.slotsAssignName?.let { scope.declare(it) }
  options.propsAssignName?.let { scope.declare(it) }

  if (vueCompilerOptions.inferTemplateDollarSlots) ctx.dollarVars.add("\$slots")
  if (vueCompilerOptions.inferTemplateDollarAttrs) ctx.dollarVars.add("\$attrs")
  if (vueCompilerOptions.inferTemplateDollarRefs) ctx.dollarVars.add("\$refs")
  if (vueCompilerOptions.inferTemplateDollarEl) ctx.dollarVars.add("\$el")

  val ast = options.template.ast
  if (ast != null) {
    yieldAll(generateTemplateChild(options, ctx, ast as Node))
  }

  yieldAll(ctx.generateHoistVariables())
  yieldAll(generateSlotsType(options, ctx))
  yieldAll(generateInheritedAttrsType(options, ctx))
  yieldAll(generateTemplateRefsType(options, ctx))
  yieldAll(generateRootElType(ctx))

  if (ctx.dollarVars.isNotEmpty()) {
    yield("var ${names.dollars}!: {$newLine")
    if ("\$slots" in ctx.dollarVars) {
      val type = if (names.Slots in ctx.generatedTypes) names.Slots else "{}"
      yield("\$slots: $type$endOfLine")
    }
    if ("\$attrs" in ctx.dollarVars) {
      yield("\$attrs: import('${vueCompilerOptions.lib}').ComponentPublicInstance['\$attrs']")
      if (names.InheritedAttrs in ctx.generatedTypes) {
        yield(" & ${names.InheritedAttrs}")
      }
      yield(endOfLine)
    }
    if ("\$refs" in ctx.dollarVars) {
      val type = if (names.TemplateRefs in ctx.generatedTypes) names.TemplateRefs else "{}"
      yield("\$refs: $type$endOfLine")
    }
    if ("\$el" in ctx.dollarVars) {
      val type = if (names.RootEl in ctx.generatedTypes) names.RootEl else "any"
      yield("\$el: $type$endOfLine")
    }
    yield("} & { [K in keyof import('${vueCompilerOptions.lib}').ComponentPublicInstance]: unknown }$endOfLine")
  }

  yieldAll(scope.end())
}

private fun generateSlotsType(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
): Sequence<Code> = sequence {
  if (options.hasDefineSlots) {
    ctx.generatedTypes.add(names.Slots)
    return@sequence
  }
  if (ctx.slots.isEmpty() && ctx.dynamicSlots.isEmpty())
    return@sequence

  ctx.generatedTypes.add(names.Slots)

  yield("type ${names.Slots} = {}")
  for ((expVar, propsVar) in ctx.dynamicSlots) {
    yield("$newLine& { [K in NonNullable<typeof $expVar>]?: (props: typeof $propsVar) => any }")
  }
  for (slot in ctx.slots) {
    yield("$newLine& { ")
    if (slot.name.isNotEmpty() && slot.offset != null) {
      yieldAll(generateObjectProperty(options, ctx, slot.name, slot.offset, codeFeatures.navigation))
    }
    else {
      val boundary = yield(Boundary.start("template", slot.tagRange.first, codeFeatures.navigation))
      yield("default")
      yield(boundary.end(slot.tagRange.second))
    }
    yield("?: (props: typeof ${slot.propsVar}) => any }")
  }
  yield(endOfLine)
}

private fun generateInheritedAttrsType(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
): Sequence<Code> = sequence {
  if (ctx.inheritedAttrVars.isEmpty()) return@sequence
  ctx.generatedTypes.add(names.InheritedAttrs)

  val type = ctx.inheritedAttrVars.joinToString(" & ") { "typeof $it" }
  yield("type ${names.InheritedAttrs} = ${
    if (options.vueCompilerOptions.checkRequiredFallthroughAttributes) type else "Partial<$type>"
  }")
  yield(endOfLine)
}

private fun generateTemplateRefsType(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
): Sequence<Code> = sequence {
  if (ctx.templateRefs.isEmpty()) return@sequence
  ctx.generatedTypes.add(names.TemplateRefs)

  yield("type ${names.TemplateRefs} = {}")
  for ((name, refs) in ctx.templateRefs) {
    yield("$newLine& ")
    if (refs.size >= 2) yield("(")
    for ((i, ref) in refs.withIndex()) {
      if (i > 0) yield(" | ")
      yield("{ ")
      yieldAll(generateObjectProperty(options, ctx, name, ref.offset, codeFeatures.navigation))
      yield(": ${ref.typeExp} }")
    }
    if (refs.size >= 2) yield(")")
  }
  yield(endOfLine)
}

private fun generateRootElType(ctx: TemplateCodegenContext): Sequence<Code> = sequence {
  if (ctx.singleRootElTypes.isEmpty() || null in ctx.singleRootNodes) return@sequence
  ctx.generatedTypes.add(names.RootEl)

  yield("type ${names.RootEl} = ")
  for (type in ctx.singleRootElTypes) {
    yield("$newLine| $type")
  }
  yield(endOfLine)
}
