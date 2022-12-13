// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.psi.tree.CustomLanguageASTComparator
import com.intellij.util.ThreeState
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.vuejs.lang.expr.parser.VueJSEmbeddedExprTokenType

/**
 * When the script language of the file changes between JS and TS, we need to reparse all template expressions,
 * so that the whole file doesn't mix them.
 */
class VueASTComparator : CustomLanguageASTComparator {
  override fun compareAST(oldNode: ASTNode, newNode: LighterASTNode, structure: FlyweightCapableTreeStructure<LighterASTNode>): ThreeState {
    val old = oldNode.elementType
    val new = newNode.tokenType
    if (old is VueJSEmbeddedExprTokenType && new is VueJSEmbeddedExprTokenType) {
      if (old.langMode != new.langMode) {
        return ThreeState.NO
      }
    }

    return ThreeState.UNSURE
  }
}