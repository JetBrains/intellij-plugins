// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils

import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Expression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Identifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrayBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isBindingElement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode

data class BindingIdentifier(
  val id: Identifier,
  val isRest: Boolean,
  val initializer: Expression?,
)

fun collectBindingNames(
  node: Node,
  ast: SourceFile,
): List<String> =
  collectBindingIdentifiers(node)
    .map { getNodeText(it.id, ast) }

fun collectBindingRanges(
  node: Node,
  ast: SourceFile,
): List<TextRange<Identifier>> =
  collectBindingIdentifiers(node)
    .map { getStartEnd(it.id, ast) }

fun collectBindingIdentifiers(
  node: Node,
  results: MutableList<BindingIdentifier> = mutableListOf(),
  isRest: Boolean = false,
  initializer: Expression? = null,
): List<BindingIdentifier> {
  if (isIdentifier(node)) {
    results.add(
      BindingIdentifier(
        id = node,
        isRest = isRest,
        initializer = initializer,
      ),
    )
  }
  else if (isArrayBindingPattern(node) || isObjectBindingPattern(node)) {
    for (el in node.elements) {
      if (isBindingElement(el)) {
        collectBindingIdentifiers(
          node = el.name,
          results = results,
          isRest = el.dotDotDotToken != null,
          initializer = el.initializer,
        )
      }
    }
  }
  else {
    for (child in forEachNode(node)) {
      collectBindingIdentifiers(
        node = child,
        results = results,
        isRest = false,
      )
    }
  }

  return results
}