// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlEmbeddedContentImpl
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.tree.IFileElementType
import org.jetbrains.annotations.NotNull
import org.jetbrains.vuejs.lang.html.lexer.VueLexerImpl

class VueParserDefinition : HTMLParserDefinition() {

  companion object {
    fun createLexer(project: Project, interpolationConfig: Pair<String, String>?): Lexer {
      val level = JSRootConfiguration.getInstance(project).languageLevel
      return VueLexerImpl(if (level.isES6Compatible) level else JSLanguageLevel.ES6, project, interpolationConfig)
    }
  }

  override fun createLexer(project: Project): Lexer {
    return Companion.createLexer(project, null)
  }

  override fun getFileNodeType(): IFileElementType {
    return VueFileElementType.INSTANCE
  }

  override fun createFile(viewProvider: @NotNull FileViewProvider): @NotNull PsiFile {
    return HtmlFileImpl(viewProvider, VueFileElementType.INSTANCE)
  }

  override fun createElement(node: ASTNode?): PsiElement {
    if (node?.elementType === VueElementTypes.VUE_EMBEDDED_CONTENT) {
      return HtmlEmbeddedContentImpl(node)
    }
    return super.createElement(node)
  }

  override fun createParser(project: Project?): HTMLParser = VueParser()
}
