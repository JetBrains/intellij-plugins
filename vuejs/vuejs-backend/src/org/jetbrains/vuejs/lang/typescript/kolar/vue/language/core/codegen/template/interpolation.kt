// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.javascript.psi.JSBlockStatement
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isBlock
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.CommonCodegenOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.FakeSourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getRefBrandArgument
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRE
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getNodeText
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getStartEnd
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateInterpolation(
  options: CommonCodegenOptions,
  ctx: TemplateCodegenContext,
  block: IRBlock,
  data: VueCodeInformation,
  code: String,
  start: Int,
  prefix: String = "",
  suffix: String = "",
  inNarrowing: Boolean = false,
): Sequence<Code> = sequence {
  if (prefix.isNotEmpty()) {
    yield(prefix)
  }

  // manual: only the template codegen options carry `reassertBindings`
  val reassertBindings = (options as? TemplateCodegenOptions)?.reassertBindings

  // Assertion narrowing does not flow into functions for imports and `let`/`var`
  // bindings (TS limitation), so each function body re-asserts the ones it reads.
  // Bodies are buffered until their end, when those reads are known.
  val bodies = if (!reassertBindings.isNullOrEmpty() && !identifierRE.matches(code))
    collectFunctionBodies(getTypeScriptAST(prefix + code + suffix, options))
      .map { body -> body.copy(start = body.start - prefix.length, end = body.end - prefix.length) }
  else
    emptyList()
  // Exits precede enters at the same offset; the stable sort keeps the inner exit and the outer enter first.
  val marks = (
    bodies.map { body -> FunctionBodyMark(offset = body.end, body = body, enter = false) }.reversed()
    + bodies.map { body -> FunctionBodyMark(offset = body.start, body = body, enter = true) }
  ).sortedBy { it.offset }
  val frames = mutableListOf<FunctionBodyFrame>()
  var markIndex = 0

  fun write(codes: Sequence<Code>): Sequence<Code> {
    if (frames.isNotEmpty()) {
      frames.last().codes.addAll(codes)
      return emptySequence()
    }
    return codes
  }

  fun generateFunctionBody(frame: FunctionBodyFrame): Sequence<Code> = sequence {
    val (isBlock, codes, reasserts) = frame
    if (reasserts.isEmpty()) {
      yieldAll(codes)
      return@sequence
    }
    yield(if (isBlock) newLine else "{$newLine")
    for (name in reasserts) {
      yield("${names.withDotValue}($name, ${getRefBrandArgument(options.vueCompilerOptions, options.scriptLang)})$endOfLine")
    }
    if (isBlock) {
      yieldAll(codes)
    }
    else {
      yield("return (")
      yieldAll(codes)
      yield(")$endOfLine}")
    }
  }

  fun generateCodeUntil(initialFrom: Int, end: Int): Sequence<Code> = sequence {
    var from = initialFrom
    while (markIndex < marks.size && marks[markIndex].offset <= end) {
      val (offset, body, enter) = marks[markIndex++]
      yieldAll(write(generateNonIdentifierCode(code.substring(from, offset), block.name, start + from, data, from > 0)))
      from = offset
      if (enter) {
        ctx.enterFunction()
        frames.add(FunctionBodyFrame(isBlock = body.isBlock, codes = mutableListOf(), reasserts = linkedSetOf()))
      }
      else {
        ctx.exitFunction()
        yieldAll(write(generateFunctionBody(frames.removeLast())))
      }
    }
    yieldAll(write(generateNonIdentifierCode(code.substring(from, end), block.name, start + from, data, from > 0)))
  }

  var prevEnd = 0
  for ((name, offset, isShorthand, isNarrowing, inTypeQuery, isNewOperand) in forEachIdentifiers(options, ctx, code, prefix, suffix, inNarrowing)) {
    if (isShorthand) {
      yieldAll(generateCodeUntil(prevEnd, offset + name.length))
      yieldAll(write(sequenceOf(StringSegment(": "))))
    }
    else {
      yieldAll(generateCodeUntil(prevEnd, offset))
    }

    if (
      frames.isNotEmpty()
      && reassertBindings!!.contains(name)
      && !options.setupConsts.contains(name)
      && !options.setupRefs.contains(name)
      && !shouldIdentifierSkipped(ctx, name)
    ) {
      frames.last().reasserts.add(name)
    }

    val codes = listOf<Code>(DataSegment(
      text = name,
      source = block.name,
      sourceOffset = start + offset,
      data = if (isShorthand) data.copy(__shorthandExpression = VueCodeInformation.ShorthandExpression.js) else data,
    ))

    yieldAll(write(generateIdentifier(
      options = options,
      ctx = ctx,
      codes = codes,
      name = name,
      source = block.name,
      start = start + offset,
      end = start + offset + name.length,
      isNarrowing = isNarrowing,
      inTypeQuery = inTypeQuery,
      isNewOperand = isNewOperand,
    )))

    prevEnd = offset + name.length
  }

  yieldAll(generateCodeUntil(prevEnd, code.length))

  if (suffix.isNotEmpty()) {
    yield(suffix)
  }
}

private data class FunctionBody(
  val start: Int,
  val end: Int,
  val isBlock: Boolean,
)

private data class FunctionBodyMark(
  val offset: Int,
  val body: FunctionBody,
  val enter: Boolean,
)

private data class FunctionBodyFrame(
  val isBlock: Boolean,
  val codes: MutableList<Code>,
  val reasserts: MutableSet<String>,
)

// Bodies in pre-order; a block body spans between its braces.
// manual: a PSI class static block is the block itself, a member of the class
private fun collectFunctionBodies(ast: FakeSourceFile): List<FunctionBody> {
  val bodies = mutableListOf<FunctionBody>()
  fun visit(node: PsiElement) {
    val body = when {
      node is JSFunction ->
        node.block ?: if (node.isShorthandArrowFunction) PsiTreeUtil.getChildOfType(node, JSExpression::class.java) else null
      isBlock(node) && node.parent is JSClass -> node
      else -> null
    }
    if (body != null) {
      val (_, start, end) = getStartEnd(body)
      val isBlock = isBlock(body)
      if (body !is JSBlockStatement || body.statementListItems.isNotEmpty()) {
        bodies.add(FunctionBody(start = if (isBlock) start + 1 else start, end = if (isBlock) end - 1 else end, isBlock = isBlock))
      }
    }
    for (child in forEachNode(node)) {
      visit(child)
    }
  }
  for (child in forEachNode(ast)) {
    visit(child)
  }
  return bodies
}

/**
 * Yield a code chunk, cutting the boundary character off as verification-only.
 *
 * Adjacent mappings share the boundary offset (closed interval), so both the
 * neighbouring token's end and this chunk's start claim the same source offset.
 * Downgrading the boundary character to verification-only keeps content-sensitive
 * features (rename / navigation) from firing on the neighbouring chunk.
 */
private fun generateNonIdentifierCode(
  code: String,
  source: Source,
  offset: Int,
  data: VueCodeInformation,
  shouldCut: Boolean = true,
): Sequence<Code> = sequence {
  if (code.isEmpty()) {
    return@sequence
  }
  if (!shouldCut) {
    yield(DataSegment(text = code, source = source, sourceOffset = offset, data = data))
    return@sequence
  }
  yield(DataSegment(
    text = code.substring(0, 1),
    source = source,
    sourceOffset = offset,
    data = VueCodeInformation(verification = data.verification),
  ))
  if (code.length > 1) {
    yield(DataSegment(text = code.substring(1), source = source, sourceOffset = offset + 1, data = data))
  }
}

// Access strategy, in precedence order:
// - setup consts → direct reference
// - local-scope / global names → direct reference (the interpolation path
//   filters these earlier; the v-bind shorthand path relies on this branch)
// - template refs → direct `.value`
// - dotValue bindings (narrowed at least once anywhere) → `.value` at
//   every position; narrowing then works on the `.value` reference chain
// - other bindings → `__VLS_unwrap` (plain reads keep the original type)
// - otherwise → `__VLS_ctx.<name>`
fun generateIdentifier(
  options: CommonCodegenOptions,
  ctx: TemplateCodegenContext,
  codes: List<Code>,
  name: String,
  source: Source,
  start: Int,
  end: Int,
  isNarrowing: Boolean = false,
  inTypeQuery: Boolean = false,
  isNewOperand: Boolean = false,
): Sequence<Code> = sequence {
  if (options.setupConsts.contains(name) || shouldIdentifierSkipped(ctx, name)) {
    yieldAll(codes)
  }
  else if (options.setupRefs.contains(name)) {
    yieldAll(codes)
    yield(".")
    val boundary = yield(Boundary.start(
      source,
      start,
      end,
      codeFeatures.verification,
    ))
    yield("value")
    yield(boundary.end())
  }
  else if (options.setupBindings.contains(name)) {
    // First pass records narrowing accesses here; the second pass emits from dotValueBindings.
    ctx.accessVariable(source, name, start, inTypeQuery || isNarrowing)
    if (inTypeQuery || options.dotValueBindings.contains(name)) {
      yieldAll(codes)
      yield(".")
      val boundary = yield(Boundary.start(
        source,
        start,
        end,
        codeFeatures.verification,
      ))
      yield("value")
      yield(boundary.end())
    }
    else {
      // `new __VLS_unwrap(Foo)()` parses as `new (__VLS_unwrap(Foo)())`,
      // whose target lacks a construct signature; keep the operand parenthesized.
      if (isNewOperand) {
        yield("(")
      }
      yield("${names.unwrap}(")
      yieldAll(codes)
      yield(", ${getRefBrandArgument(options.vueCompilerOptions, options.scriptLang)})")
      if (isNewOperand) {
        yield(")")
      }
    }
  }
  else {
    // #1205, #1264
    val boundary = yield(Boundary.start(
      source,
      start,
      end,
      codeFeatures.verification,
    ))
    if (ctx.dollarVars.contains(name)) {
      yield(names.dollars)
    }
    else {
      ctx.accessVariable(source, name, start)
      yield(names.ctx)
    }
    yield(".")
    yieldAll(codes)
    yield(boundary.end())
  }
}

private fun forEachIdentifiers(
  options: CommonCodegenOptions,
  ctx: TemplateCodegenContext,
  code: String,
  prefix: String,
  suffix: String,
  inNarrowing: Boolean,
): Sequence<IdentifierAccess> = sequence {
  if (identifierRE.matches(code) && !shouldIdentifierSkipped(ctx, code)) {
    yield(IdentifierAccess(code, 0, isShorthand = false, isNarrowing = inNarrowing, inTypeQuery = false, isNewOperand = false))
    return@sequence
  }

  val scope = ctx.scope()
  val ast = getTypeScriptAST(prefix + code + suffix, options)
  for (item in forEachDeclarationsInSourceFile(ast, ctx, scope, inNarrowing)) {
    if (item.skipped) {
      continue
    }
    val text = getNodeText(item.id)
    yield(IdentifierAccess(
      name = text,
      offset = getStartEnd(item.id).start - prefix.length,
      isShorthand = item.isShorthand,
      isNarrowing = item.isNarrowing,
      inTypeQuery = item.inTypeQuery,
      isNewOperand = item.isNewOperand,
    ))
  }
  scope.end()
}

private data class IdentifierAccess(
  val name: String,
  val offset: Int,
  val isShorthand: Boolean,
  val isNarrowing: Boolean,
  val inTypeQuery: Boolean,
  val isNewOperand: Boolean,
)
