// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import org.angular2.lang.html.Angular2HtmlFileElementType
import org.angular2.lang.html.lexer.Angular2HtmlLexer
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorElementType

open class Angular2HtmlParserDefinition : HTMLParserDefinition() {
  override fun createLexer(project: Project): Lexer {
    return Angular2HtmlLexer(true, null)
  }

  override fun createParser(project: Project): PsiParser {
    return Angular2HtmlParser()
  }

  override fun getFileNodeType(): IFileElementType {
    return Angular2HtmlFileElementType.INSTANCE
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return Angular2HtmlFile(viewProvider)
  }

  override fun createElement(node: ASTNode): PsiElement {
    return node.elementType.asSafely<Angular2HtmlNgContentSelectorElementType>()?.createPsi(node)
           ?: super.createElement(node)
  }
}