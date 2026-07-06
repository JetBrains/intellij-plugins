// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.replaceSourceRange
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrowFunction
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isExpressionStatement
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingNames
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateVSlot(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
  slotDir: DirectiveNode?,
  ctxVar: String,
): Sequence<Code> = sequence {
  val slotVar = ctx.getInternalVariable()
  val arg = slotDir?.arg
  if (slotDir != null) {
    yield("{$newLine")
    yield("const { ")
    if (arg is SimpleExpressionNode && arg.content.isNotEmpty()) {
      yieldAll(generateObjectProperty(
        options = options,
        ctx = ctx,
        code = arg.loc.source,
        offset = arg.loc.start.offset,
        features = if (arg.isStatic) codeFeatures.withoutHighlight else codeFeatures.all,
        shouldCamelize = false,
        shouldBeConstant = true,
      ))
    }
    else {
      val boundary = yield(Boundary.start(Source("template"), slotDir.loc.start.offset, codeFeatures.withoutHighlightAndCompletion))
      yield("default")
      yield(boundary.end(slotDir.loc.start.offset + (slotDir.rawName?.length ?: 0)))
    }
  }
  else {
    yield("const { ")
    // #932: reference for implicit default slot
    val boundary = yield(Boundary.start(Source("template"), node.loc.start.offset, codeFeatures.navigation))
    yield("default")
    yield(boundary.end(node.loc.end.offset))
  }
  yield(": $slotVar } = $ctxVar.slots!$endOfLine")

  val scope = ctx.scope()
  val exp = slotDir?.exp
  if (exp is SimpleExpressionNode) {
    val slotAst = getTypeScriptAST(options.template, "(${exp.content}) => {}")
    yieldAll(generateSlotParameters(options, ctx, slotAst, exp, slotVar))
    scope.declare(collectBindingNames(slotAst))
  }
  for (child in node.children) {
    yieldAll(generateTemplateChild(options, ctx, child))
  }
  yieldAll(scope.end())

  if (slotDir != null) {
    var isStatic = true
    if (arg is SimpleExpressionNode) {
      isStatic = arg.isStatic
    }
    if (isStatic && arg == null) {
      yield("$ctxVar.slots!['")
      val prefixLen = when {
        slotDir.loc.source.startsWith("#") -> "#".length
        slotDir.loc.source.startsWith("v-slot:") -> "v-slot:".length
        else -> 0
      }
      yield(DataSegment(text = "",
                        source = Source("template"),
                        sourceOffset = slotDir.loc.start.offset + prefixLen,
                        data = codeFeatures.completion))
      yield("'/* empty slot name completion */]$endOfLine")
    }
    yield("}$newLine")
  }
}

private fun generateSlotParameters(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  ast: SourceFile,
  exp: SimpleExpressionNode,
  slotVar: String,
): Sequence<Code> = sequence {
  val statement = ast.statements.firstOrNull()
  if (statement == null) return@sequence
  if (!isExpressionStatement(statement)) return@sequence

  val expression = statement.expression
  if (!isArrowFunction(expression)) return@sequence

  val startOffset = exp.loc.start.offset - 1
  val types = mutableListOf<Code?>()
  val interpolation = generateInterpolation(
    options = options,
    ctx = ctx,
    block = options.template,
    data = codeFeatures.all,
    code = ast.text,
    start = startOffset,
  ).toMutableList()

  replaceSourceRange(interpolation, Source("template"), startOffset, startOffset + "(".length)
  replaceSourceRange(
    interpolation,
    Source("template"),
    startOffset + ast.text.length - ") => {}".length,
    startOffset + ast.text.length,
  )

  for (param in expression.parameters) {
    val paramType = param.type
    if (paramType != null) {
      types.add(DataSegment(
        text = ast.text.substring(param.name.end, paramType.end),
        source = Source("template"),
        sourceOffset = startOffset + param.name.end,
        data = codeFeatures.all,
      ))
      replaceSourceRange(interpolation, Source("template"), startOffset + param.name.end, startOffset + paramType.end)
    }
    else {
      types.add(null)
    }
  }

  yield("const [")
  yieldAll(interpolation)
  yield("] = ${names.vSlot}($slotVar!")

  if (types.any { it != null }) {
    yield(", ")
    val boundary = yield(Boundary.start(Source("template"), exp.loc.start.offset, codeFeatures.verification))
    yield("(")
    for (type in types) {
      if (type != null) {
        yield("_")
        yield(type)
        yield(", ")
      }
      else {
        yield("_, ")
      }
    }
    yield(") => [] as any")
    yield(boundary.end(exp.loc.end.offset))
  }
  yield(")$endOfLine")
}
