// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.lang.*
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.xml.XMLParserDefinition
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlEmbeddedContentImpl
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.AstroFileElementType
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.lexer.AstroLexerImpl
import org.jetbrains.astro.lang.psi.AstroHtmlLiteralExpressionImpl

open class AstroParserDefinition : JavascriptParserDefinition() {
  override fun createLexer(project: Project): Lexer {
    return AstroLexerImpl(project)
  }

  override fun createParser(project: Project): PsiParser {
    return AstroParser()
  }

  override fun createJSParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
    return AstroParsing(builder).tsxParser
  }

  override fun createLexerForLazyParse(project: Project, language: Language, chameleon: ASTNode): Lexer? {
    if (language == AstroLanguage.INSTANCE) {
      var search: ASTNode? = chameleon
      while (search != null && search.elementType != JSStubElementTypes.EMBEDDED_EXPRESSION) {
        search = search.treeParent
      }
      if (search != null) {
        return AstroLexerImpl(project, true)
      }
      else {
        return JSFlexAdapter(DialectOptionHolder.TS)
      }
    }
    else {
      return super.createLexerForLazyParse(project, language, chameleon)
    }
  }

  override fun getFileNodeType(): IFileElementType {
    return AstroFileElementType.INSTANCE
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return AstroFileImpl(viewProvider)
  }

  override fun getWhitespaceTokens(): TokenSet {
    return TokenSet.orSet(super.getWhitespaceTokens(), XmlTokenType.WHITESPACES)
  }

  override fun getCommentTokens(): TokenSet {
    return TokenSet.orSet(super.getCommentTokens(), XmlTokenType.COMMENTS)
  }

  override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
    return XMLParserDefinition.canStickTokensTogether(left, right)
             .takeIf { it != ParserDefinition.SpaceRequirements.MAY }
           ?: super.spaceExistenceTypeBetweenTokens(left, right)
  }

  override fun createElement(node: ASTNode): PsiElement {
    return when (val elementType = node.elementType) {
      is XmlStubBasedElementType<*, *> -> {
        elementType.createPsi(node)
      }
      is HtmlCustomEmbeddedContentTokenType -> {
        elementType.createPsi(node)
      }
      XmlElementType.HTML_EMBEDDED_CONTENT -> {
        HtmlEmbeddedContentImpl(node)
      }
      else -> super.createElement(node)
    }
  }

  override fun createComposite(type: IElementType): CompositeElement? {
    return when (type) {
      JSElementTypes.JSX_XML_LITERAL_EXPRESSION -> {
        AstroHtmlLiteralExpressionImpl(type)
      }
      else -> super.createComposite(type)
    }
  }

}