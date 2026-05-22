// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.PsiBuilder
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerEx
import org.jetbrains.vuejs.lang.expr.highlighting.VueJSSyntaxHighlighter
import org.jetbrains.vuejs.lang.expr.highlighting.VueTSSyntaxHighlighter
import org.jetbrains.vuejs.lang.expr.parser.VueExprParser
import org.jetbrains.vuejs.lang.expr.parser.VueJSParser
import org.jetbrains.vuejs.lang.expr.parser.VueJSParserDefinition
import org.jetbrains.vuejs.lang.expr.parser.VueTSParser
import org.jetbrains.vuejs.lang.expr.parser.VueTSParserDefinition
import org.jetbrains.vuejs.lang.html.VueFile

internal object VueScriptLangs {
  internal val LANG_MODE = Key.create<LangMode>("LANG_MODE")

  fun createLexer(langMode: LangMode, project: Project?): Lexer {
    if (langMode == LangMode.HAS_TS) {
      return VueTSParserDefinition.Util.createLexer(project)
    }
    else {
      return VueJSParserDefinition.Util.createLexer(project)
    }
  }

  fun createParser(langMode: LangMode, builder: PsiBuilder): VueExprParser {
    return if (langMode == LangMode.HAS_TS) {
      VueTSParser(builder)
    }
    else {
      VueJSParser(builder)
    }
  }

  fun createExprHighlightingLexer(langMode: LangMode): Lexer {
    return if (langMode == LangMode.HAS_TS) {
      VueTSSyntaxHighlighter().highlightingLexer
    }
    else {
      VueJSSyntaxHighlighter().highlightingLexer
    }
  }

  fun getLatestKnownLang(project: Project?, virtualFile: VirtualFile?): LangMode {
    project ?: return LangMode.DEFAULT
    virtualFile ?: return LangMode.DEFAULT
    if (!virtualFile.isValid) return LangMode.DEFAULT
    val file = if (virtualFile is VirtualFileWindow) {
      // InjectionRegistrarImpl.parseFile used in InjectionRegistrarImpl.reparse
      // doesn't set ViewProvider for some reason, and PsiManager#findFile fails because of that
      PsiManagerEx.getInstanceEx(project).fileManager.getCachedPsiFile(virtualFile)
    }
    else {
      PsiManager.getInstance(project).findFile(virtualFile)
    }
    return (file as? VueFile)?.langMode ?: LangMode.DEFAULT
  }

  fun getLatestKnownLang(element: PsiElement): LangMode {
    return (element.containingFile as? VueFile)?.langMode ?: return LangMode.DEFAULT
  }

}

