package com.jetbrains.lang.makefile.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.lang.makefile.psi.MakefileTypes

interface IMakeFileCommand : PsiElement {
  fun getShellCommandRange(): TextRange
}

abstract class MakefileCommandMixin(node: ASTNode) : ASTWrapperPsiElement(node), IMakeFileCommand {

  override fun getShellCommandRange(): TextRange {
    assert(node.firstChildNode.elementType == MakefileTypes.RECIPE_PREFIX)
    assert(node.firstChildNode.textLength == 1)

    var startOffset = 1
    while (startOffset < textLength) {
      val char = text[startOffset]
      if (!char.isWhitespace() && char != '@' && char != '-') break

      startOffset++;
    }

    return textRangeInParent.cutOut(TextRange.create(startOffset, textLength))
  }
}
