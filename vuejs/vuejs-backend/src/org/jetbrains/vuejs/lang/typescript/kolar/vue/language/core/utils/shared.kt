// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils

import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.getTokenPosOfNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange

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
