// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenSequence
import com.intellij.psi.xml.XmlComment
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node

enum class ParentScope {
  IF,
  FOR,
  ELEMENT,

  ;
}

fun children(
  tag: XmlTag,
  parentScope: ParentScope,
): Lazy<List<Node>> = lazy {
  getChildren(tag, parentScope)
}

fun getChildren(
  tag: XmlTag,
  parentScope: ParentScope,
): List<Node> =
  when (parentScope) {
    ParentScope.IF if tag.hasAttribute(V_FOR)
      -> listOf(ForNodeImpl(tag))

    ParentScope.IF if !isTemplate(tag)
      -> listOf(ElementNodeImpl(tag))

    ParentScope.FOR if !isTemplate(tag)
      -> listOf(ElementNodeImpl(tag))

    else -> tag.childrenSequence
      .mapNotNull(::getChild)
      .toList()
  }

private val NODE_FACTORY_MAP: Map<String, (XmlTag) -> Node?> = mapOf(
  V_IF to ::IfNodeImpl,
  V_ELSE_IF to { null },
  V_ELSE to { null },
  V_FOR to ::ForNodeImpl,
)

private fun getChild(
  child: PsiElement,
): Node? {
  if (child is XmlTag) {
    for ((directiveName, factory) in NODE_FACTORY_MAP) {
      if (child.hasAttribute(directiveName)) {
        return factory(child)
      }
    }

    return ElementNodeImpl(child)
  }

  if (child is XmlComment)
    return CommentNodeImpl(child)

  return null
}
