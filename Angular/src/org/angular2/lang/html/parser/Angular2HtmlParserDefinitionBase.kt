// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.util.asSafely
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.lexer.Angular2HtmlLexer
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorElementType

abstract class Angular2HtmlParserDefinitionBase : HTMLParserDefinition() {

  protected abstract val syntax: Angular2TemplateSyntax

  override fun createLexer(project: Project?): Lexer {
    return Angular2HtmlLexer(false, syntax, null)
  }

  override fun createParser(project: Project?): PsiParser {
    return Angular2HtmlParser(syntax)
  }

  abstract override fun getFileNodeType(): IFileElementType

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return Angular2HtmlFile(viewProvider, fileNodeType)
  }

  override fun createElement(node: ASTNode): PsiElement {
    return node.elementType.asSafely<Angular2HtmlNgContentSelectorElementType>()?.createPsi(node)
           ?: super.createElement(node)
  }
}