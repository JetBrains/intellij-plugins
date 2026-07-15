// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.psi.xml.XmlComment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.CommentNode

class CommentNodeImpl(
  private val comment: XmlComment,
) : NodeImpl(comment),
    CommentNode {
  override val content: String
    get() = comment.commentText
}
