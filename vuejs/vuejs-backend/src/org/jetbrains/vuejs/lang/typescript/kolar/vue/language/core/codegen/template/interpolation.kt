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
    val identifierData = if (isShorthand) data.copy(__shorthandExpression = VueCodeInformation.ShorthandExpression.js) else data
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

    // Access strategy, in precedence order (keep in sync with the v-bind
    // shorthand handling in elementProps.kt):
    // - destructured props / imported components → direct reference
    // - template refs → direct `.value`
    // - dotValue bindings (narrowed at least once anywhere) → `.value` at
    //   every position; narrowing then works on the `.value` reference chain
    // - other bindings → `__VLS_unwrap` (plain reads keep the original type)
    // - otherwise → `__VLS_ctx.<name>`
    if (options.destructuredProps.contains(name) || options.importedComponents.contains(name)) {
      yield(DataSegment(
        text = name,
        source = block.name,
        sourceOffset = start + offset,
        data = identifierData,
      ))
    }
    else if (options.setupRefs.contains(name)) {
      yield(DataSegment(
        text = name,
        source = block.name,
        sourceOffset = start + offset,
        data = data,
      ))
      yield(".value")
    }
    else if (options.setupBindings.contains(name)) {
      // First pass records narrowing accesses here; the second pass emits from dotValueBindings.
      ctx.accessVariable(block.name, name, start + offset, inTypeQuery || isNarrowing)
      if (inTypeQuery || options.dotValueBindings.contains(name)) {
        yield(DataSegment(
          text = name,
          source = block.name,
          sourceOffset = start + offset,
          data = identifierData,
        ))
        yield(".value")
      }
      else {
        // `new __VLS_unwrap(Foo)()` parses as `new (__VLS_unwrap(Foo)())`,
        // whose target lacks a construct signature; keep the operand parenthesized.
        if (isNewOperand) {
          yield("(")
        }
        yield("${names.unwrap}(")
        yield(DataSegment(
          text = name,
          source = block.name,
          sourceOffset = start + offset,
          data = identifierData,
        ))
        yield(", ${getRefBrandArgument(options.vueCompilerOptions)})")
        if (isNewOperand) {
          yield(")")
        }
      }
    }
    else {
      // #1205, #1264
      val boundary = yield(Boundary.start(
        block.name,
        start + offset,
        start + offset + name.length,
        codeFeatures.verification,
      ))
      if (ctx.dollarVars.contains(name)) {
        yield(names.dollars)
      }
      else {
        ctx.accessVariable(block.name, name, start + offset)
        yield(names.ctx)
      }
      yield(".")
      yield(DataSegment(
        text = name,
        source = block.name,
        sourceOffset = start + offset,
        data = identifierData,
      ))
      yield(boundary.end())
    }

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
