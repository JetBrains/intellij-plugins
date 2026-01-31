// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.dialects

import com.intellij.lang.ASTFactory
import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageUtil
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lang.actionscript.parsing.ActionScriptParser
import com.intellij.lang.javascript.BasicJavaScriptElementFactory
import com.intellij.lang.javascript.FlexFileElementTypes
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

open class ECMAL4ParserDefinition : ASTFactory(), ParserDefinition {

  override fun createLexer(project: Project?): Lexer {
    return JSFlexAdapter(ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER)
  }

  override fun getFileNodeType(): IFileElementType {
    return FlexFileElementTypes.ECMA4_FILE
  }

  private val elementFactory: BasicJavaScriptElementFactory by lazy {
    service<BasicJavaScriptElementFactory>()
  }

  override fun getWhitespaceTokens(): TokenSet {
    return JSTokenTypes.PARSER_WHITE_SPACE_TOKENS
  }

  override fun getCommentTokens(): TokenSet {
    return JSElementTypes.COMMENTS
  }

  override fun getStringLiteralElements(): TokenSet {
    return JSTokenTypes.STRING_LITERALS
  }

  override fun createParser(project: Project?): PsiParser {
    return PsiParser { root, builder ->
      ActionScriptParser(builder).parseJS(root)
      builder.treeBuilt
    }
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return elementFactory.createFile(viewProvider, fileNodeType.language)
  }

  override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode): SpaceRequirements {
    if (left is PsiWhiteSpace) return SpaceRequirements.MAY
    if (left.elementType === JSTokenTypes.XML_LBRACE) return SpaceRequirements.MAY
    val lexer = createLexer(left.psi.project)
    return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer)
  }

  override fun createElement(node: ASTNode): PsiElement {
    return elementFactory.createElement(node)
  }

  override fun createComposite(type: IElementType): CompositeElement? {
    return elementFactory.createComposite(type)
  }
}