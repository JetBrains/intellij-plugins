// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils

import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.getTokenPosOfNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.TextNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRTemplate
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.hyphenate

fun hyphenateTag(str: String): String =
  hyphenate(str)

fun hyphenateAttr(str: String): String {
  var hyphencase = hyphenate(str)
  // fix https://github.com/vuejs/core/issues/8811
  if (str.isNotEmpty() && str[0] != str[0].lowercaseChar()) {
    hyphencase = "-$hyphencase"
  }
  return hyphencase
}

fun normalizeAttributeValue(
  node: TextNode,
): Pair<String, Int> {
  val source = node.loc.source
  val startOffset = node.loc.start.offset
  if (
    (source.startsWith('"') && source.endsWith('"'))
    || (source.startsWith("'") && source.endsWith("'"))
  ) {
    return Pair(source.substring(1, source.length - 1), startOffset + 1)
  }
  return Pair(source, startOffset)
}

fun getElementTagOffsets(
  node: ElementNode,
  template: IRTemplate,
): List<Int> {
  val tagOffsets = mutableListOf(template.content.indexOf(node.tag, node.loc.start.offset))
  if (!node.isSelfClosing && template.lang == "html") {
    val endTagOffset = node.loc.start.offset + node.loc.source.lastIndexOf(node.tag)
    if (endTagOffset > tagOffsets[0]) {
      tagOffsets.add(endTagOffset)
    }
  }
  return tagOffsets
}

fun <T : Node> getStartEnd(
  node: T,
  ast: SourceFile,
): TextRange<T> {
  return TextRange(
    node = node,
    start = getTokenPosOfNode(node, ast),
    end = node.end,
  )
}

fun getNodeText(
  node: Node,
  ast: SourceFile,
): String {
  val (_, start, end) = getStartEnd(node, ast)
  return ast.text.substring(start, end)
}
