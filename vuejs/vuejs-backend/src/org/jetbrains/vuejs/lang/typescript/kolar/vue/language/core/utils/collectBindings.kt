// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.getBindingElements
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrayBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode

data class BindingIdentifier(
  val id: PsiElement,
  val isRest: Boolean,
  val initializer: JSExpression?,
)

fun collectBindingNames(
  node: PsiElement,
): List<String> =
  collectBindingIdentifiers(node)
    .map { getNodeText(it.id) }

fun collectBindingRanges(
  node: PsiElement,
): List<TextRange<PsiElement>> =
  collectBindingIdentifiers(node)
    .map { getStartEnd(it.id) }

fun collectBindingIdentifiers(
  node: PsiElement,
  results: MutableList<BindingIdentifier> = mutableListOf(),
  isRest: Boolean = false,
  initializer: JSExpression? = null,
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
    for (el in node.getBindingElements()) {
      collectBindingIdentifiers(
        node = el.nameIdentifier,
        results = results,
        isRest = el.isRest,
        initializer = el.initializer,
      )
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
