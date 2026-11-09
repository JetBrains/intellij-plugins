// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.CommonCodegenOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getRefBrandArgument
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRE
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

  var prevEnd = 0
  for ((name, offset, isShorthand, isNarrowing, inTypeQuery, isNewOperand) in forEachIdentifiers(options, ctx, code, prefix, suffix, inNarrowing)) {
    if (isShorthand) {
      yieldAll(generateNonIdentifierCode(
        code = code.substring(prevEnd, offset + name.length),
        source = block.name,
        offset = start + prevEnd,
        data = data,
        shouldCut = prevEnd > 0,
      ))
      yield(": ")
    }
    else if (prevEnd < offset) {
      yieldAll(generateNonIdentifierCode(
        code = code.substring(prevEnd, offset),
        source = block.name,
        offset = start + prevEnd,
        data = data,
        shouldCut = prevEnd > 0,
      ))
    }

    val codes = listOf<Code>(DataSegment(
      text = name,
      source = block.name,
      sourceOffset = start + offset,
      data = if (isShorthand) data.copy(__shorthandExpression = VueCodeInformation.ShorthandExpression.js) else data,
    ))

    yieldAll(generateIdentifier(
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
    ))

    prevEnd = offset + name.length
  }

  if (prevEnd < code.length) {
    yieldAll(generateNonIdentifierCode(
      code = code.substring(prevEnd),
      source = block.name,
      offset = start + prevEnd,
      data = data,
      shouldCut = prevEnd > 0,
    ))
  }

  if (suffix.isNotEmpty()) {
    yield(suffix)
  }
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
