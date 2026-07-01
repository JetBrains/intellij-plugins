// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.CompoundExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.IfNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.InterpolationNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.NodeTypes
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

  if (node.type == NodeTypes.ROOT) {
    val rootNode = node as RootNode
    for (item in collectSingleRootNodes(options, rootNode.children)) {
      if (item != null) ctx.singleRootNodes.add(item)
    }
    for (child in rootNode.children) {
      yieldAll(generateTemplateChild(options, ctx, child))
    }
  }
  else if (node.type == NodeTypes.ELEMENT) {
    val elementNode = node as ElementNode
    if (elementNode.tagType == ElementTypes.SLOT) {
      yieldAll(generateSlotOutlet(options, ctx, elementNode))
    }
    else {
      val slotDir = elementNode.props.filterIsInstance<DirectiveNode>().find { it.name == "slot" }
      if (elementNode.tagType == ElementTypes.TEMPLATE && ctx.components.isNotEmpty() && slotDir != null) {
        yieldAll(generateVSlot(options, ctx, elementNode, slotDir, ctx.components.last()()))
      }
      else if (elementNode.tagType == ElementTypes.TEMPLATE && treatTemplateAsFragment) {
        yieldAll(generateFragment(options, ctx, elementNode))
      }
      else if (elementNode.tagType == ElementTypes.COMPONENT) {
        yieldAll(generateComponent(options, ctx, elementNode))
      }
      else {
        yieldAll(generateElement(options, ctx, elementNode))
      }
    }
  }
  else if (node.type == NodeTypes.COMPOUND_EXPRESSION) {
    // {{ ... }} {{ ... }}
    val compoundNode = node as CompoundExpressionNode
    for (child in compoundNode.children) {
      if (child !is Node) continue
      yieldAll(generateTemplateChild(options, ctx, child, enterNode = false))
    }
  }
  else if (node.type == NodeTypes.INTERPOLATION) {
    // {{ ... }}
    val interpolationNode = node as InterpolationNode
    val (content, start) = parseInterpolationNode(interpolationNode, options.template.content)
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
  else if (node.type == NodeTypes.IF) {
    // v-if / v-else-if / v-else
    yieldAll(generateVIf(options, ctx, node as IfNode))
  }
  else if (node.type == NodeTypes.FOR) {
    // v-for
    yieldAll(generateVFor(options, ctx, node as ForNode))
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
  val filteredChildren = children.filter { it.type != NodeTypes.COMMENT }

  if (filteredChildren.size != 1) {
    if (filteredChildren.size > 1) yield(null)
    return@sequence
  }

  val child = filteredChildren[0]
  if (child.type == NodeTypes.IF) {
    val ifNode = child as IfNode
    for (branch in ifNode.branches) {
      yieldAll(collectSingleRootNodes(options, branch.children, true))
    }
    return@sequence
  }
  else if (child.type != NodeTypes.ELEMENT) {
    return@sequence
  }

  val elementChild = child as ElementNode
  if (elementChild.tagType == ElementTypes.TEMPLATE && treatTemplateAsFragment) {
    yieldAll(collectSingleRootNodes(options, elementChild.children))
    return@sequence
  }
  yield(elementChild)

  val tag = hyphenateTag(elementChild.tag)
  if (options.vueCompilerOptions.fallthroughComponentNames.contains(tag)) {
    yieldAll(collectSingleRootNodes(options, elementChild.children))
  }
}

private fun parseInterpolationNode(
  node: InterpolationNode,
  template: IRContent,
): Pair<String, Int> {
  var start = node.content.loc.start.offset
  var end = node.content.loc.end.offset

  // fix https://github.com/vuejs/language-tools/issues/1787
  while (start > 0 && template[start - 1].isWhitespace()) {
    start--
  }
  while (end < template.length && template[end].isWhitespace()) {
    end++
  }

  return Pair(template.substring(start, end), start)
}
