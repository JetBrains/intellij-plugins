// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenSequence
import com.intellij.psi.xml.XmlComment
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node

fun children(
  tag: XmlTag,
): Lazy<List<Node>> = lazy {
  getChildren(tag)
}

fun getChildren(
  tag: XmlTag,
): List<Node> =
  tag.childrenSequence
    .mapNotNull(::getChild)
    .toList()

private val NODE_FACTORY_MAP: Map<String, ((XmlTag) -> Node)?> = mapOf(
  V_IF to ::IfNodeImpl,
  V_ELSE_IF to null,
  V_ELSE to null,
  V_FOR to ::ForNodeImpl,
)

private fun getChild(
  child: PsiElement,
): Node? {
  if (child is XmlTag) {
    for ((directiveName, factory) in NODE_FACTORY_MAP) {
      if (child.hasAttribute(directiveName)) {
        return factory?.invoke(child)
      }
    }

    return ElementNodeImpl(child)
  }

  if (child is XmlComment)
    return CommentNodeImpl(child)

  return null
}
