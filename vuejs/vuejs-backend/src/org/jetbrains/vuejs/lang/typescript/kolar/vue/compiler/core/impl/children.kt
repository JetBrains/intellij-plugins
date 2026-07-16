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
): List<Node> {
  if (parentScope == ParentScope.IF && tag.hasAttribute(V_FOR))
    return listOf(ForNodeImpl(tag))

  if (parentScope == ParentScope.IF && !isTemplate(tag))
    return listOf(ElementNodeImpl(tag))

  if (parentScope == ParentScope.FOR && !isTemplate(tag))
    return listOf(ElementNodeImpl(tag))

  return tag.childrenSequence
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
