// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenSequence
import com.intellij.psi.xml.XmlComment
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.sequenceOfNotNull
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
  if (parentScope == ParentScope.IF) {
    val forNode = parseForNode(tag)
    if (forNode != null) {
      return listOf(forNode)
    }
  }

  if ((parentScope == ParentScope.IF || parentScope == ParentScope.FOR)
      && !isTemplate(tag))
    return listOf(ElementNodeImpl(tag))

  return tag.childrenSequence
    .flatMap(::getChildrenSequence)
    .toList()
}

private val NODE_FACTORY_MAP: Map<String, (XmlTag) -> Node?> = mapOf(
  V_IF to ::IfNodeImpl,
  V_ELSE_IF to { null },
  V_ELSE to { null },
)

private fun getChildrenSequence(
  element: PsiElement,
): Sequence<Node> =
  when (element) {
    is XmlTag -> sequenceOfNotNull(getTagChild(element))
    is XmlComment -> sequenceOf(CommentNodeImpl(element))
    else -> emptySequence()
  }

private fun getTagChild(
  tag: XmlTag,
): Node? {
  for ((directiveName, factory) in NODE_FACTORY_MAP) {
    if (tag.hasAttribute(directiveName)) {
      return factory(tag)
    }
  }

  return parseForNode(tag)
         ?: ElementNodeImpl(tag)
}
