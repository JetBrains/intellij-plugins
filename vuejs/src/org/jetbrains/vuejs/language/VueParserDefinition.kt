// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
            if ("Col" == originalTagName) {
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
