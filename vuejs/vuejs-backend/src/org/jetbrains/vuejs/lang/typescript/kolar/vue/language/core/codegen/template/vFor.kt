// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.javascript.psi.JSDestructuringArray
import com.intellij.lang.javascript.psi.JSDestructuringElement
import com.intellij.lang.javascript.psi.JSDestructuringObject
import com.intellij.lang.javascript.psi.JSInitializerOwner
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingNames
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getStartEnd
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

private data class VForParseResult(
  val leftExpressionRange: OffsetRange?,
  val leftExpressionText: String?,
) {
  data class OffsetRange(
    val start: Int,
    val end: Int,
  )
}

fun generateVFor(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ForNode,
): Sequence<Code> = sequence {
  val source = node.parseResult.source
  val (leftExpressionRange, leftExpressionText) = parseVForNode(node)
  val scope = ctx.scope()
  var bindingNames: List<String> = emptyList()
  val defaultInitializerRanges = mutableListOf<Pair<Int, Int>>()

  if (leftExpressionRange != null && leftExpressionText != null) {
    val wrap = "const ["
    val collectAst = getTypeScriptAST("$wrap$leftExpressionText]", options)
    bindingNames = collectBindingNames(collectAst)
    val declaration = PsiTreeUtil.findChildOfType(collectAst, JSDestructuringArray::class.java)
    val initializers = mutableListOf<PsiElement>()
    if (declaration != null) {
      collectDefaultInitializers(declaration, initializers)
    }
    for (initializer in initializers) {
      val range = getStartEnd(initializer)
      defaultInitializerRanges.add(Pair(range.start - wrap.length, range.end - wrap.length))
    }
    defaultInitializerRanges.sortBy { it.first }
  }

  // Evaluate the source before the loop bindings enter scope (`v-for="x in x"` reads the outer `x`);
  // destructuring defaults (`{ a, b = a }`) are interpolated after the declaration to see the sibling aliases.
  var sourceAlias: String? = null
  if (source is SimpleExpressionNode) {
    sourceAlias = ctx.getInternalVariable()
    // tryAsConstant keeps inline literal sources from widening to `number[]` (#6067).
    yield("const $sourceAlias = ${names.tryAsConstant}(")
    yieldAll(generateInterpolation(
      options = options,
      ctx = ctx,
      block = options.template,
      data = codeFeatures.all,
      code = source.content,
      start = source.loc.startOffset,
      prefix = "(",
      suffix = ")",
    ))
    yield(")")
    yield(endOfLine)
  }

  scope.declare(bindingNames)

  yield("for (const [")
  if (leftExpressionRange != null && leftExpressionText != null) {
    var lastOffset = 0
    for ((start, end) in defaultInitializerRanges) {
      if (start > lastOffset) {
        yield(DataSegment(
          text = leftExpressionText.substring(lastOffset, start),
          source = Source("template"),
          sourceOffset = leftExpressionRange.start + lastOffset,
          data = codeFeatures.all,
        ))
      }
      yieldAll(generateInterpolation(
        options = options,
        ctx = ctx,
        block = options.template,
        data = codeFeatures.all,
        code = leftExpressionText.substring(start, end),
        start = leftExpressionRange.start + start,
      ))
      lastOffset = end
    }
    if (lastOffset < leftExpressionText.length) {
      yield(DataSegment(
        text = leftExpressionText.substring(lastOffset),
        source = Source("template"),
        sourceOffset = leftExpressionRange.start + lastOffset,
        data = codeFeatures.all,
      ))
    }
  }
  yield("] of ")
  if (sourceAlias != null) {
    yield("${names.vFor}(")
    yield(sourceAlias)
    yield("!)") // #3102
  }
  else {
    yield("{} as any")
  }
  yield(") {$newLine")

  val inVFor = ctx.inVFor
  ctx.inVFor = true
  for (child in node.children) {
    yieldAll(generateTemplateChild(
      options = options,
      ctx = ctx,
      node = child,
      enterNode = false,
      treatTemplateAsFragment = true,
    ))
  }
  ctx.inVFor = inVFor

  yieldAll(scope.end())
  yield("}$newLine")
}

private fun parseVForNode(
  node: ForNode,
): VForParseResult {
  val result = node.parseResult
  val leftExpressionRange = if (result.value != null || result.key != null || result.index != null) {
    VForParseResult.OffsetRange(
      start = (result.value ?: result.key ?: result.index)!!.loc.startOffset,
      end = (result.index ?: result.key ?: result.value)!!.loc.endOffset,
    )
  }
  else null
  val leftExpressionText = if (leftExpressionRange != null) {
    node.loc.source.substring(
      leftExpressionRange.start - node.loc.startOffset,
      leftExpressionRange.end - node.loc.startOffset,
    )
  }
  else null
  return VForParseResult(leftExpressionRange, leftExpressionText)
}

private fun collectDefaultInitializers(
  pattern: PsiElement,
  out: MutableList<PsiElement>,
) {
  val elements: List<PsiElement?> = when (pattern) {
    is JSDestructuringArray -> pattern.elements.toList()
    is JSDestructuringObject -> pattern.properties.map { it.destructuringElement }
    else -> return
  }
  for (element in elements) {
    if (element is JSDestructuringElement) {
      element.target?.let { collectDefaultInitializers(it, out) }
    }
    (element as? JSInitializerOwner)?.initializer?.let { out.add(it) }
  }
}
