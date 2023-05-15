// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.highlighting

import com.intellij.lang.javascript.dialects.ECMA6SyntaxHighlighterFactory
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage

class VueJSHighlighterFactory : SyntaxHighlighterFactory() {

  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    return VueJSSyntaxHighlighter()
  }
}

class VueTSHighlighterFactory : SyntaxHighlighterFactory() {

  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    return VueTSSyntaxHighlighter()
  }
}

class VueJSSyntaxHighlighter : ECMA6SyntaxHighlighterFactory.ECMA6SyntaxHighlighter(VueJSLanguage.INSTANCE.optionHolder, false)

class VueTSSyntaxHighlighter : TypeScriptHighlighter(VueTSLanguage.INSTANCE.optionHolder, false)