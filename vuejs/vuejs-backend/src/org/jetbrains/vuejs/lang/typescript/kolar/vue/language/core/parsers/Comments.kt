// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings

fun getCommentsAtStart(
  file: JSEmbeddedContent,
): List<PsiComment> {
  val firstChild = file.firstChild
                   ?: return emptyList()

  return firstChild.siblings()
    .takeWhile { it is PsiComment || it is PsiWhiteSpace }
    .filterIsInstance<PsiComment>()
    .toList()
}

fun getCommentsBefore(
  node: PsiElement,
): List<PsiComment> {
  return node.siblings(forward = false, withSelf = false)
    .takeWhile { it is PsiComment || it is PsiWhiteSpace }
    .filterIsInstance<PsiComment>()
    .toList()
    .reversed()
}
