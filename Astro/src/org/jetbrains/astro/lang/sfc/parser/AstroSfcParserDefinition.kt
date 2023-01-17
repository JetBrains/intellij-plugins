// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.parser

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.lang.*
import com.intellij.lang.javascript.*
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.lang.xml.XMLParserDefinition
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.HtmlEmbeddedContentImpl
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.sfc.AstroSfcLanguage
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcLexerImpl

open class AstroSfcParserDefinition : JavascriptParserDefinition() {
  override fun createLexer(project: Project): Lexer {
    return AstroSfcLexerImpl(project)
  }

  override fun createParser(project: Project): PsiParser {
    return AstroSfcParser()
  }

  override fun createJSParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
    return AstroSfcParsing(builder).tsxParser
  }

  override fun createLexerForLazyParse(project: Project, language: Language, chameleon: ASTNode): Lexer? {
    if (language == AstroSfcLanguage.INSTANCE) {
      var search: ASTNode? = chameleon
      while (search != null && search.elementType != JSStubElementTypes.EMBEDDED_EXPRESSION) {
        search = search.treeParent
      }
      if (search != null) {
        return AstroSfcLexerImpl(project, true)
      } else {
        return JSFlexAdapter(DialectOptionHolder.TS)
      }
    } else {
      return super.createLexerForLazyParse(project, language, chameleon)
    }
  }

  override fun getFileNodeType(): IFileElementType {
    return FILE
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

  companion object {
    val FILE: IFileElementType = JSFileElementType.create(AstroSfcLanguage.INSTANCE)
  }

}