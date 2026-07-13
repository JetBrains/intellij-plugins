// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
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
  val startOffset = node.loc.startOffset
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
  val tagOffsets = mutableListOf(template.content.indexOf(node.tag, node.loc.startOffset))
  if (!node.isSelfClosing && template.lang == "html") {
    val endTagOffset = node.loc.startOffset + node.loc.source.lastIndexOf(node.tag)
    if (endTagOffset > tagOffsets[0]) {
      tagOffsets.add(endTagOffset)
    }
  }
  return tagOffsets
}

fun <T : PsiElement> getStartEnd(
  node: T,
): TextRange<T> {
  return TextRange(
    node = node,
    start = node.startOffset,
    end = node.endOffset,
  )
}

fun getNodeText(
  node: PsiElement,
): String {
  return node.text
}
