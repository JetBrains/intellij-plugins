// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.parser

import com.intellij.lang.PsiParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.sfc.AstroSfcFile
import org.jetbrains.astro.lang.sfc.AstroSfcFileElementType
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcLexerImpl

open class AstroSfcParserDefinition : HTMLParserDefinition() {
  override fun createLexer(project: Project): Lexer {
    return AstroSfcLexerImpl(project)
  }

  override fun createParser(project: Project): PsiParser {
    return AstroSfcParser()
  }

  override fun getFileNodeType(): IFileElementType {
    return AstroSfcFileElementType.INSTANCE
  }

  override fun getCommentTokens(): TokenSet {
    return TokenSet.orSet(XmlTokenType.COMMENTS, JSTokenTypes.COMMENTS)
  }

  override fun getStringLiteralElements(): TokenSet {
    return JSTokenTypes.STRING_LITERALS
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return AstroSfcFile(viewProvider)
  }

}