// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlComment
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node

fun children(
  tag: XmlTag,
): Lazy<List<Node>> = lazy {
  tag.children.mapNotNull { child ->
    when (child) {
      is XmlTag -> ElementNodeImpl(child)
      is XmlComment -> CommentNodeImpl(child)
      else -> null
    }
  }
}
