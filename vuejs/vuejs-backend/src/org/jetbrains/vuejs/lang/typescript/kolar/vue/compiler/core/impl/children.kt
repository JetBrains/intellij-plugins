// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlComment
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node

fun children(
  tag: XmlTag,
): Lazy<List<Node>> = lazy {
  tag.children.mapNotNull(::getChild)
}

private fun getChild(
  child: PsiElement,
): Node? {
  if (child is XmlTag)
    return ElementNodeImpl(child)

  if (child is XmlComment)
    return CommentNodeImpl(child)

  return null
}
