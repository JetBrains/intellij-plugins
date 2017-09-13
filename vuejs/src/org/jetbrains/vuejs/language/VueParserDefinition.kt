package org.jetbrains.vuejs.language

import com.intellij.lang.html.HTMLParserDefinition
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

  companion object {
    internal var HTML_FILE: IFileElementType = IStubFileElementType<PsiFileStub<HtmlFileImpl>>(VueLanguage.INSTANCE)
  }
}
