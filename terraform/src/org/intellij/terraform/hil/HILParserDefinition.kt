// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.psi.HILLexer
import org.intellij.terraform.hil.psi.ILPsiFile
import org.jetbrains.annotations.NotNull

class HILParserDefinition : ParserDefinition {

  override fun createLexer(project: Project) = HILLexer()

  override fun createParser(project: Project) = HILParser()

  override fun getFileNodeType() = FILE

  override fun getCommentTokens(): TokenSet {
    return TokenSet.EMPTY
  }

  override fun getStringLiteralElements() = HILTokenTypes.STRING_LITERALS

  override fun createElement(node: ASTNode): PsiElement {
    val type = node.elementType
    if (type is HILElementType) {
      return HILElementTypes.Factory.createElement(node)
    }
    if (type is HILTokenType) {
      return HILElementTypes.Factory.createElement(node)
    }
    return ASTWrapperPsiElement(node)
  }

  override fun createFile(viewProvider: @NotNull FileViewProvider): @NotNull PsiFile {
    return ILPsiFile(viewProvider)
  }

  override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?) = ParserDefinition.SpaceRequirements.MAY

  companion object {
    val FILE: IFileElementType = IFileElementType(HILLanguage)
  }
}
