// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.psi.tree.CustomLanguageASTComparator
import com.intellij.util.ThreeState
import com.intellij.util.diff.FlyweightCapableTreeStructure

class Angular2HtmlASTComparator: CustomLanguageASTComparator {
  override fun compareAST(oldNode: ASTNode, newNode: LighterASTNode, structure: FlyweightCapableTreeStructure<LighterASTNode>): ThreeState {
    val old = oldNode.elementType
    val new = newNode.tokenType
    if (old is Angular2EmbeddedExprTokenType && new is Angular2EmbeddedExprTokenType) {
      return old.compareTo(new)
    }
    return ThreeState.UNSURE
  }
}