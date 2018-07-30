// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.language

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.html.HtmlParsing
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.IStubFileElementType
import org.jetbrains.vuejs.VueLanguage

class VueParserDefinition : HTMLParserDefinition() {
  override fun createLexer(project: Project): Lexer {
    return VueLexer(JSRootConfiguration.getInstance(project).languageLevel)
  }

  override fun getFileNodeType(): IFileElementType {
    return HTML_FILE
  }

  override fun createFile(viewProvider: FileViewProvider): PsiFile {
    return HtmlFileImpl(viewProvider, HTML_FILE)
  }

  override fun createParser(project: Project?): PsiParser {
    return object: HTMLParser() {
      override fun createHtmlParsing(builder: PsiBuilder): HtmlParsing {
        return object: HtmlParsing(builder) {
          override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
            // There are heavily-used Vue components called like 'Col' or 'Input'. Unlike HTML tags <col> and <input> Vue components do have closing tags.
            // The following 'if' is a little bit hacky but it's rather tricky to solve the problem in a better way at parser level.
            if (tagName != originalTagName) {
              return false
            }
            return super.isSingleTag(tagName, originalTagName)
          }
        }
      }
    }
  }

  companion object {
    internal var HTML_FILE: IFileElementType = IStubFileElementType<PsiFileStub<HtmlFileImpl>>(VueLanguage.INSTANCE)
  }
}
