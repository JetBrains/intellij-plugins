// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.psi.ILPsiFile
import org.jetbrains.annotations.NotNull

class HILParserDefinition : ParserDefinition {
  private val FILE = IFileElementType(HILLanguage)

  override fun createLexer(project: Project): Lexer = HILLexer()

  override fun createParser(project: Project): PsiParser = HILParser()

  override fun getFileNodeType(): IFileElementType = FILE

  override fun getCommentTokens(): TokenSet {
    return TokenSet.EMPTY
  }

  override fun getStringLiteralElements(): TokenSet = HILTokenTypes.STRING_LITERALS

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

  override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements = SpaceRequirements.MAY
}
