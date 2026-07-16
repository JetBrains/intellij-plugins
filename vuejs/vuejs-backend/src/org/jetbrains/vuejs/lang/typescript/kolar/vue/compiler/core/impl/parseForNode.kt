// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.codeInsight.findJSExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForNode

fun parseForNode(
  tag: XmlTag,
): ForNode? {
  val expression = tag.getAttribute(V_FOR)
                     ?.valueElement
                     ?.findJSExpression<VueJSVForExpression>()
                   ?: return null

  val collectionExpression = expression.getCollectionExpression()
                             ?: return null

  return ForNodeImpl(
    tag = tag,
    parseResult = ForParseResultImpl(
      varStatement = expression.getVarStatement(),
      collectionExpression = collectionExpression,
    ),
  )
}