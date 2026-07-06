// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.lastLeaf
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node

// manual
interface FakeSourceFile : Node {
  val text: String
}

val FakeSourceFile.statements: List<Node>
  get() = forEachNode(this).toList()

fun Node.endsWithComma(): Boolean {
  // migration check
  this as PsiElement

  val leaf = lastLeaf()
  return leaf is LeafElement
         && leaf.tokenType == JSTokenTypes.COMMA
}
