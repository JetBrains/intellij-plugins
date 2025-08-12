// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.psi.tree.CustomLanguageASTComparator
import com.intellij.psi.tree.IElementType
import com.intellij.util.ThreeState
import com.intellij.util.diff.FlyweightCapableTreeStructure

class Angular2HtmlASTComparator : CustomLanguageASTComparator {
  override fun compareAST(oldNode: ASTNode, newNode: LighterASTNode, structure: FlyweightCapableTreeStructure<LighterASTNode>): ThreeState =
    compareTypes(oldNode.elementType, newNode.tokenType)

  override fun compareAST(oldNode: ASTNode, newNode: ASTNode): ThreeState =
    compareTypes(oldNode.elementType, newNode.elementType)

  private fun compareTypes(old: IElementType, new: IElementType): ThreeState {
    if (old is Angular2EmbeddedExprTokenType && new is Angular2EmbeddedExprTokenType) {
      return old.compareTo(new)
    }
    if (old is Angular2ElementTypes.Angular2TemplateBindingType && new is Angular2ElementTypes.Angular2TemplateBindingType) {
      return old.compareTo(new)
    }
    if (old is Angular2ElementTypes.Angular2TemplateBindingsType && new is Angular2ElementTypes.Angular2TemplateBindingsType) {
      return old.compareTo(new)
    }
    return ThreeState.UNSURE
  }
}