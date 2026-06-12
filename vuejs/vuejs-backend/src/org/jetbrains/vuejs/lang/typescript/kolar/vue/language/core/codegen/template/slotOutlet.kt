// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.AttributeNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.createVBindShorthandInlayHintInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.startBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getElementTagOffsets
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.normalizeAttributeValue
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateSlotOutlet(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
): Sequence<Code> = sequence {
  val tagOffsets = getElementTagOffsets(node, options.template)
  val startTagOffset = tagOffsets[0]
  val startTagEndOffset = startTagOffset + node.tag.length
  val propsVar = ctx.getInternalVariable()
  val nameProp = node.props.find { prop ->
    when {
      prop is AttributeNode -> prop.name == "name"
      prop is DirectiveNode && prop.name == "bind" && prop.arg is SimpleExpressionNode ->
        (prop.arg as SimpleExpressionNode).content == "name"
      else -> false
    }
  }

  if (options.hasDefineSlots) {
    yield("${names.asFunctionalSlot}(")
    if (nameProp != null) {
      val token = yield(startBoundary("template", nameProp.loc.start.offset, codeFeatures.verification))
      yield(options.slotsAssignName ?: names.slots)
      if (nameProp is AttributeNode && nameProp.value != null) {
        val (content, offset) = normalizeAttributeValue(nameProp.value!!)
        yieldAll(generatePropertyAccess(options, ctx, content, offset, codeFeatures.navigationAndVerification))
      }
      else if (nameProp is DirectiveNode && nameProp.exp is SimpleExpressionNode) {
        yield("[")
        yieldAll(generatePropExp(options, ctx, nameProp, nameProp.exp as SimpleExpressionNode))
        yield("]")
      }
      else {
        yield("['default']")
      }
      yield(endBoundary(token, nameProp.loc.end.offset))
    }
    else {
      val token = yield(startBoundary("template", startTagOffset, codeFeatures.verification))
      yield("${options.slotsAssignName ?: names.slots}[")
      val token2 = yield(startBoundary("template", startTagOffset, codeFeatures.verification))
      yield("'default'")
      yield(endBoundary(token2, startTagEndOffset))
      yield("]")
      yield(endBoundary(token, startTagEndOffset))
    }
    yield(")(")
    val token = yield(startBoundary("template", startTagOffset, codeFeatures.verification))
    yield("{$newLine")
    yieldAll(generateElementProps(options, ctx, node, node.props.filter { it !== nameProp }, true))
    yield("}")
    yield(endBoundary(token, startTagEndOffset))
    yield(")$endOfLine")
  }
  else {
    yield("var $propsVar = {$newLine")
    yieldAll(generateElementProps(options, ctx, node, node.props.filter { it !== nameProp }, options.vueCompilerOptions.checkUnknownProps))
    yield("}$endOfLine")

    if (nameProp is AttributeNode && nameProp.value != null) {
      ctx.slots.add(TemplateCodegenContext.Slot(
        name = nameProp.value!!.content,
        offset = nameProp.loc.start.offset + nameProp.loc.source.indexOf(nameProp.value!!.content, nameProp.name.length),
        tagRange = Pair(startTagOffset, startTagOffset + node.tag.length),
        nodeLoc = node.loc,
        propsVar = ctx.getHoistVariable(propsVar),
      ))
    }
    else if (nameProp is DirectiveNode && nameProp.exp is SimpleExpressionNode) {
      val namePropExp = nameProp.exp as SimpleExpressionNode
      val isShortHand = nameProp.arg?.loc?.start?.offset == namePropExp.loc.start.offset
      if (isShortHand) {
        ctx.inlayHints.add(createVBindShorthandInlayHintInfo(namePropExp.loc, "name"))
      }
      val expVar = ctx.getInternalVariable()
      yield("var $expVar = ${names.tryAsConstant}(")
      yieldAll(generateInterpolation(
        options = options,
        ctx = ctx,
        block = options.template,
        data = if (isShortHand) codeFeatures.withoutHighlightAndCompletion else codeFeatures.all,
        code = namePropExp.content,
        start = namePropExp.loc.start.offset,
      ))
      yield(")$endOfLine")
      ctx.dynamicSlots.add(TemplateCodegenContext.DynamicSlot(
        expVar = ctx.getHoistVariable(expVar),
        propsVar = ctx.getHoistVariable(propsVar),
      ))
    }
    else {
      ctx.slots.add(TemplateCodegenContext.Slot(
        name = "default",
        offset = null,
        tagRange = Pair(startTagOffset, startTagEndOffset),
        nodeLoc = node.loc,
        propsVar = ctx.getHoistVariable(propsVar),
      ))
    }
  }
  for (child in node.children) {
    yieldAll(generateTemplateChild(options, ctx, child))
  }
}
