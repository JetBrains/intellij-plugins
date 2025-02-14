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
import com.intellij.psi.tree.IFileElementType
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.html.VueFileElementType
import org.jetbrains.vuejs.lang.html.lexer.VueLexer
import org.jetbrains.vuejs.lang.html.lexer.VueParsingLexer
import org.jetbrains.vuejs.lang.html.psi.impl.VueFileImpl

class VueParserDefinition : HTMLParserDefinition() {

  object Util {
    fun createLexer(project: Project,
                    interpolationConfig: Pair<String, String>?,
                    htmlCompatMode: Boolean,
                    parentLangMode: LangMode? = null): Lexer {
      val level = JSRootConfiguration.getInstance(project).languageLevel
      return VueParsingLexer(
        VueLexer(if (level.isES6Compatible) level else JSLanguageLevel.ES6, project, interpolationConfig, htmlCompatMode, false),
        parentLangMode
      )
    }
  }

  override fun createLexer(project: Project?): Lexer {
    return Util.createLexer(project!!, null, true)
  }

  override fun getFileNodeType(): IFileElementType {
    return VueFileElementType.INSTANCE
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return VueFileImpl(viewProvider)
  }

  override fun createElement(node: ASTNode): PsiElement {
    if (node.elementType is VueElementTypes.EmbeddedVueContentElementType) {
      return HtmlEmbeddedContentImpl(node)
    }
    return super.createElement(node)
  }

  override fun createParser(project: Project?): HTMLParser = VueParser()
}
