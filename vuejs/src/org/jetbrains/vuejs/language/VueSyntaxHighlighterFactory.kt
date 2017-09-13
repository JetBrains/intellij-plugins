package org.jetbrains.vuejs.language

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class VueSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    if (project == null) return createHighlighter(VueHighlightingLexer(JSLanguageLevel.JSX))
    return createHighlighter(VueHighlightingLexer(JSRootConfiguration.getInstance(project).languageLevel))
  }

  private fun createHighlighter(lexer: Lexer): SyntaxHighlighter {
    return object : HtmlFileHighlighter() {
      override fun getHighlightingLexer(): Lexer = lexer
    }
  }
}
