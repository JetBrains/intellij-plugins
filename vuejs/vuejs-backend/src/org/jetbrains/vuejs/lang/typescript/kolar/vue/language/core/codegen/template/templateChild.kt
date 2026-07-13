// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.CommentNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.CompoundExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.IfNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.InterpolationNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.RootNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRContent
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.hyphenateTag

fun generateTemplateChild(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: Node,
  enterNode: Boolean = true,
  treatTemplateAsFragment: Boolean = false,
): Sequence<Code> = sequence {
  if (enterNode && !ctx.enter(node)) return@sequence

  if (node is RootNode) {
    for (item in collectSingleRootNodes(options, node.children)) {
      if (item != null) ctx.singleRootNodes.add(item)
    }
    for (child in node.children) {
      yieldAll(generateTemplateChild(options, ctx, child))
    }
  }
  else if (node is ElementNode) {
    if (node.tagType == ElementTypes.SLOT) {
      yieldAll(generateSlotOutlet(options, ctx, node))
    }
    else {
      val slotDir = node.props.filterIsInstance<DirectiveNode>().find { it.name == "slot" }
      if (node.tagType == ElementTypes.TEMPLATE && ctx.components.isNotEmpty() && slotDir != null) {
        yieldAll(generateVSlot(options, ctx, node, slotDir, ctx.components.last()()))
      }
      else if (node.tagType == ElementTypes.TEMPLATE && treatTemplateAsFragment) {
        yieldAll(generateFragment(options, ctx, node))
      }
      else if (node.tagType == ElementTypes.COMPONENT) {
        yieldAll(generateComponent(options, ctx, node))
      }
      else {
        yieldAll(generateElement(options, ctx, node))
      }
    }
  }
  else if (node is CompoundExpressionNode) {
    // {{ ... }} {{ ... }}
    for (child in node.children) {
      if (child !is Node) continue
      yieldAll(generateTemplateChild(options, ctx, child, enterNode = false))
    }
  }
  else if (node is InterpolationNode) {
    // {{ ... }}
    val (content, start) = parseInterpolationNode(node, options.template.content)
    yieldAll(generateInterpolation(
      options = options,
      ctx = ctx,
      block = options.template,
      data = codeFeatures.all,
      code = content,
      start = start,
      prefix = "(",
      suffix = ")$endOfLine",
    ))
  }
  else if (node is IfNode) {
    // v-if / v-else-if / v-else
    yieldAll(generateVIf(options, ctx, node))
  }
  else if (node is ForNode) {
    // v-for
    yieldAll(generateVFor(options, ctx, node))
  }

  if (enterNode) {
    yieldAll(ctx.exit())
  }
}

private fun collectSingleRootNodes(
  options: TemplateCodegenOptions,
  children: List<Node>,
  treatTemplateAsFragment: Boolean = false,
): Sequence<ElementNode?> = sequence {
  val filteredChildren = children.filter { it !is CommentNode }

  if (filteredChildren.size != 1) {
    if (filteredChildren.size > 1) yield(null)
    return@sequence
  }

  val child = filteredChildren[0]
  if (child is IfNode) {
    for (branch in child.branches) {
      yieldAll(collectSingleRootNodes(options, branch.children, true))
    }
    return@sequence
  }
  else if (child !is ElementNode) {
    return@sequence
  }

  if (child.tagType == ElementTypes.TEMPLATE && treatTemplateAsFragment) {
    yieldAll(collectSingleRootNodes(options, child.children))
    return@sequence
  }
  yield(child)

  val tag = hyphenateTag(child.tag)
  if (options.vueCompilerOptions.fallthroughComponentNames.contains(tag)) {
    yieldAll(collectSingleRootNodes(options, child.children))
  }
}

private fun parseInterpolationNode(
  node: InterpolationNode,
  template: IRContent,
): Pair<String, Int> {
  var start = node.content.loc.startOffset
  var end = node.content.loc.endOffset

  // fix https://github.com/vuejs/language-tools/issues/1787
  while (start > 0 && template[start - 1].isWhitespace()) {
    start--
  }
  while (end < template.length && template[end].isWhitespace()) {
    end++
  }

  return Pair(template.substring(start, end), start)
}
