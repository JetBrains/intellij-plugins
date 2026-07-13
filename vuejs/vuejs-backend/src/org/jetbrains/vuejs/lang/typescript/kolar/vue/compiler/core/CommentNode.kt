// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

import com.intellij.psi.xml.XmlComment

// CompilerDOM.CommentNode
interface CommentNode : Node {
  val content: String
}

class CommentNodeImpl(
  private val comment: XmlComment,
) : CommentNode {
  override val loc: SourceLocation
    get() = PsiSourceLocation(comment)

  override val content: String
    get() = comment.commentText
}
